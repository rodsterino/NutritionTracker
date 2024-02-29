package com.example.nutritiontracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private void initialize() {
        setupLoginButtonAction();
    }

    @FXML
    private void setupLoginButtonAction() {
        loginButton.setOnAction(event -> {
            String email = usernameTextField.getText();
            String password = passwordField.getText();
            if (validateCredentials(email, password)) {
                showAlertDialog(Alert.AlertType.INFORMATION, "Login Successful", "Welcome!" );
                switchToMainPane(); // This line switches to the MainPane.fxml after successful login
            } else {
                showAlertDialog(Alert.AlertType.ERROR, "Login Failed", "Incorrect email or password.");
            }
        });
    }

    private boolean validateCredentials(String email, String password) {
        try {
            // Query to find the user with the provided email and password
            String query = "SELECT * FROM User WHERE Email = ? AND Password = ?";
            PreparedStatement pst = Implementation.connection.prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet resultSet = pst.executeQuery();

            // Check if a user was found
            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            showAlertDialog(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database.");
            return false; // Incorrect credentials or database error
        }
    }

    private void switchToMainPane() {
        try {
            // Load the main pane
            Parent mainPaneRoot = FXMLLoader.load(getClass().getResource("MainPane.fxml"));
            Scene mainPaneScene = new Scene(mainPaneRoot, 1200, 800); // Adjust size as needed

            // Get the current stage (window) from any control, e.g., the login button
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set the scene to the main pane
            stage.setScene(mainPaneScene);
            stage.setTitle("Main Pane"); // Optionally set a new title
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
