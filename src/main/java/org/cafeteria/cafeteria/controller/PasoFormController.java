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
import org.cafeteria.cafeteria.model.PasoDetalle;
import org.cafeteria.cafeteria.model.Receta;

import java.util.List;
import java.util.stream.Collectors;

public class PasoFormController {
    @FXML private ComboBox<Receta> recetaCombo;
    @FXML private TextField nombreField;
    @FXML private TextArea descripcionArea;
    @FXML private TextArea detalleArea;
    @FXML private ListView<String> detallesList;
    @FXML private TableView<Paso> pasosTable;
    @FXML private TableColumn<Paso, Long> idColumn;
    @FXML private TableColumn<Paso, String> recetaColumn;
    @FXML private TableColumn<Paso, String> nombreColumn;
    @FXML private TableColumn<Paso, String> descripcionColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<Paso> pasos = FXCollections.observableArrayList();
    private FilteredList<Paso> filteredPasos;
    private final ObservableList<String> detalles = FXCollections.observableArrayList();

    @FXML private void initialize() {
        loadRecetas();

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idPaso));
        recetaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().receta != null && cell.getValue().receta.producto != null
                        ? cell.getValue().receta.producto.descripcion + " (" + cell.getValue().receta.tamano + ")"
                        : ""));
        nombreColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().nombre));
        descripcionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().descripcion));

        if (detallesList != null) {
            detallesList.setItems(detalles);
        }

        // Filtrado/ordenado
        filteredPasos = new FilteredList<>(pasos, it -> true);
        SortedList<Paso> sorted = new SortedList<>(filteredPasos);
        sorted.comparatorProperty().bind(pasosTable.comparatorProperty());
        pasosTable.setItems(sorted);

        pasosTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectReceta(selected.receta != null ? selected.receta.idReceta : null);
                nombreField.setText(selected.nombre);
                descripcionArea.setText(selected.descripcion);
                detalles.setAll(selected.detalles == null ? List.of() : selected.detalles.stream()
                        .map(d -> d.detalle)
                        .collect(Collectors.toList()));
                detalleArea.clear();
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Receta", "Nombre", "Descripción", "Detalle"));
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
            String nombre = safeLower(it.nombre);
            String desc = safeLower(it.descripcion);
            String detalle = safeLower(it.detalles == null ? "" : it.detalles.stream()
                    .map(d -> d.detalle == null ? "" : d.detalle)
                    .collect(Collectors.joining(" ")));
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Receta": return receta.contains(q);
                case "Nombre": return nombre.contains(q);
                case "Descripción": return desc.contains(q);
                case "Detalle": return detalle.contains(q);
                default: return id.contains(q) || receta.contains(q) || nombre.contains(q) || desc.contains(q) || detalle.contains(q);
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
        String nombre = nombreField.getText();
        if (nombre == null || nombre.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El nombre del paso es requerido."); return; }
        String desc = descripcionArea.getText();
        if (desc == null || desc.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "La descripción del paso es requerida."); return; }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Paso p = new Paso();
            p.receta = em.find(Receta.class, receta.idReceta);
            p.nombre = nombre.trim();
            p.descripcion = desc.trim();
            if (!detalles.isEmpty()) {
                for (String detalle : detalles) {
                    PasoDetalle pd = new PasoDetalle();
                    pd.paso = p;
                    pd.detalle = detalle;
                    p.detalles.add(pd);
                }
            }
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
        String nombre = nombreField.getText();
        if (nombre == null || nombre.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El nombre del paso es requerido."); return; }
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
            persistido.nombre = nombre.trim();
            persistido.descripcion = desc.trim();
            persistido.detalles.clear();
            if (!detalles.isEmpty()) {
                for (String detalle : detalles) {
                    PasoDetalle pd = new PasoDetalle();
                    pd.paso = persistido;
                    pd.detalle = detalle;
                    persistido.detalles.add(pd);
                }
            }
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
        nombreField.clear();
        descripcionArea.clear();
        detalleArea.clear();
        detalles.clear();
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
            List<Paso> lista = em.createQuery("select distinct p from Paso p left join fetch p.detalles", Paso.class).getResultList();
            lista.sort((a,b) -> {
                if (a.idPaso == null || b.idPaso == null) return 0;
                return a.idPaso.compareTo(b.idPaso);
            });
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

    @FXML private void onAddDetalle() {
        if (detalleArea == null) return;
        String detalle = detalleArea.getText();
        if (detalle == null || detalle.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campo requerido", "Captura el detalle del paso antes de agregarlo.");
            return;
        }
        detalles.add(detalle.trim());
        detalleArea.clear();
    }

    @FXML private void onRemoveDetalle() {
        if (detallesList == null) return;
        String seleccionado = detallesList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un detalle", "Selecciona un detalle para eliminar.");
            return;
        }
        detalles.remove(seleccionado);
    }
}
