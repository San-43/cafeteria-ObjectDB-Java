package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Ingrediente;

public class IngredienteFormController {
    @FXML private TextField nombreField;
    @FXML private TextArea descripcionArea;
    @FXML private TextArea preparacionArea;

    @FXML
    private void onSave() {
        String nombre = nombreField.getText();
        if (nombre == null || nombre.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campo requerido", "El nombre es requerido.");
            return;
        }
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Ingrediente i = new Ingrediente();
            i.nombre = nombre.trim();
            i.descripcion = descripcionArea.getText();
            i.preparacion = preparacionArea.getText();
            em.persist(i);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Ingrediente guardado con ID: " + i.idIngrediente);
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
        nombreField.clear();
        descripcionArea.clear();
        preparacionArea.clear();
        nombreField.requestFocus();
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}

