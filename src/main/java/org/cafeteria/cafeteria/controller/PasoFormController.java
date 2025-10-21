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
import org.cafeteria.cafeteria.model.Paso;
import org.cafeteria.cafeteria.model.PasoDetalle;
import org.cafeteria.cafeteria.model.Receta;

import java.util.List;
import java.util.stream.Collectors;

public class PasoFormController {
    @FXML private ComboBox<Receta> recetaCombo;
    @FXML private TextField numeroPasoField;
    @FXML private TextArea detalleArea;
    @FXML private ListView<String> detallesList;
    @FXML private TableView<Paso> pasosTable;
    @FXML private TableColumn<Paso, String> idColumn;
    @FXML private TableColumn<Paso, String> recetaColumn;
    @FXML private TableColumn<Paso, Number> numeroColumn;
    @FXML private TableColumn<Paso, String> detallesColumn;
    // Buscador
    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;

    private final ObservableList<Paso> pasos = FXCollections.observableArrayList();
    private FilteredList<Paso> filteredPasos;
    private final ObservableList<String> detalles = FXCollections.observableArrayList();

    @FXML private void initialize() {
        loadRecetas();

        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().idPaso));
        recetaColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().receta != null && cell.getValue().receta.producto != null
                        ? cell.getValue().receta.producto.descripcion + " (" + cell.getValue().receta.tamano + ")"
                        : ""));
        numeroColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().numeroPaso));
        detallesColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().detalles == null
                ? ""
                : cell.getValue().detalles.stream()
                        .map(d -> d.pasoDetalle)
                        .collect(Collectors.joining(" | "))));

        if (detallesList != null) {
            detallesList.setItems(detalles);
        }

        // Filtrado/ordenado
        filteredPasos = new FilteredList<>(pasos, it -> true);
        SortedList<Paso> sorted = new SortedList<>(filteredPasos);
        sorted.comparatorProperty().bind(pasosTable.comparatorProperty());
        pasosTable.setItems(sorted);

        pasosTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectReceta(selected.receta != null ? selected.receta.idReceta : null);
                numeroPasoField.setText(selected.numeroPaso != null ? selected.numeroPaso.toString() : "");
                detalles.setAll(selected.detalles == null ? List.of() : selected.detalles.stream()
                        .map(d -> d.pasoDetalle)
                        .collect(Collectors.toList()));
                detalleArea.clear();
            }
        });

        // Buscador
        if (searchFieldCombo != null) {
            searchFieldCombo.setItems(FXCollections.observableArrayList("Todos", "ID", "Receta", "Número", "Detalle"));
            searchFieldCombo.getSelectionModel().selectFirst();
            searchFieldCombo.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> applyFilter());
        }
        if (searchTextField != null) {
            searchTextField.textProperty().addListener((o,a,b) -> applyFilter());
        }
    }

    private void applyFilter() {
        if (filteredPasos == null) return;
        String q = searchTextField != null ? safeLower(searchTextField.getText()) : "";
        String field = searchFieldCombo != null ? searchFieldCombo.getSelectionModel().getSelectedItem() : "Todos";
        if (q.isBlank()) { filteredPasos.setPredicate(it -> true); return; }
        final String selectedField = (field == null) ? "Todos" : field;
        filteredPasos.setPredicate(it -> {
            String id = it.idPaso != null ? it.idPaso.toLowerCase() : "";
            String receta = safeLower(it.receta != null ? buildRecetaLabel(it.receta) : "");
            String numero = it.numeroPaso != null ? String.valueOf(it.numeroPaso).toLowerCase() : "";
            String detalle = safeLower(it.detalles == null ? "" : it.detalles.stream()
                    .map(d -> d.pasoDetalle == null ? "" : d.pasoDetalle)
                    .collect(Collectors.joining(" ")));
            switch (selectedField) {
                case "ID": return id.contains(q);
                case "Receta": return receta.contains(q);
                case "Número": return numero.contains(q);
                case "Detalle": return detalle.contains(q);
                default: return id.contains(q) || receta.contains(q) || numero.contains(q) || detalle.contains(q);
            }
        });
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase().trim(); }

    private void loadRecetas() {
        EntityManager em = JPAUtil.em();
        try {
            List<Receta> recetas = em.createQuery("select r from Receta r order by r.nombre", Receta.class).getResultList();
            recetaCombo.setItems(FXCollections.observableArrayList(recetas));
            recetaCombo.setConverter(new StringConverter<>() {
                @Override public String toString(Receta r) { return r == null ? "" : buildRecetaLabel(r); }
                @Override public Receta fromString(String s) { return null; }
            });
            recetaCombo.setCellFactory(cb -> new ListCell<>() {
                @Override protected void updateItem(Receta item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?"": buildRecetaLabel(item)); }
            });
        } finally { em.close(); }
    }

    @FXML private void onList() {
        loadPasos();
    }

    @FXML private void onSave() {
        Receta receta = recetaCombo.getValue();
        if (receta == null) { alert(Alert.AlertType.WARNING, "Campo requerido", "Selecciona una receta."); return; }
        Long numeroPaso = parseNumero(numeroPasoField.getText());
        if (numeroPaso == null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Paso p = new Paso();
            p.receta = em.find(Receta.class, receta.idReceta);
            p.numeroPaso = numeroPaso;
            if (!detalles.isEmpty()) {
                for (String detalle : detalles) {
                    PasoDetalle pd = new PasoDetalle();
                    pd.paso = p;
                    pd.pasoDetalle = detalle;
                    p.detalles.add(pd);
                }
            }
            em.persist(p);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Paso guardado con ID: " + p.idPaso);
            loadPasos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onUpdate() {
        Paso seleccionado = pasosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un paso", "Debe seleccionar un paso para actualizarlo.");
            return;
        }

        Receta receta = recetaCombo.getValue();
        if (receta == null) { alert(Alert.AlertType.WARNING, "Campo requerido", "Selecciona una receta."); return; }
        Long numeroPaso = parseNumero(numeroPasoField.getText());
        if (numeroPaso == null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Paso persistido = em.find(Paso.class, seleccionado.idPaso);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El paso ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadPasos();
                return;
            }
            persistido.receta = em.find(Receta.class, receta.idReceta);
            persistido.numeroPaso = numeroPaso;
            persistido.detalles.clear();
            if (!detalles.isEmpty()) {
                for (String detalle : detalles) {
                    PasoDetalle pd = new PasoDetalle();
                    pd.paso = persistido;
                    pd.pasoDetalle = detalle;
                    persistido.detalles.add(pd);
                }
            }
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Actualizado", "Paso actualizado correctamente.");
            loadPasos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al actualizar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onDelete() {
        Paso seleccionado = pasosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un paso", "Debe seleccionar un paso para eliminarlo.");
            return;
        }

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Paso persistido = em.find(Paso.class, seleccionado.idPaso);
            if (persistido == null) {
                alert(Alert.AlertType.ERROR, "No encontrado", "El paso ya no existe en la base de datos.");
                em.getTransaction().rollback();
                loadPasos();
                return;
            }
            em.remove(persistido);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Eliminado", "Paso eliminado correctamente.");
            loadPasos();
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al eliminar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        recetaCombo.getSelectionModel().clearSelection();
        numeroPasoField.clear();
        detalleArea.clear();
        detalles.clear();
        pasosTable.getSelectionModel().clearSelection();
        recetaCombo.requestFocus();
    }

    private Long parseNumero(String value) {
        try {
            if (value == null || value.isBlank()) {
                alert(Alert.AlertType.WARNING, "Campo requerido", "El número de paso es requerido.");
                return null;
            }
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            alert(Alert.AlertType.WARNING, "Formato inválido", "Ingresa un número de paso válido.");
            return null;
        }
    }

    private void alert(Alert.AlertType type, String header, String content) {
        var a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void loadPasos() {
        EntityManager em = JPAUtil.em();
        try {
            List<Paso> lista = em.createQuery("select distinct p from Paso p left join fetch p.detalles order by p.numeroPaso", Paso.class).getResultList();
            pasos.setAll(lista);
            applyFilter();
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Error al cargar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    private void selectReceta(String id) {
        if (id == null) {
            recetaCombo.getSelectionModel().clearSelection();
            return;
        }
        recetaCombo.getItems().stream()
                .filter(r -> r.idReceta != null && r.idReceta.equals(id))
                .findFirst()
                .ifPresent(recetaCombo.getSelectionModel()::select);
    }

    @FXML private void onAddDetalle() {
        if (detalleArea == null) return;
        String detalle = detalleArea.getText();
        if (detalle == null || detalle.isBlank()) {
            alert(Alert.AlertType.WARNING, "Campo requerido", "Captura el detalle del paso antes de agregarlo.");
            return;
        }
        detalles.add(detalle.trim());
        detalleArea.clear();
    }

    @FXML private void onRemoveDetalle() {
        if (detallesList == null) return;
        String seleccionado = detallesList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            alert(Alert.AlertType.WARNING, "Seleccione un detalle", "Selecciona un detalle para eliminar.");
            return;
        }
        detalles.remove(seleccionado);
    }

    private String buildRecetaLabel(Receta receta) {
        if (receta == null) return "";
        String nombre = receta.nombre != null ? receta.nombre : "";
        String producto = receta.producto != null ?
                (receta.producto.nombre != null && !receta.producto.nombre.isBlank()
                        ? receta.producto.nombre
                        : receta.producto.descripcion != null ? receta.producto.descripcion : "") : "";
        StringBuilder sb = new StringBuilder();
        if (!nombre.isBlank()) sb.append(nombre);
        if (!producto.isBlank()) {
            if (sb.length() > 0) sb.append(" — ");
            sb.append(producto);
        }
        if (receta.tamano != null && !receta.tamano.isBlank()) {
            if (sb.length() > 0) sb.append(" (" + receta.tamano + ")");
            else sb.append(receta.tamano);
        }
        if (sb.length() == 0 && receta.idReceta != null) sb.append(receta.idReceta);
        return sb.toString();
    }
}
