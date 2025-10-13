package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Producto;
import org.cafeteria.cafeteria.model.ProductoVendido;
import org.cafeteria.cafeteria.model.Venta;

import java.math.BigDecimal;
import java.util.List;

public class ProductoVendidoFormController {
    @FXML private ComboBox<Venta> ventaCombo;
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private TextField cantidadField;
    @FXML private TextField precioField;

    @FXML private void initialize() { loadVentas(); loadProductos(); }

    private void loadVentas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Venta> ventas = em.createQuery("select v from Venta v order by v.fecha desc", Venta.class).getResultList();
            ventaCombo.setItems(FXCollections.observableArrayList(ventas));
            ventaCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Venta v) { return v==null?"": ("#"+v.idVenta + " — " + (v.tienda!=null? v.tienda.direccion:"") + " — " + v.fecha); }
                @Override public Venta fromString(String s) { return null; }
            });
        } finally { em.close(); }
    }

    private void loadProductos() {
        EntityManager em = JPAUtil.em();
        try {
            List<Producto> productos = em.createQuery("select p from Producto p order by p.descripcion", Producto.class).getResultList();
            productoCombo.setItems(FXCollections.observableArrayList(productos));
            productoCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Producto p) { return p==null?"": p.descripcion; }
                @Override public Producto fromString(String s) { return null; }
            });
        } finally { em.close(); }
    }

    @FXML private void onSave() {
        Venta v = ventaCombo.getValue();
        Producto p = productoCombo.getValue();
        Integer cant = parseInt(cantidadField.getText(), "cantidad");
        BigDecimal precio = parseBigDecimal(precioField.getText(), "precio");
        if (v==null || p==null || cant==null || precio==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            ProductoVendido pv = new ProductoVendido();
            pv.venta = em.find(Venta.class, v.idVenta);
            pv.producto = em.find(Producto.class, p.idProducto);
            pv.cantidad = cant;
            pv.precio = precio;
            em.persist(pv);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Producto vendido guardado con ID: " + pv.idProductoVendido);
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        ventaCombo.getSelectionModel().clearSelection();
        productoCombo.getSelectionModel().clearSelection();
        cantidadField.clear();
        precioField.clear();
        ventaCombo.requestFocus();
    }

    private Integer parseInt(String s, String label) {
        try {
            if (s==null||s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "La "+label+" es requerida."); return null; }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese una "+label+" válida (por ejemplo 3).");
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String s, String label) {
        try {
            if (s==null||s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El "+label+" es requerido."); return null; }
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese un "+label+" válido (por ejemplo 12.50).");
            return null;
        }
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}

