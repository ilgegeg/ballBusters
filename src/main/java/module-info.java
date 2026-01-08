module com.example.ballbusters {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ballbusters to javafx.fxml;
    exports com.example.ballbusters;
}