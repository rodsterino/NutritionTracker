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

    // Static variable to hold the current user's ID for global access
    public static int currentUserId = -1;

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
            // Adjusted to use "ID" as the column name, matching your User table structure
            String query = "SELECT ID, Email, Password FROM User WHERE Email = ? AND Password = ?";
            PreparedStatement pst = Implementation.connection.prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet resultSet = pst.executeQuery();

            if (resultSet.next()) {
                // Correctly fetches the "ID" field from the result set
                currentUserId = resultSet.getInt("ID");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlertDialog(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database.");
            return false;
        }
    }


    public static int getCurrentUserId() {
        return currentUserId;
    }

    private void switchToMainPane() {
        try {
            Parent mainPaneRoot = FXMLLoader.load(getClass().getResource("MainPane.fxml"));
            Scene mainPaneScene = new Scene(mainPaneRoot, 1400, 800); // Adjust size as needed

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(mainPaneScene);
            stage.setTitle("Main Pane");
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
