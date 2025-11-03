module com.example.otp2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.otp2 to javafx.fxml;
    exports com.example.otp2;
}