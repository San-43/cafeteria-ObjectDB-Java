package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Ingrediente;

public class IngredienteFormController {
    @FXML private TextField nombreField;
    @FXML private TextArea descripcionArea;
    @FXML private TextArea preparacionArea;
    @FXML private TableView<Ingrediente> ingredientesTable;
    @FXML private TableColumn<Ingrediente, Long> idColumn;
    @FXML private TableColumn<Ingrediente, String> nombreColumn;
    @FXML private TableColumn<Ingrediente, String> descripcionColumn;
    @FXML private TableColumn<Ingrediente, String> preparacionColumn;

    private final ObservableList<Ingrediente> ingredientes = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idIngrediente));
        nombreColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().nombre));
        descripcionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().descripcion));
        preparacionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().preparacion));

        ingredientesTable.setItems(ingredientes);
        ingredientesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                nombreField.setText(selected.nombre);
                descripcionArea.setText(selected.descripcion);
                preparacionArea.setText(selected.preparacion);
            }
        });

        loadIngredientes();
    }

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
            loadIngredientes();
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
    private void onUpdate() {
        Ingrediente seleccionado = ingredientesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un ingrediente", "Debe seleccionar un ingrediente para actualizarlo.");
            return;
        }

        String nombre = nombreField.getText();
        if (nombre == null || nombre.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campo requerido", "El nombre es requerido.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Ingrediente persistido = em.find(Ingrediente.class, seleccionado.idIngrediente);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El ingrediente ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadIngredientes();
                return;
            }
            persistido.nombre = nombre.trim();
            persistido.descripcion = descripcionArea.getText();
            persistido.preparacion = preparacionArea.getText();
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Ingrediente actualizado correctamente.");
            loadIngredientes();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }

    @FXML
    private void onDelete() {
        Ingrediente seleccionado = ingredientesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un ingrediente", "Debe seleccionar un ingrediente para eliminarlo.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Ingrediente persistido = em.find(Ingrediente.class, seleccionado.idIngrediente);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El ingrediente ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadIngredientes();
                return;
            }
            em.remove(persistido);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Ingrediente eliminado correctamente.");
            loadIngredientes();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al eliminar", ex.getMessage());
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
        ingredientesTable.getSelectionModel().clearSelection();
        nombreField.requestFocus();
    }

    private void loadIngredientes() {
        EntityManager em = JPAUtil.em();
        try {
            var lista = em.createQuery("select i from Ingrediente i order by i.idIngrediente", Ingrediente.class)
                    .getResultList();
            ingredientes.setAll(lista);
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}