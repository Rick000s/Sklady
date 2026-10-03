module com.example.sklady {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.sklady to javafx.fxml;
    exports com.example.sklady;
}