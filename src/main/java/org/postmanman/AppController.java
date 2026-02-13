package org.postmanman;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AppController {

    @FXML private ListView<String> historyList;
    @FXML private ComboBox<String> methodCombo;
    @FXML private TextField urlField;

    @FXML private TextArea requestBodyArea;
    @FXML private TextArea requestHeadersArea;

    @FXML private TextArea responseBodyArea;
    @FXML private TextArea responseHeadersArea;

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @FXML
    public void initialize() {
        methodCombo.getItems().addAll("GET", "POST", "PUT", "DELETE");
        methodCombo.setValue("GET");

        historyList.setPlaceholder(new Label("No history yet"));
    }

    @FXML
    protected void onSendRequest() {
        String url = urlField.getText();

        if (url == null || !url.startsWith("http")) {
            responseBodyArea.setText("Error: URL must start with http:// or https://");
            return;
        }

        responseBodyArea.setText("Requesting data from: " + url + "...");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(rep -> {
                    String body = rep.body();
                    String formattedResult;

                    try {
                        JsonElement je = JsonParser.parseString(body);
                        formattedResult = gson.toJson(je);
                    }

                    catch (Exception e) {
                        formattedResult = body;
                    }

                    final String finalOutput = formattedResult;

                    Platform.runLater(() -> {
                        responseBodyArea.setText(finalOutput);
                        historyList.getItems().add(0, "GET - " + url);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        responseBodyArea.setText("Network Error: " + ex.getMessage());
                    });

                    return null;
                });
    }
}