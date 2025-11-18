module com.example.otp2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;


    opens com.example.otp2 to javafx.fxml;
    exports com.example.otp2;
}