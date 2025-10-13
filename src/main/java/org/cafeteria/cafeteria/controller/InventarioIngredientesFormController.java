package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Ingrediente;
import org.cafeteria.cafeteria.model.InventarioIngredientes;
import org.cafeteria.cafeteria.model.Tienda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InventarioIngredientesFormController {
    @FXML private ComboBox<Tienda> tiendaCombo;
    @FXML private ComboBox<Ingrediente> ingredienteCombo;
    @FXML private DatePicker fechaCompraPicker;
    @FXML private DatePicker fechaCaducidadPicker;
    @FXML private TextField costoCompraField;
    @FXML private TextField precioPorcionField;

    @FXML private void initialize() { loadTiendas(); loadIngredientes(); }

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

    private void loadIngredientes() {
        EntityManager em = JPAUtil.em();
        try {
            List<Ingrediente> ingredientes = em.createQuery("select i from Ingrediente i order by i.nombre", Ingrediente.class).getResultList();
            ingredienteCombo.setItems(FXCollections.observableArrayList(ingredientes));
            ingredienteCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Ingrediente i) { return i==null?"": i.nombre; }
                @Override public Ingrediente fromString(String s) { return null; }
            });
        } finally { em.close(); }
    }

    @FXML private void onSave() {
        Tienda t = tiendaCombo.getValue();
        Ingrediente ing = ingredienteCombo.getValue();
        LocalDate compra = fechaCompraPicker.getValue();
        LocalDate cad = fechaCaducidadPicker.getValue();
        BigDecimal costo = parseBigDecimal(costoCompraField.getText(), "costo de compra");
        BigDecimal precio = parseBigDecimal(precioPorcionField.getText(), "precio por porción");
        if (t==null || ing==null || compra==null || cad==null || costo==null || precio==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            InventarioIngredientes ii = new InventarioIngredientes();
            ii.tienda = em.find(Tienda.class, t.idTienda);
            ii.ingrediente = em.find(Ingrediente.class, ing.idIngrediente);
            ii.fechaCompra = compra;
            ii.fechaCaducidad = cad;
            ii.costoCompra = costo;
            ii.precioVentaPorcion = precio;
            em.persist(ii);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Inventario de ingrediente guardado con ID: " + ii.idInventarioIngredientes);
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        tiendaCombo.getSelectionModel().clearSelection();
        ingredienteCombo.getSelectionModel().clearSelection();
        fechaCompraPicker.setValue(null);
        fechaCaducidadPicker.setValue(null);
        costoCompraField.clear();
        precioPorcionField.clear();
        tiendaCombo.requestFocus();
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

