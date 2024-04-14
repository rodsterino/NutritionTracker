package com.example.nutritiontracker;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class ToolsController {

    @FXML
    private ToggleGroup Gendergroup;

    @FXML
    private ComboBox<String> activityComboBox;

    @FXML
    private TextField ageTextfield;

    @FXML
    private Label bmilbl;

    @FXML
    private Button calculateButton;

    @FXML
    private TextField calorieageTextField;

    @FXML
    private TextField caloriefeetTextfield;

    @FXML
    private TextField calorieinchesTextField;

    @FXML
    private TextField feetTextfield;

    @FXML
    private RadioButton femaleRadio;

    @FXML
    private TextField inchesTextField;

    @FXML
    private RadioButton maleRadio;

    @FXML
    private Button searchbmiButton;

    @FXML
    private TextField weightTextfield;

    @FXML
    private GridPane calorieResultsGrid;
    @FXML
    private TextField calorieWeightTextField;

    @FXML
    private void initialize() {
        searchbmiButton.setOnAction(event -> calculateBMI());
        calculateButton.setOnAction(event -> calculateCalories());
    }

    @FXML
    private void calculateBMI() {
        try {
            double weightInPounds = Double.parseDouble(weightTextfield.getText());
            double weightInKg = weightInPounds * 0.453592;
            int feet = Integer.parseInt(feetTextfield.getText());
            int inches = Integer.parseInt(inchesTextField.getText());
            double heightInMeters = feet * 0.3048 + inches * 0.0254;

            double bmi = weightInKg / (heightInMeters * heightInMeters);
            bmilbl.setText(String.format("%.2f", bmi));
        } catch (NumberFormatException e) {
            bmilbl.setText("Please enter valid numbers.");
        }
    }

    private void calculateCalories() {
        // Ensure you have fields for age, weight, height (feet and inches), gender, and activity level
        int age = Integer.parseInt(calorieageTextField.getText());
        int weight = Integer.parseInt(calorieWeightTextField.getText());
        int heightFeet = Integer.parseInt(caloriefeetTextfield.getText());
        int heightInches = Integer.parseInt(calorieinchesTextField.getText());

        // Convert height to cm and weight to kg
        int heightInCm = (int) ((heightFeet * 12 + heightInches) * 2.54);
        double weightInKg = weight / 2.20462;

        // Calculate BMR
        double bmr = (maleRadio.isSelected())
                ? 88.362 + (13.397 * weightInKg) + (4.799 * heightInCm) - (5.677 * age)
                : 447.593 + (9.247 * weightInKg) + (3.098 * heightInCm) - (4.330 * age);

        // Get activity multiplier
        double activityMultiplier = getActivityMultiplier(activityComboBox.getValue());

        // Calculate maintenance calories
        double maintenanceCalories = bmr * activityMultiplier;

        // Calculate calories for weight loss goals
        double mildWeightLossCalories = maintenanceCalories - 250;  // 0.5 lb/week
        double weightLossCalories = maintenanceCalories - 500;      // 1 lb/week
        double extremeWeightLossCalories = maintenanceCalories - 1000; // 2 lb/week

        // Update the results grid
        updateResultsGrid(maintenanceCalories, mildWeightLossCalories, weightLossCalories, extremeWeightLossCalories);
    }

    private double getActivityMultiplier(String activityLevel) {
        switch (activityLevel) {
            case "Sedentary": return 1.2;
            case "Lightly active": return 1.375;
            case "Moderately active": return 1.55;
            case "Very active": return 1.725;
            case "Super active": return 1.9;
            default: return 1; // Default case, should not occur
        }
    }

    private void updateResultsGrid(double maintenance, double mildLoss, double loss, double extremeLoss) {
        calorieResultsGrid.getChildren().clear(); // Clear previous results

        calorieResultsGrid.add(new Label("Maintain weight"), 0, 0);
        calorieResultsGrid.add(new Label(String.format("%.0f Calories/day 100%%", maintenance)), 1, 0);

        calorieResultsGrid.add(new Label("Mild weight loss\n 0.5 lb/week"), 0, 1);
        calorieResultsGrid.add(new Label(String.format("%.0f Calories/day 91%%", mildLoss)), 1, 1);

        calorieResultsGrid.add(new Label("Weight loss\n 1 lb/week"), 0, 2);
        calorieResultsGrid.add(new Label(String.format("%.0f Calories/day 81%%", loss)), 1, 2);

        calorieResultsGrid.add(new Label("Extreme weight loss\n 2 lb/week"), 0, 3);
        calorieResultsGrid.add(new Label(String.format("%.0f Calories/day 62%%", extremeLoss)), 1, 3);
    }

}
