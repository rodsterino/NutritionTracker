module com.example.nutritiontracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens com.example.nutritiontracker to javafx.fxml;
    exports com.example.nutritiontracker;
}