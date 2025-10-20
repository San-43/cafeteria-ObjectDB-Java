package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    @FXML private TableView<ProporcionIngrediente> proporcionesTable;
    @FXML private TableColumn<ProporcionIngrediente, Long> idColumn;
    @FXML private TableColumn<ProporcionIngrediente, String> recetaColumn;
    @FXML private TableColumn<ProporcionIngrediente, String> ingredienteColumn;
    @FXML private TableColumn<ProporcionIngrediente, String> proporcionColumn;
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<ProporcionIngrediente> proporciones = FXCollections.observableArrayList();

    @FXML private void initialize() {
        loadRecetas();
        loadIngredientes();

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idProporcion));
        recetaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().receta != null && cell.getValue().receta.producto != null
                        ? cell.getValue().receta.producto.descripcion + " (" + cell.getValue().receta.tamano + ")"
                        : ""));
        ingredienteColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().ingrediente != null ? cell.getValue().ingrediente.nombre : ""));
        proporcionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().proporcion));

        proporcionesTable.setItems(proporciones);
        proporcionesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectReceta(selected.receta != null ? selected.receta.idReceta : null);
                selectIngrediente(selected.ingrediente != null ? selected.ingrediente.idIngrediente : null);
                proporcionField.setText(selected.proporcion);
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList(
                    "Todos", "ID", "Receta", "Ingrediente", "Proporción"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }
    }

    private void applyFilter() {
        String field = searchFieldCombo == null ? "Todos" : searchFieldCombo.getValue();
        String text = searchTextField == null ? "" : searchTextField.getText();
        if (field == null) field = "Todos";
        if (field.equals("Todos") || text == null || text.isBlank()) {
            loadProporciones();
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            jakarta.persistence.TypedQuery<ProporcionIngrediente> query;
            switch (field) {
                case "ID":
                    try {
                        Long id = Long.parseLong(text.trim());
                        query = em.createQuery("select p from ProporcionIngrediente p where p.idProporcion = :id", ProporcionIngrediente.class);
                        query.setParameter("id", id);
                    } catch (NumberFormatException nfe) {
                        alert(Alert.AlertType.WARNING, "Buscar por ID", "ID inválido.");
                        return;
                    }
                    break;
                case "Receta":
                    String tRec = "%" + text.trim().toLowerCase() + "%";
                    query = em.createQuery(
                            "select p from ProporcionIngrediente p join p.receta r join r.producto prod " +
                                    "where lower(prod.descripcion) like :t or lower(r.tamano) like :t",
                            ProporcionIngrediente.class);
                    query.setParameter("t", tRec);
                    break;
                case "Ingrediente":
                    String tIng = "%" + text.trim().toLowerCase() + "%";
                    query = em.createQuery(
                            "select p from ProporcionIngrediente p join p.ingrediente i where lower(i.nombre) like :t",
                            ProporcionIngrediente.class);
                    query.setParameter("t", tIng);
                    break;
                case "Proporción":
                    String tProp = "%" + text.trim().toLowerCase() + "%";
                    query = em.createQuery(
                            "select p from ProporcionIngrediente p where lower(p.proporcion) like :t",
                            ProporcionIngrediente.class);
                    query.setParameter("t", tProp);
                    break;
                default:
                    loadProporciones();
                    return;
            }
            List<ProporcionIngrediente> lista = query.getResultList();
            proporciones.setAll(lista);
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al buscar", ex.getMessage());
            ex.printStackTrace();
        } finally {
            em.close();
        }
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