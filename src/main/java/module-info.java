module org.cafeteria.cafeteria {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jakarta.persistence;

    opens org.cafeteria.cafeteria to javafx.fxml;
    opens org.cafeteria.cafeteria.model; // JPA/ObjectDB por reflexión

    exports org.cafeteria.cafeteria;
}
