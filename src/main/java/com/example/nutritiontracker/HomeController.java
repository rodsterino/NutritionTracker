package com.example.nutritiontracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeController extends Implementation {

    @FXML
    private PieChart pieChart;

    public void initialize() {
        loadMacroDataIntoPieChart();
    }
    private void loadMacroDataIntoPieChart() {
        connectDB(); // Make sure you have a connection method

        String sql = "SELECT SUM(CAST(Calories AS DOUBLE)) AS totalCalories, SUM(CAST(Protein AS DOUBLE)) AS totalProtein, SUM(CAST(Fat AS DOUBLE)) AS totalFat, SUM(CAST(Carbs AS DOUBLE)) AS totalCarbs FROM Tracker WHERE UserID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.currentUserId); // Set the current user's ID
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double totalCalories = rs.getDouble("totalCalories");
                double totalProtein = rs.getDouble("totalProtein");
                double totalFat = rs.getDouble("totalFat");
                double totalCarbs = rs.getDouble("totalCarbs");
                double totalMacros = totalProtein + totalFat + totalCarbs;

                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                        new PieChart.Data(String.format("Protein %.1f%%", (totalProtein / totalMacros * 100)), totalProtein),
                        new PieChart.Data(String.format("Fat %.1f%%", (totalFat / totalMacros * 100)), totalFat),
                        new PieChart.Data(String.format("Carbs %.1f%%", (totalCarbs / totalMacros * 100)), totalCarbs)
                );

                pieChart.setData(pieChartData);
                pieChart.setTitle("Daily Macros");
            }
        } catch (SQLException e) {
            System.out.println("Error loading macro data into pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

