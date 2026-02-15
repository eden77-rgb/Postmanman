package org.postmanman;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.postmanman.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

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

        methodCombo.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodCombo.setValue("GET");

        methodCombo.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll(
                            "method-get","method-post",
                            "method-put","method-delete",
                            "method-patch"
                    );
                } else {
                    setText(item);
                    getStyleClass().removeAll(
                            "method-get","method-post",
                            "method-put","method-delete",
                            "method-patch"
                    );
                    getStyleClass().add("method-" + item.toLowerCase());
                }
            }
        });

        methodCombo.setButtonCell(methodCombo.getCellFactory().call(null));

        historyList.setItems(Logger.load());

        historyList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && historyList.getSelectionModel().getSelectedItem() != null) {
                String selectedItem =  historyList.getSelectionModel().getSelectedItem();

                String[] parts = selectedItem.split(" - ", 2);
                if (parts.length == 2) {
                    String method = parts[0];
                    String url = parts[1];

                    methodCombo.setValue(method);
                    urlField.setText(url);

                    responseBodyArea.clear();
                    responseHeadersArea.clear();
                }
            }
        });
    }

    @FXML
    protected void onSendRequest() {
        String url = urlField.getText();
        String method = methodCombo.getValue();

        if (url == null || !url.startsWith("http")) {
            responseBodyArea.setText("Error: URL must start with http:// or https://");
            return;
        }

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

            String rawHeaders = requestHeadersArea.getText();
            if (rawHeaders != null && !rawHeaders.isBlank()) {

                rawHeaders.lines().forEach(line -> {
                    if (line.contains(":")) {

                        String[] parts = line.split(":", 2);
                        builder.header(parts[0].trim(), parts[1].trim());
                    }
                });
            }

            String bodyData = requestBodyArea.getText();
            if (bodyData == null) {

                bodyData = "";
            }

            switch (method) {
                case "POST":
                    builder.POST(HttpRequest.BodyPublishers.ofString(bodyData));
                    break;

                case "PUT":
                    builder.PUT(HttpRequest.BodyPublishers.ofString(bodyData));
                    break;

                case "PATCH":
                    builder.method("PATCH", HttpRequest.BodyPublishers.ofString(bodyData));
                    break;

                case "DELETE":
                    builder.DELETE();
                    break;

                default:
                    builder.GET();
            }

            responseBodyArea.setText("Requesting data from: " + url + "...");

            client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenAccept(rep -> {
                        String rawResponseBody = rep.body();
                        String formattedBody;

                        try {
                            JsonElement je = JsonParser.parseString(rawResponseBody);
                            formattedBody = gson.toJson(je);
                        }

                        catch (Exception e) {
                            formattedBody = rawResponseBody;
                        }

                        String statusLine = "Status: " + rep.statusCode() + "\n";
                        String repHeaders = rep.headers().map().entrySet().stream()
                                .map(entry -> entry.getKey() + ": " + String.join(", ", entry.getValue()))
                                .collect(Collectors.joining("\n"));

                        final String finalBody = formattedBody;
                        final String finalHeaders = statusLine + "--------------------------\n" + repHeaders;

                        Platform.runLater(() -> {
                            responseBodyArea.setText(finalBody);
                            responseHeadersArea.setText(finalHeaders);

                            historyList.getItems().add(0, method + " - " + url);
                            Logger.save(historyList.getItems());
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            responseBodyArea.setText("Network Error: " + ex.getMessage());
                        });

                        return null;
                    });
        }

        catch (Exception e ) {
            responseBodyArea.setText("Construction Error: " + e.getMessage());
        }
    }
}