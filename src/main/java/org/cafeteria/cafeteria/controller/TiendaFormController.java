package org.cafeteria.cafeteria.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import jakarta.persistence.EntityManager;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Tienda;

public class TiendaFormController {
    @FXML private TextField telefonoField;
    @FXML private TextField direccionField;
    @FXML private TextField empleadoField;

    @FXML
    private void onSave() {
        String telefono = telefonoField.getText();
        String direccion = direccionField.getText();
        String empleado = empleadoField.getText();

        if (telefono == null || telefono.isBlank() || direccion == null || direccion.isBlank() || empleado == null || empleado.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campos requeridos", "Por favor completa teléfono, dirección y responsable.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Tienda t = new Tienda();
            t.telefono = telefono.trim();
            t.direccion = direccion.trim();
            t.empleadoResponsable = empleado.trim();
            em.persist(t);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Tienda guardada con ID: " + t.idTienda);
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
        telefonoField.clear();
        direccionField.clear();
        empleadoField.clear();
        telefonoField.requestFocus();
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}

