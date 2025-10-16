package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Tienda;

public class TiendaFormController {
    @FXML private TextField telefonoField;
    @FXML private TextField direccionField;
    @FXML private TextField empleadoField;
    @FXML private TableView<Tienda> tiendasTable;
    @FXML private TableColumn<Tienda, Long> idColumn;
    @FXML private TableColumn<Tienda, String> telefonoColumn;
    @FXML private TableColumn<Tienda, String> direccionColumn;
    @FXML private TableColumn<Tienda, String> empleadoColumn;

    private final ObservableList<Tienda> tiendas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idTienda));
        telefonoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().telefono));
        direccionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().direccion));
        empleadoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().empleadoResponsable));

        tiendasTable.setItems(tiendas);
        tiendasTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                telefonoField.setText(selected.telefono);
                direccionField.setText(selected.direccion);
                empleadoField.setText(selected.empleadoResponsable);
            }
        });

        loadTiendas();
    }

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
            loadTiendas();
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
        Tienda seleccionada = tiendasTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alert(Alert.AlertType.WARNING, "Seleccione una tienda", "Debe seleccionar una tienda para actualizarla.");
            return;
        }

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
            Tienda persistida = em.find(Tienda.class, seleccionada.idTienda);
            if (persistida == null) {
                alert(Alert.AlertType.ERROR, "No encontrada", "La tienda ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadTiendas();
                return;
            }
            persistida.telefono = telefono.trim();
            persistida.direccion = direccion.trim();
            persistida.empleadoResponsable = empleado.trim();
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Tienda actualizada correctamente.");
            loadTiendas();
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
        Tienda seleccionada = tiendasTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alert(Alert.AlertType.WARNING, "Seleccione una tienda", "Debe seleccionar una tienda para eliminarla.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Tienda persistida = em.find(Tienda.class, seleccionada.idTienda);
            if (persistida == null) {
                alert(Alert.AlertType.ERROR, "No encontrada", "La tienda ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadTiendas();
                return;
            }
            em.remove(persistida);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Tienda eliminada correctamente.");
            loadTiendas();
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
        telefonoField.clear();
        direccionField.clear();
        empleadoField.clear();
        tiendasTable.getSelectionModel().clearSelection();
        telefonoField.requestFocus();
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void loadTiendas() {
        EntityManager em = JPAUtil.em();
        try {
            var lista = em.createQuery("select t from Tienda t order by t.idTienda", Tienda.class)
                    .getResultList();
            tiendas.setAll(lista);
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }
}