package org.cafeteria.cafeteria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import org.cafeteria.cafeteria.config.JPAUtil;

import java.io.IOException;

public class MainController {
    @FXML private StackPane contentHolder;

    private void loadCenter(String fxml) {
        try {
            Node view = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentHolder.getChildren().setAll(view);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error al cargar vista");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    // Botones del menú lateral
    @FXML private void openTienda() { loadCenter("TiendaForm.fxml"); }
    @FXML private void openProducto() { loadCenter("ProductoForm.fxml"); }
    @FXML private void openIngrediente() { loadCenter("IngredienteForm.fxml"); }
    @FXML private void openReceta() { loadCenter("RecetaForm.fxml"); }
    @FXML private void openPaso() { loadCenter("PasoForm.fxml"); }
    @FXML private void openInventario() { loadCenter("InventarioForm.fxml"); }
    @FXML private void openInventarioIngredientes() { loadCenter("InventarioIngredientesForm.fxml"); }
    @FXML private void openVenta() { loadCenter("VentaForm.fxml"); }
    @FXML private void openProductoVendido() { loadCenter("ProductoVendidoForm.fxml"); }
    @FXML private void openProporcionIngrediente() { loadCenter("ProporcionIngredienteForm.fxml"); }

    @FXML private void exitApp() {
        try { JPAUtil.close(); } catch (Exception ignored) {}
        javafx.application.Platform.exit();
    }

}
