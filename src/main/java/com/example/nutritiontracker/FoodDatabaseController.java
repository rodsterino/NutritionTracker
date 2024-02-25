package com.example.nutritiontracker;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class FoodDatabaseController {

    private final String baseUrl = "https://api.edamam.com/api/food-database/v2/parser";
    private final String appId = "2779cfe2";
    private final String appKey = "66be6e191fb200b10f484134e8be2b23";

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<String> foodItemList;

    @FXML
    private TextArea macronutrientDetailsTextArea;

    @FXML
    public void initialize() {
        // Set up the listener for the ListView selection
        foodItemList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                fetchMacronutrients(newValue);
            }
        });
    }

    @FXML
    private void handleSearchAction() {
        foodItemList.getItems().clear(); // Clear previous search results
        new Thread(() -> {
            String query = searchTextField.getText();
            searchFoodItem(query);
        }).start();
    }

    private void searchFoodItem(String query) {
        try {
            String appId = "2779cfe2"; // Use your actual App ID
            String appKey = "66be6e191fb200b10f484134e8be2b23"; // Use your actual App Key
            String baseUrl = "https://api.edamam.com/api/food-database/v2/parser";
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL(baseUrl + "?ingr=" + encodedQuery + "&app_id=" + appId + "&app_key=" + appKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray hints = jsonResponse.getJSONArray("hints");
                Platform.runLater(() -> {
                    for (int i = 0; i < hints.length(); i++) {
                        JSONObject food = hints.getJSONObject(i).getJSONObject("food");
                        String label = food.getString("label");
                        foodItemList.getItems().add(label); // Add labels to the ListView
                    }
                });
            } else {
                Platform.runLater(() -> foodItemList.getItems().add("Request failed. Response Code: " + responseCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> foodItemList.getItems().add("Error occurred while fetching data: " + e.getMessage()));
        }
    }

    private void fetchMacronutrients(String foodLabel) {
        new Thread(() -> {
            try {
                String encodedLabel = URLEncoder.encode(foodLabel, "UTF-8");
                URL url = new URL(baseUrl + "?ingr=" + encodedLabel + "&app_id=" + appId + "&app_key=" + appKey);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray hints = jsonResponse.getJSONArray("hints");
                    if (hints.length() > 0) {
                        JSONObject food = hints.getJSONObject(0).getJSONObject("food");
                        String label = food.getString("label");
                        JSONObject nutrients = food.getJSONObject("nutrients");

                        double calories = nutrients.optDouble("ENERC_KCAL", 0);
                        double protein = nutrients.optDouble("PROCNT", 0);
                        double fat = nutrients.optDouble("FAT", 0);
                        double carbs = nutrients.optDouble("CHOCDF", 0);

                        String nutritionFacts = String.format(
                                "Nutrition facts: Serving Size (100 Gram)\nFood Item: %s\nCalories: %.2f kcal\nCarbs: %.2f g\nProtein: %.2f g\nTotal Fats: %.2f g",
                                label, calories, carbs, protein, fat
                        );

                        Platform.runLater(() -> macronutrientDetailsTextArea.setText(nutritionFacts));
                    } else {
                        Platform.runLater(() -> macronutrientDetailsTextArea.setText("No results found."));
                    }
                } else {
                    Platform.runLater(() -> macronutrientDetailsTextArea.setText("GET request not worked"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> macronutrientDetailsTextArea.setText("Error occurred while fetching data."));
                e.printStackTrace();
            }
        }).start();
    }

}

