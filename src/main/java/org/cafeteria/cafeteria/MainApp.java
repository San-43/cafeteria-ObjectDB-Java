package org.cafeteria.cafeteria;

import org.cafeteria.cafeteria.config.DbBootstrap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

public final class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::showStatusWindow);
    }

    private static void showStatusWindow() {
        String title = "Cafetería - Estado";
        try {
            Path databasePath = DbBootstrap.init();
            String message = "✅ Base de datos inicializada correctamente." +
                    "\nArchivo: " + databasePath.toAbsolutePath();
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            String message = "❌ Error al iniciar la aplicación." +
                    "\n" + ex.getClass().getSimpleName() + ": " + ex.getMessage();
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    private MainApp() {
    }
}
