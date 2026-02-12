module org.postmanman {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.postmanman to javafx.fxml;
    exports org.postmanman;
}