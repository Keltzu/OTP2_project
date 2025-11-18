package com.example.otp2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class ShoppingCartController {

    @FXML private VBox rootPane;

    @FXML private Label lblSelectLanguage;
    @FXML private ComboBox<String> comboLanguage;
    @FXML private Button btnConfirmLanguage;
    @FXML private Button btnSaveToDb;

    @FXML private Label lblPrompt;
    @FXML private TextField txtItemCount;
    @FXML private Button btnEnterItems;
    @FXML private Button btnCalculate;
    @FXML private ListView<String> listItems;
    @FXML private Label lblTotal;

    private final List<Double> prices = new ArrayList<>();
    private ResourceBundle rb;
    private Map<String, String> dbStrings = Collections.emptyMap();
    private double lastTotal = 0.0;
    private String currentLanguageCode = "en";

    private String tr(String key) {
        if (dbStrings != null) {
            String fromDb = dbStrings.get(key);
            if (fromDb != null) {
                return fromDb;
            }
        }
        if (rb != null && rb.containsKey(key)) {
            return rb.getString(key);
        }
        return key;
    }

    @FXML
    public void initialize() {
        if (comboLanguage != null) {
            comboLanguage.getItems().setAll("EN", "FR", "UR", "VI");
            comboLanguage.getSelectionModel().select("EN");
        }
        setLanguage("en", "US");
        lblTotal.setText("Total: 0.00 €");
        btnCalculate.setDisable(true);

        if (btnSaveToDb != null) {
            btnSaveToDb.setDisable(true);
        }
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
        currentLanguageCode = lang;

        Locale locale = new Locale(lang, country);
        rb = ResourceBundle.getBundle("MessagesBundle", locale);
        dbStrings = LocalizationService.getLocalizedStrings(locale);

        if (lblPrompt != null && lblPrompt.getScene() != null) {
            Stage stage = (Stage) lblPrompt.getScene().getWindow();
            if (stage != null) stage.setTitle(tr("title"));
        }

        lblSelectLanguage.setText(tr("selectLanguage"));
        btnConfirmLanguage.setText(tr("confirmLanguage"));
        lblPrompt.setText(tr("enterItemsCount"));
        txtItemCount.setPromptText(tr("itemsCountPlaceholder"));
        btnEnterItems.setText(tr("enterItems"));
        btnCalculate.setText(tr("calculateTotal"));

        if (btnSaveToDb != null) {
            btnSaveToDb.setText(tr("saveToDb"));
        }

        if (rootPane != null) {
            if ("ur".equals(lang)) {
                rootPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                txtItemCount.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                listItems.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                lblSelectLanguage.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                lblPrompt.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                lblTotal.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            } else {
                rootPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                txtItemCount.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                listItems.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                lblSelectLanguage.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                lblPrompt.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                lblTotal.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
        }
    }

    @FXML
    public void onEnterItems(ActionEvent e) {
        prices.clear();
        listItems.getItems().clear();
        lblTotal.setText("Total: 0.00 €");
        lastTotal = 0.0;
        if (btnSaveToDb != null) {
            btnSaveToDb.setDisable(true);
        }

        int count;
        try {
            count = Integer.parseInt(txtItemCount.getText().trim());
            if (count <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showInfo(tr("errInvalidCount"));
            return;
        }

        for (int i = 1; i <= count; i++) {
            Double price = askForPrice(i);
            if (price == null) {
                showInfo(String.format(tr("msgCancelled"), (i - 1)));
                break;
            }
            prices.add(price);
            listItems.getItems().add(
                    String.format("%s %d: %.2f €", tr("itemWord"), i, price)
            );
        }

        btnCalculate.setDisable(prices.isEmpty());
    }

    @FXML
    public void onCalculate(ActionEvent e) {
        if (prices.isEmpty()) {
            showInfo(tr("errNoItems"));
            return;
        }
        double total = calculateTotal(prices);
        lastTotal = total;
        lblTotal.setText(String.format("Total: %.2f €", total));

        if (btnSaveToDb != null) {
            btnSaveToDb.setDisable(false);
        }
    }

    private Double askForPrice(int index) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(String.format("%s %d", tr("itemWord"), index));
        dialog.setHeaderText(null);
        dialog.setContentText(String.format(tr("promptPriceFor"), index));

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        okBtn.setText(tr("ok"));
        cancelBtn.setText(tr("cancel"));

        while (true) {
            var res = dialog.showAndWait();
            if (res.isEmpty()) return null;
            try {
                double p = Double.parseDouble(res.get().trim().replace(',', '.'));
                if (p < 0) throw new NumberFormatException();
                return p;
            } catch (NumberFormatException ex) {
                showInfo(tr("errInvalidPrice"));
                dialog.getEditor().setText("");
            }
        }
    }

    private double calculateTotal(List<Double> prices) {
        double sum = 0;
        for (double p : prices) {
            sum += p;
        }
        return sum;
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(tr("messageTitle"));
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    public void onSaveToDb(ActionEvent e) {
        if (prices.isEmpty() || lastTotal <= 0) {
            showInfo(tr("errNoItems"));
            return;
        }

        ShoppingCartResultService.saveCartResult(
                new ArrayList<>(prices),
                lastTotal,
                currentLanguageCode,
                null
        );

        showInfo(tr("savedToDb"));
        btnSaveToDb.setDisable(true);
    }
}
