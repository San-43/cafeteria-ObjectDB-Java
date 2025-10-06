package org.cafeteria.cafeteria;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.cafeteria.cafeteria.config.DbBootstrap;
import org.cafeteria.cafeteria.config.JPAUtil;

import java.nio.file.Files;
import java.nio.file.Path;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        Label status = new Label("Inicializando base de datos…");
        status.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label detail = new Label();
        Button cerrar = new Button("Cerrar");
        cerrar.setOnAction(e -> { try { JPAUtil.close(); } catch (Exception ignored) {} Platform.exit(); });

        VBox root = new VBox(12, status, detail, cerrar);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        stage.setScene(new Scene(root, 460, 180));
        stage.setTitle("Cafetería — Estado de la Base de Datos");
        stage.show();

        try {
            Path dataDir = Path.of("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
        } catch (Exception ex) {
            status.setText("❌ Error al preparar carpeta de datos");
            detail.setText(ex.getMessage());
            ex.printStackTrace();
            return;
        }

        try {
            DbBootstrap.init();
            status.setText("✅ Base de datos iniciada correctamente");
            detail.setText("Archivo: ./data/cafeteria.odb");
        } catch (Exception ex) {
            status.setText("❌ Error al iniciar la base de datos");
            detail.setText(ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "" : ex.getMessage()));
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) { launch(args); }
}
