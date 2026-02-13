module org.postmanman {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;


    opens org.postmanman to javafx.fxml;
    exports org.postmanman;
}