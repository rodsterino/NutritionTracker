package com.example.nutritiontracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HomeController extends Implementation {

    @FXML
    private PieChart pieChart;
    @FXML
    private Label nomacroLabel;
    @FXML
    private Label totalcaloriesLabel;

    @FXML
    private Label totalcarbsLabel;

    @FXML
    private Label totalfatLabel;

    @FXML
    private Label totalproteinLabel;
    public void initialize() {
        loadMacroDataIntoPieChart();
    }
    private void loadMacroDataIntoPieChart() {
        connectDB(); // Make sure you have a connection method
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        String sql = "SELECT SUM(CAST(Calories AS DOUBLE)) AS totalCalories, " +
                "SUM(CAST(Protein AS DOUBLE)) AS totalProtein, " +
                "SUM(CAST(Fat AS DOUBLE)) AS totalFat, " +
                "SUM(CAST(Carbs AS DOUBLE)) AS totalCarbs " +
                "FROM Tracker WHERE UserID = ? AND FORMAT(DateAdded, 'MM/dd/yyyy') = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.currentUserId);
            pstmt.setString(2, formattedDate);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double totalCalories = rs.getDouble("totalCalories");
                double totalProtein = rs.getDouble("totalProtein");
                double totalFat = rs.getDouble("totalFat");
                double totalCarbs = rs.getDouble("totalCarbs");

                double totalMacros = totalProtein + totalFat + totalCarbs;
                if (totalMacros > 0) {
                    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                            new PieChart.Data(String.format("Protein %.1f%%", (totalProtein / totalMacros * 100)), totalProtein),
                            new PieChart.Data(String.format("Fat %.1f%%", (totalFat / totalMacros * 100)), totalFat),
                            new PieChart.Data(String.format("Carbs %.1f%%", (totalCarbs / totalMacros * 100)), totalCarbs)
                    );

                    pieChart.setData(pieChartData);
                    pieChart.setTitle("Daily Macros");

                    // Set the text of each label with the corresponding values
                    totalcaloriesLabel.setText(String.format("%.2f", totalCalories));
                    totalproteinLabel.setText(String.format("%.2f g", totalProtein));
                    totalfatLabel.setText(String.format("%.2f g", totalFat));
                    totalcarbsLabel.setText(String.format("%.2f g", totalCarbs));
                } else {
                    pieChart.setData(FXCollections.observableArrayList()); // Clear the pie chart
                    nomacroLabel.setText("No macro data available for the current date.");
                    clearLabels();
                }
            } else {
                pieChart.setData(FXCollections.observableArrayList()); // Clear the pie chart
                nomacroLabel.setText("No macro data available for the current date.");
                clearLabels();
            }
        } catch (SQLException e) {
            System.out.println("Error loading macro data into pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearLabels() {
        // Clear the text of each label
        totalcaloriesLabel.setText("N/A");
        totalproteinLabel.setText("N/A");
        totalfatLabel.setText("N/A");
        totalcarbsLabel.setText("N/A");
    }
}

