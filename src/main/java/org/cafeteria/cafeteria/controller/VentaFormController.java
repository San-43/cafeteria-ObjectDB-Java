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
import org.cafeteria.cafeteria.model.Tienda;
import org.cafeteria.cafeteria.model.Producto;
import org.cafeteria.cafeteria.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class VentaFormController {
    @FXML private ComboBox<Tienda> tiendaCombo;
    @FXML private ComboBox<Producto> productoCombo;
    @FXML private DatePicker fechaPicker;
    @FXML private TextField totalField;
    @FXML private TableView<Venta> ventasTable;
    @FXML private TableColumn<Venta, String> idColumn;
    @FXML private TableColumn<Venta, String> tiendaColumn;
    @FXML private TableColumn<Venta, String> productoColumn;
    @FXML private TableColumn<Venta, LocalDate> fechaColumn;
    @FXML private TableColumn<Venta, BigDecimal> totalColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<Venta> ventas = FXCollections.observableArrayList();
    private FilteredList<Venta> filteredVentas;

    @FXML private void initialize() {
        loadTiendas();
        loadProductos();
        fechaPicker.setValue(LocalDate.now());

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idVenta));
        tiendaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().tienda != null ? cell.getValue().tienda.direccion : ""));
        productoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().producto != null ? buildProductoLabel(cell.getValue().producto) : ""));
        fechaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().fecha));
        totalColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().total));

        // Filtrado/ordenado
        filteredVentas = new FilteredList<>(ventas, it -> true);
        SortedList<Venta> sorted = new SortedList<>(filteredVentas);
        sorted.comparatorProperty().bind(ventasTable.comparatorProperty());
        ventasTable.setItems(sorted);

        ventasTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectTienda(selected.tienda != null ? selected.tienda.idTienda : null);
                selectProducto(selected.producto != null ? selected.producto.idProducto : null);
                fechaPicker.setValue(selected.fecha);
                totalField.setText(selected.total != null ? selected.total.toPlainString() : "");
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Tienda", "Producto", "Fecha", "Total"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }
    }

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

    private void applyFilter() {
        if (filteredVentas == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredVentas.setPredicate(it -> true); return; }
        final String selectedField = (field == null) ? "Todos" : field;
        filteredVentas.setPredicate(it -> {
            String id = it.idVenta != null ? it.idVenta.toLowerCase() : "";
            String tienda = it.tienda != null ? safeLower(it.tienda.direccion) : "";
            String producto = it.producto != null ? safeLower(buildProductoLabel(it.producto)) : "";
            String fecha = it.fecha != null ? safeLower(it.fecha.toString()) : "";
            String total = it.total != null ? it.total.toPlainString().toLowerCase() : "";
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Tienda": return tienda.contains(q);
                case "Producto": return producto.contains(q);
                case "Fecha": return fecha.contains(q);
                case "Total": return total.contains(q);
                default: return id.contains(q) || tienda.contains(q) || producto.contains(q) || fecha.contains(q) || total.contains(q);
            }
        });
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

    @FXML private void onList() {
        loadVentas();
    }

    @FXML private void onSave() {
        Tienda t = tiendaCombo.getValue();
        Producto producto = productoCombo.getValue();
        LocalDate fecha = fechaPicker.getValue();
        BigDecimal total = parseBigDecimal(totalField.getText(), "total");
        if (t==null || producto==null || fecha==null || total==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Venta v = new Venta();
            v.tienda = em.find(Tienda.class, t.idTienda);
            v.producto = em.find(Producto.class, producto.idProducto);
            v.fecha = fecha;
            v.total = total;
            em.persist(v);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Venta guardada con ID: " + v.idVenta);
            loadVentas();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onUpdate() {
        Venta seleccionada = ventasTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alert(Alert.AlertType.WARNING, "Seleccione una venta", "Debe seleccionar una venta para actualizarla.");
            return;
        }

        Tienda t = tiendaCombo.getValue();
        Producto producto = productoCombo.getValue();
        LocalDate fecha = fechaPicker.getValue();
        BigDecimal total = parseBigDecimal(totalField.getText(), "total");
        if (t==null || producto==null || fecha==null || total==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Venta persistida = em.find(Venta.class, seleccionada.idVenta);
            if (persistida == null) {
                alert(Alert.AlertType.ERROR, "No encontrada", "La venta ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadVentas();
                return;
            }
            persistida.tienda = em.find(Tienda.class, t.idTienda);
            persistida.producto = em.find(Producto.class, producto.idProducto);
            persistida.fecha = fecha;
            persistida.total = total;
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Venta actualizada correctamente.");
            loadVentas();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onDelete() {
        Venta seleccionada = ventasTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alert(Alert.AlertType.WARNING, "Seleccione una venta", "Debe seleccionar una venta para eliminarla.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Venta persistida = em.find(Venta.class, seleccionada.idVenta);
            if (persistida == null) {
                alert(Alert.AlertType.ERROR, "No encontrada", "La venta ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadVentas();
                return;
            }
            em.remove(persistida);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Venta eliminada correctamente.");
            loadVentas();
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
        fechaPicker.setValue(LocalDate.now());
        totalField.clear();
        ventasTable.getSelectionModel().clearSelection();
        tiendaCombo.requestFocus();
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

    private void loadVentas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Venta> lista = em.createQuery("select v from Venta v order by v.fecha", Venta.class)
                    .getResultList();
            ventas.setAll(lista);
            applyFilter();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    private void selectTienda(String id) {
        if (id == null) {
            tiendaCombo.getSelectionModel().clearSelection();
            return;
        }
        tiendaCombo.getItems().stream()
                .filter(t -> t.idTienda != null && t.idTienda.equals(id))
                .findFirst()
                .ifPresent(tiendaCombo.getSelectionModel()::select);
    }

    private void selectProducto(String id) {
        if (id == null) {
            productoCombo.getSelectionModel().clearSelection();
            return;
        }
        productoCombo.getItems().stream()
                .filter(p -> p.idProducto != null && p.idProducto.equals(id))
                .findFirst()
                .ifPresent(productoCombo.getSelectionModel()::select);
    }

    private void loadProductos() {
        EntityManager em = JPAUtil.em();
        try {
            List<Producto> productos = em.createQuery("select p from Producto p order by p.nombre", Producto.class).getResultList();
            productoCombo.setItems(FXCollections.observableArrayList(productos));
            productoCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Producto p) { return p==null?"": buildProductoLabel(p); }
                @Override public Producto fromString(String s) { return null; }
            });
        } finally { em.close(); }
    }

    private String buildProductoLabel(Producto producto) {
        if (producto == null) return "";
        String nombre = producto.nombre != null ? producto.nombre : "";
        String descripcion = producto.descripcion != null ? producto.descripcion : "";
        if (!nombre.isBlank() && !descripcion.isBlank()) {
            return nombre + " — " + descripcion;
        }
        return !nombre.isBlank() ? nombre : descripcion;
    }
}
