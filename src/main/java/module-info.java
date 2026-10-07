module com.example.sklady {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.sklady.gui to javafx.fxml, javafx.graphics;

    exports com.example.sklady.gui;
    exports com.example.sklady.managers;
    exports com.example.sklady.models;
    exports com.example.sklady.datastructures;
    exports com.example.sklady.enums;
}