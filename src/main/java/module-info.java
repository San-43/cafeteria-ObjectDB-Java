module org.cafeteria.cafeteria {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jakarta.persistence;

    opens org.cafeteria.cafeteria to javafx.fxml;
    opens org.cafeteria.cafeteria.model; // JPA/ObjectDB por reflexión
    opens org.cafeteria.cafeteria.controller to javafx.fxml;

    exports org.cafeteria.cafeteria;
}
