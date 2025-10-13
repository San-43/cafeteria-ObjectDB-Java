package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.util.StringConverter;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Producto;
import org.cafeteria.cafeteria.model.Receta;

import java.math.BigDecimal;
import java.util.List;

public class RecetaFormController {
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private TextField tamanoField;
    @FXML private TextField costoPrepField;

    @FXML
    private void initialize() {
        loadProductos();
    }

    private void loadProductos() {
        EntityManager em = JPAUtil.em();
        try {
            List<Producto> productos = em.createQuery("select p from Producto p order by p.descripcion", Producto.class).getResultList();
            productoCombo.setItems(FXCollections.observableArrayList(productos));
            productoCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Producto p) { return p == null ? "" : p.descripcion; }
                @Override public Producto fromString(String s) { return null; }
            });
            productoCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Producto item, boolean empty) { super.updateItem(item, empty); setText(empty || item==null ? "" : item.descripcion); }
            });
            productoCombo.setCellFactory(cb -> new ListCell<>() {
                @Override protected void updateItem(Producto item, boolean empty) { super.updateItem(item, empty); setText(empty || item==null ? "" : item.descripcion); }
            });
        } finally { em.close(); }
    }

    @FXML
    private void onSave() {
        Producto prod = productoCombo.getValue();
        if (prod == null) { alert(Alert.AlertType.WARNING, "Campo requerido", "Selecciona un producto."); return; }
        String tam = tamanoField.getText();
        if (tam == null || tam.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El tamaño es requerido."); return; }
        BigDecimal costo = parseBigDecimal(costoPrepField.getText(), "costo de preparación");
        if (costo == null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Receta r = new Receta();
            r.producto = em.find(Producto.class, prod.idProducto); // aseguramos entidad gestionada
            r.tamano = tam.trim();
            r.costoPreparacion = costo;
            em.persist(r);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Receta guardada con ID: " + r.idReceta);
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML
    private void onClear() {
        productoCombo.getSelectionModel().clearSelection();
        tamanoField.clear();
        costoPrepField.clear();
        productoCombo.requestFocus();
    }

    private BigDecimal parseBigDecimal(String s, String label) {
        try {
            if (s == null || s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El " + label + " es requerido."); return null; }
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese un " + label + " válido (por ejemplo 12.50).");
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

