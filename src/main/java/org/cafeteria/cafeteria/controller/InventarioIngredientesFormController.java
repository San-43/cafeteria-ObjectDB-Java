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
import org.cafeteria.cafeteria.model.InventarioIngredientes;
import org.cafeteria.cafeteria.model.Tienda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InventarioIngredientesFormController {
    @FXML private ComboBox<Tienda> tiendaCombo;
    @FXML private ComboBox<Ingrediente> ingredienteCombo;
    @FXML private DatePicker fechaCompraPicker;
    @FXML private DatePicker fechaCaducidadPicker;
    @FXML private TextField costoCompraField;
    @FXML private TextField precioPorcionField;
    @FXML private TableView<InventarioIngredientes> inventarioTable;
    @FXML private TableColumn<InventarioIngredientes, Long> idColumn;
    @FXML private TableColumn<InventarioIngredientes, String> tiendaColumn;
    @FXML private TableColumn<InventarioIngredientes, String> ingredienteColumn;
    @FXML private TableColumn<InventarioIngredientes, LocalDate> fechaCompraColumn;
    @FXML private TableColumn<InventarioIngredientes, LocalDate> fechaCaducidadColumn;
    @FXML private TableColumn<InventarioIngredientes, BigDecimal> costoColumn;
    @FXML private TableColumn<InventarioIngredientes, BigDecimal> precioColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<InventarioIngredientes> inventarioIngredientes = FXCollections.observableArrayList();
    private FilteredList<InventarioIngredientes> filteredInventario;

    @FXML private void initialize() {
        loadTiendas();
        loadIngredientes();

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idInventarioIngredientes));
        tiendaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().tienda != null ? cell.getValue().tienda.direccion : null));
        ingredienteColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().ingrediente != null ? cell.getValue().ingrediente.nombre : null));
        fechaCompraColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().fechaCompra));
        fechaCaducidadColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().fechaCaducidad));
        costoColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().costoCompra));
        precioColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().precioVentaPorcion));

        // Tabla con filtrado/ordenado
        filteredInventario = new FilteredList<>(inventarioIngredientes, it -> true);
        SortedList<InventarioIngredientes> sorted = new SortedList<>(filteredInventario);
        sorted.comparatorProperty().bind(inventarioTable.comparatorProperty());
        inventarioTable.setItems(sorted);

        inventarioTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectTienda(selected.tienda != null ? selected.tienda.idTienda : null);
                selectIngrediente(selected.ingrediente != null ? selected.ingrediente.idIngrediente : null);
                fechaCompraPicker.setValue(selected.fechaCompra);
                fechaCaducidadPicker.setValue(selected.fechaCaducidad);
                costoCompraField.setText(selected.costoCompra != null ? selected.costoCompra.toPlainString() : "");
                precioPorcionField.setText(selected.precioVentaPorcion != null ? selected.precioVentaPorcion.toPlainString() : "");
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList(
                    "Todos", "ID", "Tienda", "Ingrediente", "Fecha compra", "Fecha caducidad", "Costo", "Precio"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }
    }

    private void applyFilter() {
        if (filteredInventario == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredInventario.setPredicate(it -> true); return; }
        final String selectedField = (field == null) ? "Todos" : field;
        filteredInventario.setPredicate(it -> {
            String id = it.idInventarioIngredientes != null ? String.valueOf(it.idInventarioIngredientes).toLowerCase() : "";
            String tienda = it.tienda != null ? safeLower(it.tienda.direccion) : "";
            String ing = it.ingrediente != null ? safeLower(it.ingrediente.nombre) : "";
            String fcompra = it.fechaCompra != null ? safeLower(it.fechaCompra.toString()) : "";
            String fcad = it.fechaCaducidad != null ? safeLower(it.fechaCaducidad.toString()) : "";
            String costo = it.costoCompra != null ? it.costoCompra.toPlainString().toLowerCase() : "";
            String precio = it.precioVentaPorcion != null ? it.precioVentaPorcion.toPlainString().toLowerCase() : "";
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Tienda": return tienda.contains(q);
                case "Ingrediente": return ing.contains(q);
                case "Fecha compra": return fcompra.contains(q);
                case "Fecha caducidad": return fcad.contains(q);
                case "Costo": return costo.contains(q);
                case "Precio": return precio.contains(q);
                default: return id.contains(q)||tienda.contains(q)||ing.contains(q)||fcompra.contains(q)||fcad.contains(q)||costo.contains(q)||precio.contains(q);
            }
        });
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
        loadInventarioIngredientes();
    }

    @FXML private void onSave() {
        Tienda t = tiendaCombo.getValue();
        Ingrediente ing = ingredienteCombo.getValue();
        LocalDate compra = fechaCompraPicker.getValue();
        LocalDate cad = fechaCaducidadPicker.getValue();
        BigDecimal costo = parseBigDecimal(costoCompraField.getText(), "costo de compra");
        BigDecimal precio = parseBigDecimal(precioPorcionField.getText(), "precio por porción");
        if (t==null || ing==null || compra==null || cad==null || costo==null || precio==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            InventarioIngredientes ii = new InventarioIngredientes();
            ii.tienda = em.find(Tienda.class, t.idTienda);
            ii.ingrediente = em.find(Ingrediente.class, ing.idIngrediente);
            ii.fechaCompra = compra;
            ii.fechaCaducidad = cad;
            ii.costoCompra = costo;
            ii.precioVentaPorcion = precio;
            em.persist(ii);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Inventario de ingrediente guardado con ID: " + ii.idInventarioIngredientes);
            loadInventarioIngredientes();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onUpdate() {
        InventarioIngredientes seleccionado = inventarioTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un registro", "Debe seleccionar un registro para actualizarlo.");
            return;
        }

        Tienda t = tiendaCombo.getValue();
        Ingrediente ing = ingredienteCombo.getValue();
        LocalDate compra = fechaCompraPicker.getValue();
        LocalDate cad = fechaCaducidadPicker.getValue();
        BigDecimal costo = parseBigDecimal(costoCompraField.getText(), "costo de compra");
        BigDecimal precio = parseBigDecimal(precioPorcionField.getText(), "precio por porción");
        if (t==null || ing==null || compra==null || cad==null || costo==null || precio==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            InventarioIngredientes persistido = em.find(InventarioIngredientes.class, seleccionado.idInventarioIngredientes);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El registro ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadInventarioIngredientes();
                return;
            }
            persistido.tienda = em.find(Tienda.class, t.idTienda);
            persistido.ingrediente = em.find(Ingrediente.class, ing.idIngrediente);
            persistido.fechaCompra = compra;
            persistido.fechaCaducidad = cad;
            persistido.costoCompra = costo;
            persistido.precioVentaPorcion = precio;
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Registro actualizado correctamente.");
            loadInventarioIngredientes();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onDelete() {
        InventarioIngredientes seleccionado = inventarioTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un registro", "Debe seleccionar un registro para eliminarlo.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            InventarioIngredientes persistido = em.find(InventarioIngredientes.class, seleccionado.idInventarioIngredientes);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El registro ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadInventarioIngredientes();
                return;
            }
            em.remove(persistido);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Registro eliminado correctamente.");
            loadInventarioIngredientes();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al eliminar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        tiendaCombo.getSelectionModel().clearSelection();
        ingredienteCombo.getSelectionModel().clearSelection();
        fechaCompraPicker.setValue(null);
        fechaCaducidadPicker.setValue(null);
        costoCompraField.clear();
        precioPorcionField.clear();
        inventarioTable.getSelectionModel().clearSelection();
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

    private void loadInventarioIngredientes() {
        EntityManager em = JPAUtil.em();
        try {
            List<InventarioIngredientes> lista = em.createQuery(
                            "select i from InventarioIngredientes i order by i.idInventarioIngredientes",
                            InventarioIngredientes.class)
                    .getResultList();
            inventarioIngredientes.setAll(lista);
            applyFilter();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }

    private void selectTienda(Long id) {
        if (id == null) {
            tiendaCombo.getSelectionModel().clearSelection();
            return;
        }
        tiendaCombo.getItems().stream()
                .filter(t -> t.idTienda != null && t.idTienda.equals(id))
                .findFirst()
                .ifPresent(tiendaCombo.getSelectionModel()::select);
    }

    private void selectIngrediente(Long id) {
        if (id == null) {
            ingredienteCombo.getSelectionModel().clearSelection();
            return;
        }
        ingredienteCombo.getItems().stream()
                .filter(i -> i.idIngrediente != null && i.idIngrediente.equals(id))
                .findFirst()
                .ifPresent(ingredienteCombo.getSelectionModel()::select);
    }
}