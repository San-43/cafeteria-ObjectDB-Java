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
import org.cafeteria.cafeteria.model.Producto;
import org.cafeteria.cafeteria.model.Receta;

import java.math.BigDecimal;
import java.util.List;

public class RecetaFormController {
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private TextField tamanoField;
    @FXML private TextField costoPrepField;
    @FXML private TableView<Receta> recetasTable;
    @FXML private TableColumn<Receta, Long> idColumn;
    @FXML private TableColumn<Receta, String> productoColumn;
    @FXML private TableColumn<Receta, String> tamanoColumn;
    @FXML private TableColumn<Receta, BigDecimal> costoColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<Receta> recetas = FXCollections.observableArrayList();
    private FilteredList<Receta> filteredRecetas;

    @FXML
    private void initialize() {
        loadProductos();

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idReceta));
        productoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().producto != null ? cell.getValue().producto.descripcion : ""));
        tamanoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().tamano));
        costoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().costoPreparacion));

        // Tabla con filtrado/ordenado
        filteredRecetas = new FilteredList<>(recetas, it -> true);
        SortedList<Receta> sorted = new SortedList<>(filteredRecetas);
        sorted.comparatorProperty().bind(recetasTable.comparatorProperty());
        recetasTable.setItems(sorted);

        recetasTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectProducto(selected.producto != null ? selected.producto.idProducto : null);
                tamanoField.setText(selected.tamano);
                costoPrepField.setText(selected.costoPreparacion != null ? selected.costoPreparacion.toPlainString() : "");
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Producto", "Tamaño", "Costo"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }
    }

    private void applyFilter() {
        if (filteredRecetas == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredRecetas.setPredicate(it -> true); return; }
        final String selectedField = (field == null) ? "Todos" : field;
        filteredRecetas.setPredicate(it -> {
            String id = it.idReceta != null ? String.valueOf(it.idReceta).toLowerCase() : "";
            String prod = it.producto != null ? safeLower(it.producto.descripcion) : "";
            String tam = safeLower(it.tamano);
            String costo = it.costoPreparacion != null ? it.costoPreparacion.toPlainString().toLowerCase() : "";
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Producto": return prod.contains(q);
                case "Tamaño": return tam.contains(q);
                case "Costo": return costo.contains(q);
                default: return id.contains(q) || prod.contains(q) || tam.contains(q) || costo.contains(q);
            }
        });
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

    private void loadProductos() {
        EntityManager em = JPAUtil.em();
        try {
            List<Producto> productos = em.createQuery("select p from Producto p order by p.descripcion", Producto.class).getResultList();
            productoCombo.setItems(FXCollections.observableArrayList(productos));
            productoCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Producto p) { return p == null ? "" : p.descripcion; }
                @Override public Producto fromString(String s) { return null; }
            });
            productoCombo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Producto item, boolean empty) { super.updateItem(item, empty); setText(empty || item==null ? "" : item.descripcion); }
            });
            productoCombo.setCellFactory(cb -> new ListCell<>() {
                @Override protected void updateItem(Producto item, boolean empty) { super.updateItem(item, empty); setText(empty || item==null ? "" : item.descripcion); }
            });
        } finally { em.close(); }
    }

    @FXML
    private void onList() {
        loadRecetas();
    }

    @FXML
    private void onSave() {
        Producto prod = productoCombo.getValue();
        if (prod == null) { alert(Alert.AlertType.WARNING, "Campo requerido", "Selecciona un producto."); return; }
        String tam = tamanoField.getText();
        if (tam == null || tam.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El tamaño es requerido."); return; }
        BigDecimal costo = parseBigDecimal(costoPrepField.getText(), "costo de preparación");
        if (costo == null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Receta r = new Receta();
            r.producto = em.find(Producto.class, prod.idProducto); // aseguramos entidad gestionada
            r.tamano = tam.trim();
            r.costoPreparacion = costo;
            em.persist(r);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Receta guardada con ID: " + r.idReceta);
            loadRecetas();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML
    private void onUpdate() {
        Receta seleccionada = recetasTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alert(Alert.AlertType.WARNING, "Seleccione una receta", "Debe seleccionar una receta para actualizarla.");
            return;
        }

        Producto prod = productoCombo.getValue();
        if (prod == null) { alert(Alert.AlertType.WARNING, "Campo requerido", "Selecciona un producto."); return; }
        String tam = tamanoField.getText();
        if (tam == null || tam.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El tamaño es requerido."); return; }
        BigDecimal costo = parseBigDecimal(costoPrepField.getText(), "costo de preparación");
        if (costo == null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Receta persistida = em.find(Receta.class, seleccionada.idReceta);
            if (persistida == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "La receta ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadRecetas();
                return;
            }
            persistida.producto = em.find(Producto.class, prod.idProducto);
            persistida.tamano = tam.trim();
            persistida.costoPreparacion = costo;
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Receta actualizada correctamente.");
            loadRecetas();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML
    private void onDelete() {
        Receta seleccionada = recetasTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alert(Alert.AlertType.WARNING, "Seleccione una receta", "Debe seleccionar una receta para eliminarla.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Receta persistida = em.find(Receta.class, seleccionada.idReceta);
            if (persistida == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "La receta ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadRecetas();
                return;
            }
            em.remove(persistida);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Receta eliminada correctamente.");
            loadRecetas();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al eliminar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML
    private void onClear() {
        productoCombo.getSelectionModel().clearSelection();
        tamanoField.clear();
        costoPrepField.clear();
        recetasTable.getSelectionModel().clearSelection();
        productoCombo.requestFocus();
    }

    private BigDecimal parseBigDecimal(String s, String label) {
        try {
            if (s == null || s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El " + label + " es requerido."); return null; }
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese un " + label + " válido (por ejemplo 12.50).");
            return null;
        }
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void loadRecetas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Receta> lista = em.createQuery("select r from Receta r order by r.idReceta", Receta.class)
                    .getResultList();
            recetas.setAll(lista);
            applyFilter();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    private void selectProducto(Long id) {
        if (id == null) {
            productoCombo.getSelectionModel().clearSelection();
            return;
        }
        productoCombo.getItems().stream()
                .filter(prod -> prod.idProducto != null && prod.idProducto.equals(id))
                .findFirst()
                .ifPresent(productoCombo.getSelectionModel()::select);
    }
}