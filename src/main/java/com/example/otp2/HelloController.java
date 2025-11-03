package com.example.otp2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void onConfirmClick(ActionEvent actionEvent) {
    }

    public void onEnterClick(ActionEvent actionEvent) {
    }

    public void onCalculateClick(ActionEvent actionEvent) {
    }
}