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
import org.cafeteria.cafeteria.model.ProductoVendido;
import org.cafeteria.cafeteria.model.Venta;

import java.math.BigDecimal;
import java.util.List;

public class ProductoVendidoFormController {
    @FXML private ComboBox<Venta> ventaCombo;
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private TextField cantidadField;
    @FXML private TextField precioField;
    @FXML private TableView<ProductoVendido> vendidosTable;
    @FXML private TableColumn<ProductoVendido, Long> idColumn;
    @FXML private TableColumn<ProductoVendido, String> ventaColumn;
    @FXML private TableColumn<ProductoVendido, String> productoColumn;
    @FXML private TableColumn<ProductoVendido, Integer> cantidadColumn;
    @FXML private TableColumn<ProductoVendido, BigDecimal> precioColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<ProductoVendido> vendidos = FXCollections.observableArrayList();
    private FilteredList<ProductoVendido> filteredVendidos;

    @FXML private void initialize() {
        loadVentas();
        loadProductos();

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idProductoVendido));
        ventaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().venta != null ? "#" + cell.getValue().venta.idVenta + " — " + cell.getValue().venta.fecha : ""));
        productoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().producto != null ? cell.getValue().producto.descripcion : ""));
        cantidadColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().cantidad));
        precioColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().precio));

        // Filtrado/ordenado
        filteredVendidos = new FilteredList<>(vendidos, it -> true);
        SortedList<ProductoVendido> sorted = new SortedList<>(filteredVendidos);
        sorted.comparatorProperty().bind(vendidosTable.comparatorProperty());
        vendidosTable.setItems(sorted);

        vendidosTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectVenta(selected.venta != null ? selected.venta.idVenta : null);
                selectProducto(selected.producto != null ? selected.producto.idProducto : null);
                cantidadField.setText(selected.cantidad != null ? selected.cantidad.toString() : "");
                precioField.setText(selected.precio != null ? selected.precio.toPlainString() : "");
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Venta", "Producto", "Cantidad", "Precio"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }

        loadVendidos();
    }

    private void applyFilter() {
        if (filteredVendidos == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredVendidos.setPredicate(it -> true); return; }
        final String selectedField = (field == null) ? "Todos" : field;
        filteredVendidos.setPredicate(it -> {
            String id = it.idProductoVendido != null ? String.valueOf(it.idProductoVendido).toLowerCase() : "";
            String venta = it.venta != null ? safeLower("#"+it.venta.idVenta + " " + it.venta.fecha.toString()) : "";
            String prod = it.producto != null ? safeLower(it.producto.descripcion) : "";
            String cant = it.cantidad != null ? String.valueOf(it.cantidad).toLowerCase() : "";
            String precio = it.precio != null ? it.precio.toPlainString().toLowerCase() : "";
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Venta": return venta.contains(q);
                case "Producto": return prod.contains(q);
                case "Cantidad": return cant.contains(q);
                case "Precio": return precio.contains(q);
                default: return id.contains(q) || venta.contains(q) || prod.contains(q) || cant.contains(q) || precio.contains(q);
            }
        });
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

    private void loadVentas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Venta> ventas = em.createQuery("select v from Venta v order by v.fecha desc", Venta.class).getResultList();
            ventaCombo.setItems(FXCollections.observableArrayList(ventas));
            ventaCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Venta v) { return v==null?"": ("#"+v.idVenta + " — " + (v.tienda!=null? v.tienda.direccion:"") + " — " + v.fecha); }
                @Override public Venta fromString(String s) { return null; }
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

    @FXML private void onList() {
        loadVendidos();
    }

    @FXML private void onSave() {
        Venta v = ventaCombo.getValue();
        Producto p = productoCombo.getValue();
        Integer cant = parseInt(cantidadField.getText(), "cantidad");
        BigDecimal precio = parseBigDecimal(precioField.getText(), "precio");
        if (v==null || p==null || cant==null || precio==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            ProductoVendido pv = new ProductoVendido();
            pv.venta = em.find(Venta.class, v.idVenta);
            pv.producto = em.find(Producto.class, p.idProducto);
            pv.cantidad = cant;
            pv.precio = precio;
            em.persist(pv);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Producto vendido guardado con ID: " + pv.idProductoVendido);
            loadVendidos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onUpdate() {
        ProductoVendido seleccionado = vendidosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un registro", "Debe seleccionar un registro para actualizarlo.");
            return;
        }

        Venta v = ventaCombo.getValue();
        Producto p = productoCombo.getValue();
        Integer cant = parseInt(cantidadField.getText(), "cantidad");
        BigDecimal precio = parseBigDecimal(precioField.getText(), "precio");
        if (v==null || p==null || cant==null || precio==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            ProductoVendido persistido = em.find(ProductoVendido.class, seleccionado.idProductoVendido);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El registro ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadVendidos();
                return;
            }
            persistido.venta = em.find(Venta.class, v.idVenta);
            persistido.producto = em.find(Producto.class, p.idProducto);
            persistido.cantidad = cant;
            persistido.precio = precio;
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Registro actualizado correctamente.");
            loadVendidos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onDelete() {
        ProductoVendido seleccionado = vendidosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un registro", "Debe seleccionar un registro para eliminarlo.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            ProductoVendido persistido = em.find(ProductoVendido.class, seleccionado.idProductoVendido);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El registro ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadVendidos();
                return;
            }
            em.remove(persistido);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Registro eliminado correctamente.");
            loadVendidos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al eliminar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        ventaCombo.getSelectionModel().clearSelection();
        productoCombo.getSelectionModel().clearSelection();
        cantidadField.clear();
        precioField.clear();
        vendidosTable.getSelectionModel().clearSelection();
        ventaCombo.requestFocus();
    }

    private Integer parseInt(String s, String label) {
        try {
            if (s==null||s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "La "+label+" es requerida."); return null; }
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese una "+label+" válida (por ejemplo 3).");
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String s, String label) {
        try {
            if (s==null||s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "El "+label+" es requerido."); return null; }
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese un "+label+" válido (por ejemplo 12.50).");
            return null;
        }
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void loadVendidos() {
        EntityManager em = JPAUtil.em();
        try {
            List<ProductoVendido> lista = em.createQuery(
                            "select pv from ProductoVendido pv order by pv.idProductoVendido",
                            ProductoVendido.class)
                    .getResultList();
            vendidos.setAll(lista);
            applyFilter();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    private void selectVenta(Long id) {
        if (id == null) {
            ventaCombo.getSelectionModel().clearSelection();
            return;
        }
        ventaCombo.getItems().stream()
                .filter(v -> v.idVenta != null && v.idVenta.equals(id))
                .findFirst()
                .ifPresent(ventaCombo.getSelectionModel()::select);
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

