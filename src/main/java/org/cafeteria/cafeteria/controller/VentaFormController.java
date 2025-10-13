package org.cafeteria.cafeteria.controller;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.cafeteria.cafeteria.config.JPAUtil;
import org.cafeteria.cafeteria.model.Tienda;
import org.cafeteria.cafeteria.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentaFormController {
    @FXML private ComboBox<Tienda> tiendaCombo;
    @FXML private DatePicker fechaPicker;
    @FXML private TextField horaField;
    @FXML private TextField totalField;

    @FXML private void initialize() { loadTiendas(); fechaPicker.setValue(LocalDate.now()); horaField.setText(LocalTime.now().withSecond(0).withNano(0).toString()); }

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

    @FXML private void onSave() {
        Tienda t = tiendaCombo.getValue();
        LocalDate fecha = fechaPicker.getValue();
        LocalTime hora = parseHora(horaField.getText());
        BigDecimal total = parseBigDecimal(totalField.getText(), "total");
        if (t==null || fecha==null || hora==null || total==null) return;

        EntityManager em = JPAUtil.em();
        try {
            em.getTransaction().begin();
            Venta v = new Venta();
            v.tienda = em.find(Tienda.class, t.idTienda);
            v.fecha = LocalDateTime.of(fecha, hora);
            v.total = total;
            em.persist(v);
            em.getTransaction().commit();
            alert(Alert.AlertType.INFORMATION, "Guardado", "Venta guardada con ID: " + v.idVenta);
            onClear();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            alert(Alert.AlertType.ERROR, "Error al guardar", ex.getMessage());
            ex.printStackTrace();
        } finally { em.close(); }
    }

    @FXML private void onClear() {
        tiendaCombo.getSelectionModel().clearSelection();
        fechaPicker.setValue(LocalDate.now());
        horaField.setText(LocalTime.now().withSecond(0).withNano(0).toString());
        totalField.clear();
        tiendaCombo.requestFocus();
    }

    private LocalTime parseHora(String s) {
        try {
            if (s==null || s.isBlank()) { alert(Alert.AlertType.WARNING, "Campo requerido", "La hora es requerida (formato HH:mm). "); return null; }
            return LocalTime.parse(s.trim(), DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception e) {
            try {
                return LocalTime.parse(s.trim());
            } catch (Exception ex) {
                alert(Alert.AlertType.WARNING, "Formato inválido", "Ingrese una hora válida (por ejemplo 14:30).");
                return null;
            }
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
}

