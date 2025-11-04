package com.example.otp2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ShoppingCartController {

    @FXML private Label lblSelectLanguage;
    @FXML private ComboBox<String> comboLanguage;
    @FXML private Button btnConfirmLanguage;

    @FXML private Label lblPrompt;
    @FXML private TextField txtItemCount;
    @FXML private Button btnEnterItems;
    @FXML private Button btnCalculate;
    @FXML private ListView<String> listItems;
    @FXML private Label lblTotal;

    private final List<Double> prices = new ArrayList<>();
    private ResourceBundle rb;

    @FXML
    public void initialize() {
        if (comboLanguage != null) {
            comboLanguage.getItems().setAll("EN", "FR", "UR", "VI");
            comboLanguage.getSelectionModel().select("EN");
        }
        setLanguage("en", "US");
        lblTotal.setText("Total: 0.00 €");
        btnCalculate.setDisable(true);
    }

    @FXML
    public void onConfirmLanguage(ActionEvent e) {
        String code = comboLanguage != null ? comboLanguage.getValue() : "EN";
        if (code == null) code = "EN";
        switch (code) {
            case "FR" -> setLanguage("fr", "FR");
            case "UR" -> setLanguage("ur", "PK");
            case "VI" -> setLanguage("vi", "VN");
            default -> setLanguage("en", "US");
        }
    }

    private void setLanguage(String lang, String country) {
        Locale locale = new Locale(lang, country);
        rb = ResourceBundle.getBundle("MessagesBundle", locale);

        if (lblPrompt != null && lblPrompt.getScene() != null) {
            Stage stage = (Stage) lblPrompt.getScene().getWindow();
            if (stage != null) stage.setTitle(rb.getString("title"));
        }

        lblSelectLanguage.setText(rb.getString("selectLanguage"));
        btnConfirmLanguage.setText(rb.getString("confirmLanguage"));
        lblPrompt.setText(rb.getString("enterItemsCount"));
        txtItemCount.setPromptText(rb.getString("itemsCountPlaceholder"));
        btnEnterItems.setText(rb.getString("enterItems"));
        btnCalculate.setText(rb.getString("calculateTotal"));
    }

    @FXML
    public void onEnterItems(ActionEvent e) {
        prices.clear();
        listItems.getItems().clear();
        lblTotal.setText("Total: 0.00 €");

        int count;
        try {
            count = Integer.parseInt(txtItemCount.getText().trim());
            if (count <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showInfo(rb.getString("errInvalidCount"));
            return;
        }

        for (int i = 1; i <= count; i++) {
            Double price = askForPrice(i);
            if (price == null) {
                showInfo(String.format(rb.getString("msgCancelled"), (i - 1)));
                break;
            }
            prices.add(price);
            listItems.getItems().add(String.format("%s %d: %.2f €", rb.getString("itemWord"), i, price));
        }

        btnCalculate.setDisable(prices.isEmpty());
    }

    @FXML
    public void onCalculate(ActionEvent e) {
        if (prices.isEmpty()) {
            showInfo(rb.getString("errNoItems"));
            return;
        }
        double total = calculateTotal(prices);
        lblTotal.setText(String.format("Total: %.2f €", total));
    }

    private Double askForPrice(int index) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(String.format("%s %d", rb.getString("itemWord"), index));
        dialog.setHeaderText(null);
        dialog.setContentText(String.format(rb.getString("promptPriceFor"), index));

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        okBtn.setText(rb.getString("ok"));
        cancelBtn.setText(rb.getString("cancel"));

        while (true) {
            var res = dialog.showAndWait();
            if (res.isEmpty()) return null;
            try {
                double p = Double.parseDouble(res.get().trim().replace(',', '.'));
                if (p < 0) throw new NumberFormatException();
                return p;
            } catch (NumberFormatException ex) {
                showInfo(rb.getString("errInvalidPrice"));
                dialog.getEditor().setText("");
            }
        }
    }

    private double calculateTotal(List<Double> prices) {
        double sum = 0;
        for (double p : prices) sum += p;
        return sum;
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(rb != null ? rb.getString("messageTitle") : "Message");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
