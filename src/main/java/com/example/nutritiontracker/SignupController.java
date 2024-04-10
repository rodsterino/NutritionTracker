package com.example.nutritiontracker;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignupController extends Implementation {
    @FXML
    private TextField emailField;
    @FXML
    private TextField firstnameField;
    @FXML
    private TextField lastnameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button signupButton;
    @FXML
    private Button backButton;


    @FXML
    public void initialize() {
        // Initialization logic if needed
    }

    @FXML
    private void handleSignupAction() {
        Task<Boolean> signupTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                String email = emailField.getText();
                String firstname = firstnameField.getText();
                String lastname = lastnameField.getText();
                String password = passwordField.getText();

                if (!validateInput(email, firstname, lastname, password)) {
                    Platform.runLater(() -> showAlert("Validation Error", "Please ensure all fields are filled correctly."));
                    return false;
                }

                if (isEmailUsed(email)) {
                    Platform.runLater(() -> showAlert("Signup Failed", "Email is already in use."));
                    return false;
                }

                boolean userCreated = createUser(firstname, lastname, email, password);
                if (userCreated) {
                    Platform.runLater(() -> {
                        showAlert("Signup Successful", "Your account has been created.");
                        clearForm();
                    });
                } else {
                    Platform.runLater(() -> showAlert("Signup Failed", "An error occurred. Please try again later."));
                }
                return userCreated;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                signupButton.setDisable(false);
            }

            @Override
            protected void failed() {
                super.failed();
                signupButton.setDisable(false);
            }
        };

        signupTask.setOnFailed(e -> {
            Throwable throwable = signupTask.getException();
            showAlert("Database Error", "An error occurred with the database. Please contact support.");
            throwable.printStackTrace();
        });

        Thread thread = new Thread(signupTask);
        thread.setDaemon(true);
        thread.start();
    }

    private boolean validateInput(String email, String firstname, String lastname, String password) {
        return !email.isEmpty() && !firstname.isEmpty() && !lastname.isEmpty() && password.length() >= 8;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if ("Signup Successful".equals(title)) {
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) signupButton.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            alert.showAndWait();
        }
    }

    private void clearForm() {
        emailField.clear();
        firstnameField.clear();
        lastnameField.clear();
        passwordField.clear();
    }

    public boolean isEmailUsed(String email) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM User WHERE Email = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("count") > 0;
        }
        return false;
    }

    public boolean createUser(String firstname, String lastname, String email, String password) throws SQLException {
        String query = "INSERT INTO User (Firstname, Lastname, Email, Password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, firstname);
            statement.setString(2, lastname);
            statement.setString(3, email);
            statement.setString(4, password);

            int result = statement.executeUpdate();
            return result > 0;
        }
    }
    @FXML
    private void handleBackAction() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();

            // Get the current stage from the back button
            Stage stage = (Stage) backButton.getScene().getWindow();

            // Set the scene to login view
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
