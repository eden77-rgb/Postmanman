package org.postmanman.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Logger {
    public static void save(ObservableList<String> items) {
        try {
            FileWriter file = new FileWriter("src/main/java/org/postmanman/data/save.txt");

            for (String item : items ) {
                file.write(item + "\n");
            }

            file.close();
        }

        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ObservableList<String> load() {
        ObservableList<String> items = FXCollections.observableArrayList();
        File file = new File("src/main/java/org/postmanman/data/save.txt");

        try (Scanner fileReader = new Scanner(file)) {

            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                items.add(data);
            }
        }

        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return items;
    }
}
