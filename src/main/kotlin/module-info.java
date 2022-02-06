module com.example.imageeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires javafx.swing;
    requires opencv;
    requires com.google.gson;


    opens com.example.imageeditor to javafx.fxml;
    exports com.example.imageeditor;
}