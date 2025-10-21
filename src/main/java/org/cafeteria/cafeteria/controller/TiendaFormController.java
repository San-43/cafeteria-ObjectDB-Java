package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Tienda;

public class TiendaFormController {
    @FXML private TextField telefonoField;
    @FXML private TextField direccionField;
    @FXML private TextField empleadoField;
    @FXML private TableView<Tienda> tiendasTable;
    @FXML private TableColumn<Tienda, String> idColumn;
    @FXML private TableColumn<Tienda, String> telefonoColumn;
    @FXML private TableColumn<Tienda, String> direccionColumn;
    @FXML private TableColumn<Tienda, String> empleadoColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<Tienda> tiendas = FXCollections.observableArrayList();
    private FilteredList<Tienda> filteredTiendas;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idTienda));
        telefonoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().telefono));
        direccionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().direccion));
        empleadoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().empleadoResponsable));

        filteredTiendas = new FilteredList<>(tiendas, it -> true);
        SortedList<Tienda> sorted = new SortedList<>(filteredTiendas);
        sorted.comparatorProperty().bind(tiendasTable.comparatorProperty());
        tiendasTable.setItems(sorted);

        tiendasTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                telefonoField.setText(selected.telefono);
                direccionField.setText(selected.direccion);
                empleadoField.setText(selected.empleadoResponsable);
            }
        });

        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Teléfono", "Dirección", "Empleado"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }
    }

    private void applyFilter() {
        if (filteredTiendas == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredTiendas.setPredicate(it -> true); return; }
        final String selectedField = (field == null) ? "Todos" : field;
        filteredTiendas.setPredicate(it -> {
            String id = it.idTienda != null ? it.idTienda.toLowerCase() : "";
            String tel = safeLower(it.telefono);
            String dir = safeLower(it.direccion);
            String emp = safeLower(it.empleadoResponsable);
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Teléfono": return tel.contains(q);
                case "Dirección": return dir.contains(q);
                case "Empleado": return emp.contains(q);
                default: return id.contains(q) || tel.contains(q) || dir.contains(q) || emp.contains(q);
            }
        });
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

    @FXML
    private void onList() {
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
            applyFilter();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }
}