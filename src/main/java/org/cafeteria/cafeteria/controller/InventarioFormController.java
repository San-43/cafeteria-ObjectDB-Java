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
import org.cafeteria.cafeteria.model.Inventario;
import org.cafeteria.cafeteria.model.Producto;
import org.cafeteria.cafeteria.model.Tienda;

import java.util.List;
import java.util.function.Predicate;

public class InventarioFormController {
    @FXML private ComboBox<Tienda> tiendaCombo;
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private TextField stockField;
    @FXML private TableView<Inventario> inventarioTable;
    @FXML private TableColumn<Inventario, Long> idColumn;
    @FXML private TableColumn<Inventario, String> tiendaColumn;
    @FXML private TableColumn<Inventario, String> productoColumn;
    @FXML private TableColumn<Inventario, Integer> stockColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<Inventario> inventarios = FXCollections.observableArrayList();
    private FilteredList<Inventario> filteredInventarios;

    @FXML private void initialize() {
        loadTiendas();
        loadProductos();

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idInventario));
        tiendaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().tienda != null ? cell.getValue().tienda.direccion : null));
        productoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().producto != null ? cell.getValue().producto.descripcion : null));
        stockColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().stock));

        // Filtrado/ordenado
        filteredInventarios = new FilteredList<>(inventarios, it -> true);
        SortedList<Inventario> sorted = new SortedList<>(filteredInventarios);
        sorted.comparatorProperty().bind(inventarioTable.comparatorProperty());
        inventarioTable.setItems(sorted);

        inventarioTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectComboValue(tiendaCombo, selected.tienda != null ? selected.tienda.idTienda : null);
                selectComboValue(productoCombo, selected.producto != null ? selected.producto.idProducto : null);
                stockField.setText(selected.stock != null ? selected.stock.toString() : "");
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Tienda", "Producto", "Stock"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }

        loadInventario();
    }

    private void applyFilter() {
        if (filteredInventarios == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredInventarios.setPredicate(it -> true); return; }
        filteredInventarios.setPredicate(makePredicate(field, q));
    }

    private Predicate<Inventario> makePredicate(String field, String q) {
        final String selectedField = (field == null) ? "Todos" : field;
        return it -> {
            String id = it.idInventario != null ? String.valueOf(it.idInventario).toLowerCase() : "";
            String tienda = it.tienda != null ? safeLower(it.tienda.direccion) : "";
            String producto = it.producto != null ? safeLower(it.producto.descripcion) : "";
            String stock = it.stock != null ? String.valueOf(it.stock).toLowerCase() : "";
            switch (selectedField) {
                case "ID":
                    return id.contains(q);
                case "Tienda":
                    return tienda.contains(q);
                case "Producto":
                    return producto.contains(q);
                case "Stock":
                    return stock.contains(q);
                default: // "Todos"
                    return id.contains(q) || tienda.contains(q) || producto.contains(q) || stock.contains(q);
            }
        };
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

    private void loadTiendas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Tienda> tiendas = em.createQuery("select t from Tienda t order by t.direccion", Tienda.class).getResultList();
            tiendaCombo.setItems(FXCollections.observableArrayList(tiendas));
            tiendaCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Tienda t) { return t==null?"": (t.direccion + " — " + t.telefono); }
                @Override public Tienda fromString(String s) { return null; }
            });
        } finally { em.close(); }
    }

    private void loadProductos() {
        EntityManager em = JPAUtil.em();
        try {
            List<Producto> productos = em.createQuery("select p from Producto p order by p.descripcion", Producto.class).getResultList();
            productoCombo.setItems(FXCollections.observableArrayList(productos));
            productoCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Producto p) { return p==null?"": p.descripcion; }
                @Override public Producto fromString(String s) { return null; }
            });
        } finally { em.close(); }
    }

    @FXML private void onSave() {
        Tienda t = tiendaCombo.getValue();
        Producto p = productoCombo.getValue();
        Integer stock = parseInt(stockField.getText(), "stock");
        if (t==null || p==null || stock==null) { return; }
        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Inventario inv = new Inventario();
            inv.tienda = em.find(Tienda.class, t.idTienda);
            inv.producto = em.find(Producto.class, p.idProducto);
            inv.stock = stock;
            em.persist(inv);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Inventario guardado con ID: " + inv.idInventario);
            loadInventario();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onUpdate() {
        Inventario seleccionado = inventarioTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un inventario", "Debe seleccionar un registro para actualizarlo.");
            return;
        }

        Tienda t = tiendaCombo.getValue();
        Producto p = productoCombo.getValue();
        Integer stock = parseInt(stockField.getText(), "stock");
        if (t == null || p == null || stock == null) { return; }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Inventario persistido = em.find(Inventario.class, seleccionado.idInventario);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El registro ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadInventario();
                return;
            }
            persistido.tienda = em.find(Tienda.class, t.idTienda);
            persistido.producto = em.find(Producto.class, p.idProducto);
            persistido.stock = stock;
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Inventario actualizado correctamente.");
            loadInventario();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onDelete() {
        Inventario seleccionado = inventarioTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un inventario", "Debe seleccionar un registro para eliminarlo.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Inventario persistido = em.find(Inventario.class, seleccionado.idInventario);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El registro ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadInventario();
                return;
            }
            em.remove(persistido);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Inventario eliminado correctamente.");
            loadInventario();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al eliminar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        tiendaCombo.getSelectionModel().clearSelection();
        productoCombo.getSelectionModel().clearSelection();
        stockField.clear();
        inventarioTable.getSelectionModel().clearSelection();
        tiendaCombo.requestFocus();
    }

    private Integer parseInt(String s, String label) {
        try {
            if (s==null || s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El "+label+" es requerido."); return null; }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese un "+label+" válido (por ejemplo 10).");
            return null;
        }
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void loadInventario() {
        EntityManager em = JPAUtil.em();
        try {
            List<Inventario> lista = em.createQuery("select i from Inventario i order by i.idInventario", Inventario.class)
                    .getResultList();
            inventarios.setAll(lista);
            applyFilter();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }

    private <T> void selectComboValue(ComboBox<T> combo, Long id) {
        if (id == null) {
            combo.getSelectionModel().clearSelection();
            return;
        }
        combo.getItems().stream()
                .filter(item -> {
                    if (item instanceof Tienda tienda) {
                        return tienda.idTienda != null && tienda.idTienda.equals(id);
                    } else if (item instanceof Producto producto) {
                        return producto.idProducto != null && producto.idProducto.equals(id);
                    }
                    return false;
                })
                .findFirst()
                .ifPresent(combo.getSelectionModel()::select);
    }
}
