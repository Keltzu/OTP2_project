module com.example.otp2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;


    opens otp2.shoppingcartapp.classes to javafx.fxml;
    exports otp2.shoppingcartapp.classes;
}