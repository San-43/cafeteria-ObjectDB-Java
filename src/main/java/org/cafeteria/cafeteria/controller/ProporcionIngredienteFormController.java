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
import org.cafeteria.cafeteria.model.Ingrediente;
import org.cafeteria.cafeteria.model.ProporcionIngrediente;
import org.cafeteria.cafeteria.model.Receta;

import java.util.List;
import java.util.function.Predicate;

public class ProporcionIngredienteFormController {
    @FXML private ComboBox<Receta> recetaCombo;
    @FXML private ComboBox<Ingrediente> ingredienteCombo;
    @FXML private TextField proporcionField;
    @FXML private TableView<ProporcionIngrediente> proporcionesTable;
    @FXML private TableColumn<ProporcionIngrediente, Long> idColumn;
    @FXML private TableColumn<ProporcionIngrediente, String> recetaColumn;
    @FXML private TableColumn<ProporcionIngrediente, String> ingredienteColumn;
    @FXML private TableColumn<ProporcionIngrediente, String> proporcionColumn;
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<ProporcionIngrediente> proporciones = FXCollections.observableArrayList();
    private FilteredList<ProporcionIngrediente> filteredProporciones;

    @FXML private void initialize() {
        loadRecetas();
        loadIngredientes();

        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Receta", "Ingrediente", "Proporción"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idProporcion));
        recetaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().receta != null && cell.getValue().receta.producto != null
                        ? cell.getValue().receta.producto.descripcion + " (" + cell.getValue().receta.tamano + ")"
                        : ""));
        ingredienteColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().ingrediente != null ? cell.getValue().ingrediente.nombre : ""));
        proporcionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().proporcion));

        filteredProporciones = new FilteredList<>(proporciones, it -> true);
        SortedList<ProporcionIngrediente> sorted = new SortedList<>(filteredProporciones);
        sorted.comparatorProperty().bind(proporcionesTable.comparatorProperty());
        proporcionesTable.setItems(sorted);
        proporcionesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectReceta(selected.receta != null ? selected.receta.idReceta : null);
                selectIngrediente(selected.ingrediente != null ? selected.ingrediente.idIngrediente : null);
                proporcionField.setText(selected.proporcion);
            }
        });

        // initial load
        loadProporciones();
    }

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

    @FXML private void onList() {
        loadProporciones();
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
            loadProporciones();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onUpdate() {
        ProporcionIngrediente seleccionado = proporcionesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un registro", "Debe seleccionar un registro para actualizarlo.");
            return;
        }

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
            ProporcionIngrediente persistido = em.find(ProporcionIngrediente.class, seleccionado.idProporcion);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El registro ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadProporciones();
                return;
            }
            persistido.receta = em.find(Receta.class, r.idReceta);
            persistido.ingrediente = em.find(Ingrediente.class, i.idIngrediente);
            persistido.proporcion = prop.trim();
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Registro actualizado correctamente.");
            loadProporciones();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onDelete() {
        ProporcionIngrediente seleccionado = proporcionesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un registro", "Debe seleccionar un registro para eliminarlo.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            ProporcionIngrediente persistido = em.find(ProporcionIngrediente.class, seleccionado.idProporcion);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El registro ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadProporciones();
                return;
            }
            em.remove(persistido);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Registro eliminado correctamente.");
            loadProporciones();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al eliminar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        recetaCombo.getSelectionModel().clearSelection();
        ingredienteCombo.getSelectionModel().clearSelection();
        proporcionField.clear();
        proporcionesTable.getSelectionModel().clearSelection();
        recetaCombo.requestFocus();
    }

    @FXML private void onSearch() {
        applyFilter();
    }

    @FXML private void onClearSearch() {
        if (searchTextField != null) {
            searchTextField.clear();
        }
        if (searchFieldCombo != null) {
            searchFieldCombo.getSelectionModel().selectFirst();
        }
        applyFilter();
        if (searchTextField != null) {
            searchTextField.requestFocus();
        }
    }

    private void applyFilter() {
        if (filteredProporciones == null) return;
        String query = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (query.isBlank()) { filteredProporciones.setPredicate(it -> true); return; }
        filteredProporciones.setPredicate(makePredicate(field, query));
    }

    private Predicate<ProporcionIngrediente> makePredicate(String field, String query) {
        final String selectedField = field == null ? "Todos" : field;
        return it -> {
            String id = it.idProporcion != null ? String.valueOf(it.idProporcion).toLowerCase() : "";
            String receta = "";
            if (it.receta != null) {
                String prodDesc = it.receta.producto != null ? it.receta.producto.descripcion : "";
                receta = safeLower(prodDesc + " " + it.receta.tamano);
            }
            String ingrediente = it.ingrediente != null ? safeLower(it.ingrediente.nombre) : "";
            String proporcion = safeLower(it.proporcion);
            switch (selectedField) {
                case "ID":
                    return id.contains(query);
                case "Receta":
                    return receta.contains(query);
                case "Ingrediente":
                    return ingrediente.contains(query);
                case "Proporción":
                    return proporcion.contains(query);
                default:
                    return id.contains(query) || receta.contains(query) || ingrediente.contains(query) || proporcion.contains(query);
            }
        };
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void loadProporciones() {
        EntityManager em = JPAUtil.em();
        try {
            List<ProporcionIngrediente> lista = em.createQuery(
                            "select p from ProporcionIngrediente p order by p.idProporcion",
                            ProporcionIngrediente.class)
                    .getResultList();
            proporciones.setAll(lista);
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

    private void selectIngrediente(Long id) {
        if (id == null) {
            ingredienteCombo.getSelectionModel().clearSelection();
            return;
        }
        ingredienteCombo.getItems().stream()
                .filter(ing -> ing.idIngrediente != null && ing.idIngrediente.equals(id))
                .findFirst()
                .ifPresent(ingredienteCombo.getSelectionModel()::select);
    }
}