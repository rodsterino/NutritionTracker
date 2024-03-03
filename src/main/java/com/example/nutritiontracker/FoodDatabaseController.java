package com.example.nutritiontracker;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.*;

public class FoodDatabaseController {


    public static Connection connection;
    Statement statement;
    ResultSet resultSet;
    private final String baseUrl = "https://api.edamam.com/api/food-database/v2/parser";
    private final String appId = "2779cfe2";
    private final String appKey = "66be6e191fb200b10f484134e8be2b23";

    @FXML
    private TextField searchTextField;
    @FXML
    private TextField weightTextField; // TextField for weight input
    @FXML
    private ListView<String> foodItemList;
    @FXML
    private TextArea macronutrientDetailsTextArea;
    @FXML
    private Button addButton;

    @FXML
    public void initialize() {
        foodItemList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                fetchMacronutrients(newValue);
            }
        });
    }

    @FXML
    private void handleSearchAction() {
        foodItemList.getItems().clear();
        new Thread(() -> {
            String query = searchTextField.getText();
            searchFoodItem(query);
        }).start();
    }
    @FXML
    private void handleAddAction() {
        FoodMacro foodMacro = parseMacroDetails(macronutrientDetailsTextArea.getText());
        // Use the static variable from LoginController to get the current user's ID
        int userID = LoginController.currentUserId;
        if (userID > 0) { // Ensures that a user is logged in
            insertIntoDatabase(userID, foodMacro);
        } else {
            // Handle the case where no user is logged in, maybe show an error message
            Platform.runLater(() -> {
                showAlertDialog(Alert.AlertType.ERROR, "No User Logged In", "Please log in to add food items.");
            });
        }
    }
    private void searchFoodItem(String query) {
        try {
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
                        foodItemList.getItems().add(label);
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
                        JSONObject nutrients = food.getJSONObject("nutrients");

                        double weight = Double.parseDouble(weightTextField.getText()); // Parse the weight
                        double calories = nutrients.optDouble("ENERC_KCAL", 0) * weight / 100;
                        double protein = nutrients.optDouble("PROCNT", 0) * weight / 100;
                        double fat = nutrients.optDouble("FAT", 0) * weight / 100;
                        double carbs = nutrients.optDouble("CHOCDF", 0) * weight / 100;

                        String nutritionFacts = String.format(
                                "Nutrition facts: Serving Size (%.2f Gram)\nFood Item: %s\nCalories: %.2f kcal\nCarbs: %.2f g\nProtein: %.2f g\nTotal Fats: %.2f g",
                                weight, foodLabel, calories, carbs, protein, fat
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
    private void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header
        alert.setContentText(content);
        Platform.runLater(alert::showAndWait);
    }
    private FoodMacro parseMacroDetails(String details) {
        String[] lines = details.split("\\n");
        String foodItem = lines[1].substring(lines[1].indexOf(":") + 1).trim();
        double calories = Double.parseDouble(lines[2].substring(lines[2].indexOf(":") + 1, lines[2].indexOf("kcal")).trim());
        double protein = Double.parseDouble(lines[3].substring(lines[3].indexOf(":") + 1, lines[3].indexOf("g")).trim());
        double fat = Double.parseDouble(lines[4].substring(lines[4].indexOf(":") + 1, lines[4].indexOf("g")).trim());
        double carbs = Double.parseDouble(lines[5].substring(lines[5].indexOf(":") + 1, lines[5].indexOf("g")).trim());

        return new FoodMacro(foodItem, calories, protein, fat, carbs); // Now including weight in the constructor
    }


    private void connectDB() {
        if (connection == null) {
            try {
                Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
                String msAccDB = "NutritionTracker.accdb";
                String dbURL = "jdbc:ucanaccess://" + msAccDB;
                connection = DriverManager.getConnection(dbURL);
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                System.out.println("Database Connected...");
            } catch (Exception e) {
                System.out.println("Error connecting to database!");
                e.printStackTrace();
            }
        }
    }

    private void insertIntoDatabase(int userID, FoodMacro foodMacro) {
        connectDB(); // Ensure the connection is established
        PreparedStatement preparedStatement = null;
        try {
            String sql = "INSERT INTO Tracker (UserID, FoodItem, Calories, Protein, Fat, Carbs) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql); // Corrected: Removed the duplicated PreparedStatement type declaration
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, foodMacro.getFoodItem());
            preparedStatement.setDouble(3, foodMacro.getCalories());
            preparedStatement.setDouble(4, foodMacro.getProtein());
            preparedStatement.setDouble(5, foodMacro.getFat());
            preparedStatement.setDouble(6, foodMacro.getCarbs());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing insert into database");
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources");
                e.printStackTrace();
            }
        }
    }




}
