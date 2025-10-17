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
import org.cafeteria.cafeteria.model.Producto;

import java.math.BigDecimal;
import java.util.List;

public class ProductoFormController {
    @FXML private TextField descripcionField;
    @FXML private TextField costoField;
    @FXML private TextField precioVentaField;
    @FXML private TableView<Producto> productosTable;
    @FXML private TableColumn<Producto, Long> idColumn;
    @FXML private TableColumn<Producto, String> descripcionColumn;
    @FXML private TableColumn<Producto, BigDecimal> costoColumn;
    @FXML private TableColumn<Producto, BigDecimal> precioColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> filteredProductos;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idProducto));
        descripcionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().descripcion));
        costoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().costo));
        precioColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().precioVenta));

        // Datos + ordenado/filtrado
        filteredProductos = new FilteredList<>(productos, it -> true);
        SortedList<Producto> sorted = new SortedList<>(filteredProductos);
        sorted.comparatorProperty().bind(productosTable.comparatorProperty());
        productosTable.setItems(sorted);

        productosTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                descripcionField.setText(selected.descripcion);
                costoField.setText(selected.costo != null ? selected.costo.toPlainString() : "");
                precioVentaField.setText(selected.precioVenta != null ? selected.precioVenta.toPlainString() : "");
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Descripción", "Costo", "Precio"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }

        loadProductos();
    }

    private void applyFilter() {
        if (filteredProductos == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredProductos.setPredicate(it -> true); return; }
        final String selectedField = (field == null) ? "Todos" : field;
        filteredProductos.setPredicate(it -> {
            String id = it.idProducto != null ? String.valueOf(it.idProducto).toLowerCase() : "";
            String desc = safeLower(it.descripcion);
            String costo = it.costo != null ? it.costo.toPlainString().toLowerCase() : "";
            String precio = it.precioVenta != null ? it.precioVenta.toPlainString().toLowerCase() : "";
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Descripción": return desc.contains(q);
                case "Costo": return costo.contains(q);
                case "Precio": return precio.contains(q);
                default: return id.contains(q) || desc.contains(q) || costo.contains(q) || precio.contains(q);
            }
        });
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

    @FXML
    private void onList() {
        loadProductos();
    }

    @FXML
    private void onSave() {
        String desc = descripcionField.getText();
        if (desc == null || desc.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campo requerido", "La descripción es requerida.");
            return;
        }
        BigDecimal costo = parseBigDecimal(costoField.getText(), "costo");
        if (costo == null) return;
        BigDecimal precio = parseBigDecimal(precioVentaField.getText(), "precio de venta");
        if (precio == null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Producto p = new Producto();
            p.descripcion = desc.trim();
            p.costo = costo;
            p.precioVenta = precio;
            em.persist(p);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Producto guardado con ID: " + p.idProducto);
            loadProductos();
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
        Producto seleccionado = productosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un producto", "Debe seleccionar un producto para actualizarlo.");
            return;
        }

        String desc = descripcionField.getText();
        if (desc == null || desc.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campo requerido", "La descripción es requerida.");
            return;
        }
        BigDecimal costo = parseBigDecimal(costoField.getText(), "costo");
        if (costo == null) return;
        BigDecimal precio = parseBigDecimal(precioVentaField.getText(), "precio de venta");
        if (precio == null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Producto persistido = em.find(Producto.class, seleccionado.idProducto);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El producto ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadProductos();
                return;
            }
            persistido.descripcion = desc.trim();
            persistido.costo = costo;
            persistido.precioVenta = precio;
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Producto actualizado correctamente.");
            loadProductos();
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
        Producto seleccionado = productosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un producto", "Debe seleccionar un producto para eliminarlo.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Producto persistido = em.find(Producto.class, seleccionado.idProducto);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El producto ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadProductos();
                return;
            }
            em.remove(persistido);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Producto eliminado correctamente.");
            loadProductos();
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
        descripcionField.clear();
        costoField.clear();
        precioVentaField.clear();
        productosTable.getSelectionModel().clearSelection();
        descripcionField.requestFocus();
    }

    private BigDecimal parseBigDecimal(String s, String label) {
        try {
            if (s == null || s.isBlank()) {
                alert(Alert.AlertType.WARNING, "Campo requerido", "El " + label + " es requerido.");
                return null;
            }
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese un " + label + " válido (por ejemplo 12.50).");
            return null;
        }
    }

    private void loadProductos() {
        EntityManager em = JPAUtil.em();
        try {
            List<Producto> resultado = em.createQuery("SELECT p FROM Producto p ORDER BY p.idProducto", Producto.class)
                    .getResultList();
            productos.setAll(resultado);
            applyFilter();
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