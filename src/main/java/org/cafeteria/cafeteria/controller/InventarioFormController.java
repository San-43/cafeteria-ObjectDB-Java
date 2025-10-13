package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Inventario;
import org.cafeteria.cafeteria.model.Producto;
import org.cafeteria.cafeteria.model.Tienda;

import java.util.List;

public class InventarioFormController {
    @FXML private ComboBox<Tienda> tiendaCombo;
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private TextField stockField;

    @FXML private void initialize() { loadTiendas(); loadProductos(); }

    private void loadTiendas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Tienda> tiendas = em.createQuery("select t from Tienda t order by t.direccion", Tienda.class).getResultList();
            tiendaCombo.setItems(FXCollections.observableArrayList(tiendas));
            tiendaCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Tienda t) { return t==null?"": (t.direccion + " — " + t.telefono); }
                @Override public Tienda fromString(String s) { return null; }
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
        Tienda t = tiendaCombo.getValue();
        Producto p = productoCombo.getValue();
        Integer stock = parseInt(stockField.getText(), "stock");
        if (t==null || p==null || stock==null) { return; }
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Inventario inv = new Inventario();
            inv.tienda = em.find(Tienda.class, t.idTienda);
            inv.producto = em.find(Producto.class, p.idProducto);
            inv.stock = stock;
            em.persist(inv);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Inventario guardado con ID: " + inv.idInventario);
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        tiendaCombo.getSelectionModel().clearSelection();
        productoCombo.getSelectionModel().clearSelection();
        stockField.clear();
        tiendaCombo.requestFocus();
    }

    private Integer parseInt(String s, String label) {
        try {
            if (s==null || s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El "+label+" es requerido."); return null; }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese un "+label+" válido (por ejemplo 10).");
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

