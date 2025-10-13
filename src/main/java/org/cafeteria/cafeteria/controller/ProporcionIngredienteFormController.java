package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Ingrediente;
import org.cafeteria.cafeteria.model.ProporcionIngrediente;
import org.cafeteria.cafeteria.model.Receta;

import java.util.List;

public class ProporcionIngredienteFormController {
    @FXML private ComboBox<Receta> recetaCombo;
    @FXML private ComboBox<Ingrediente> ingredienteCombo;
    @FXML private TextField proporcionField;

    @FXML private void initialize() { loadRecetas(); loadIngredientes(); }

    private void loadRecetas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Receta> recetas = em.createQuery("select r from Receta r order by r.idReceta desc", Receta.class).getResultList();
            recetaCombo.setItems(FXCollections.observableArrayList(recetas));
            recetaCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Receta r) { return r==null?"": (r.idReceta+" — " + (r.producto!=null? r.producto.descripcion:"") + " ("+r.tamano+")"); }
                @Override public Receta fromString(String s) { return null; }
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
        Receta r = recetaCombo.getValue();
        Ingrediente i = ingredienteCombo.getValue();
        String prop = proporcionField.getText();
        if (r == null || i == null || prop == null || prop.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campos requeridos", "Selecciona receta, ingrediente y escribe la proporción.");
            return;
        }
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            ProporcionIngrediente pi = new ProporcionIngrediente();
            pi.receta = em.find(Receta.class, r.idReceta);
            pi.ingrediente = em.find(Ingrediente.class, i.idIngrediente);
            pi.proporcion = prop.trim();
            em.persist(pi);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Proporción guardada con ID: " + pi.idProporcion);
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        recetaCombo.getSelectionModel().clearSelection();
        ingredienteCombo.getSelectionModel().clearSelection();
        proporcionField.clear();
        recetaCombo.requestFocus();
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}


