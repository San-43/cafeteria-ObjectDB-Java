package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Producto;

import java.math.BigDecimal;

public class ProductoFormController {
    @FXML private TextField descripcionField;
    @FXML private TextField costoField;
    @FXML private TextField precioVentaField;

    @FXML
    private void onSave() {
        String desc = descripcionField.getText();
        if (desc == null || desc.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campo requerido", "La descripción es requerida.");
            return;
        }
        BigDecimal costo = parseBigDecimal(costoField.getText(), "costo");
        if (costo == null) return;
        BigDecimal precio = parseBigDecimal(precioVentaField.getText(), "precio de venta");
        if (precio == null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Producto p = new Producto();
            p.descripcion = desc.trim();
            p.costo = costo;
            p.precioVenta = precio;
            em.persist(p);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Producto guardado con ID: " + p.idProducto);
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }

    @FXML
    private void onClear() {
        descripcionField.clear();
        costoField.clear();
        precioVentaField.clear();
        descripcionField.requestFocus();
    }

    private BigDecimal parseBigDecimal(String s, String label) {
        try {
            if (s == null || s.isBlank()) {
                alert(Alert.AlertType.WARNING, "Campo requerido", "El " + label + " es requerido.");
                return null;
            }
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

