package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    @FXML private TableView<Paso> pasosTable;
    @FXML private TableColumn<Paso, Long> idColumn;
    @FXML private TableColumn<Paso, String> recetaColumn;
    @FXML private TableColumn<Paso, String> descripcionColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<Paso> pasos = FXCollections.observableArrayList();
    private FilteredList<Paso> filteredPasos;

    @FXML private void initialize() {
        loadRecetas();

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idPaso));
        recetaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().receta != null && cell.getValue().receta.producto != null
                        ? cell.getValue().receta.producto.descripcion + " (" + cell.getValue().receta.tamano + ")"
                        : ""));
        descripcionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().pasoDescripcion));

        // Filtrado/ordenado
        filteredPasos = new FilteredList<>(pasos, it -> true);
        SortedList<Paso> sorted = new SortedList<>(filteredPasos);
        sorted.comparatorProperty().bind(pasosTable.comparatorProperty());
        pasosTable.setItems(sorted);

        pasosTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectReceta(selected.receta != null ? selected.receta.idReceta : null);
                descripcionArea.setText(selected.pasoDescripcion);
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Receta", "Descripción"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }
    }

    private void applyFilter() {
        if (filteredPasos == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredPasos.setPredicate(it -> true); return; }
        final String selectedField = (field == null) ? "Todos" : field;
        filteredPasos.setPredicate(it -> {
            String id = it.idPaso != null ? String.valueOf(it.idPaso).toLowerCase() : "";
            String receta = (it.receta!=null && it.receta.producto!=null ? safeLower(it.receta.producto.descripcion + " " + it.receta.tamano) : "");
            String desc = safeLower(it.pasoDescripcion);
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Receta": return receta.contains(q);
                case "Descripción": return desc.contains(q);
                default: return id.contains(q) || receta.contains(q) || desc.contains(q);
            }
        });
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

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

    @FXML private void onList() {
        loadPasos();
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
            loadPasos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onUpdate() {
        Paso seleccionado = pasosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un paso", "Debe seleccionar un paso para actualizarlo.");
            return;
        }

        Receta receta = recetaCombo.getValue();
        if (receta == null) { alert(Alert.AlertType.WARNING, "Campo requerido", "Selecciona una receta."); return; }
        String desc = descripcionArea.getText();
        if (desc == null || desc.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "La descripción del paso es requerida."); return; }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Paso persistido = em.find(Paso.class, seleccionado.idPaso);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El paso ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadPasos();
                return;
            }
            persistido.receta = em.find(Receta.class, receta.idReceta);
            persistido.pasoDescripcion = desc.trim();
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Paso actualizado correctamente.");
            loadPasos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onDelete() {
        Paso seleccionado = pasosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un paso", "Debe seleccionar un paso para eliminarlo.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Paso persistido = em.find(Paso.class, seleccionado.idPaso);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El paso ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadPasos();
                return;
            }
            em.remove(persistido);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Paso eliminado correctamente.");
            loadPasos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al eliminar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        recetaCombo.getSelectionModel().clearSelection();
        descripcionArea.clear();
        pasosTable.getSelectionModel().clearSelection();
        recetaCombo.requestFocus();
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void loadPasos() {
        EntityManager em = JPAUtil.em();
        try {
            List<Paso> lista = em.createQuery("select p from Paso p order by p.idPaso", Paso.class).getResultList();
            pasos.setAll(lista);
            applyFilter();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    private void selectReceta(Long id) {
        if (id == null) {
            recetaCombo.getSelectionModel().clearSelection();
            return;
        }
        recetaCombo.getItems().stream()
                .filter(r -> r.idReceta != null && r.idReceta.equals(id))
                .findFirst()
                .ifPresent(recetaCombo.getSelectionModel()::select);
    }
}