package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Paso;
import org.cafeteria.cafeteria.model.Receta;

import java.util.List;

public class PasoFormController {
    @FXML private ComboBox<Receta> recetaCombo;
    @FXML private TextArea descripcionArea;

    @FXML private void initialize() { loadRecetas(); }

    private void loadRecetas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Receta> recetas = em.createQuery("select r from Receta r order by r.idReceta desc", Receta.class).getResultList();
            recetaCombo.setItems(FXCollections.observableArrayList(recetas));
            recetaCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Receta r) { return r == null ? "" : (r.idReceta + " — " + (r.producto!=null? r.producto.descripcion: "") + " (" + r.tamano + ")"); }
                @Override public Receta fromString(String s) { return null; }
            });
            recetaCombo.setCellFactory(cb -> new ListCell<>() {
                @Override protected void updateItem(Receta item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?"": (item.idReceta + " — " + (item.producto!=null? item.producto.descripcion:"") + " ("+item.tamano+")")); }
            });
        } finally { em.close(); }
    }

    @FXML private void onSave() {
        Receta receta = recetaCombo.getValue();
        if (receta == null) { alert(Alert.AlertType.WARNING, "Campo requerido", "Selecciona una receta."); return; }
        String desc = descripcionArea.getText();
        if (desc == null || desc.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "La descripción del paso es requerida."); return; }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Paso p = new Paso();
            p.receta = em.find(Receta.class, receta.idReceta);
            p.pasoDescripcion = desc.trim();
            em.persist(p);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Paso guardado con ID: " + p.idPaso);
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        recetaCombo.getSelectionModel().clearSelection();
        descripcionArea.clear();
        recetaCombo.requestFocus();
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}