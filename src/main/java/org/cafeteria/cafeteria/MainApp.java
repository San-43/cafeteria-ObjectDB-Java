package org.cafeteria.cafeteria;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.cafeteria.cafeteria.config.DbBootstrap;
import org.cafeteria.cafeteria.config.JPAUtil;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        try {
            DbBootstrap.init();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al iniciar la base de datos:\n" + ex.getMessage(), ButtonType.CLOSE);
            alert.setHeaderText("No se pudo inicializar la BD");
            alert.showAndWait();
            ex.printStackTrace();
            Platform.exit();
            return;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
            stage.setTitle("Cafetería — Administrador");
            stage.setScene(new Scene(root, 1100, 700));
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al cargar la interfaz:\n" + ex.getMessage(), ButtonType.CLOSE);
            alert.setHeaderText("Fallo al cargar FXML principal");
            alert.showAndWait();
            ex.printStackTrace();
            try { JPAUtil.close(); } catch (Exception ignored) {}
            Platform.exit();
        }
    }
    public static void main(String[] args) { launch(args); }
}
