package com.example.otp2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class ShoppingCartController {

    private static final String ITEM_WORD_KEY = "itemWord";
    private static final String DIALOG_CSS = "dialog.css";
    private static final String TOTAL_KEY = "total";


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
            lblTotal.setText(tr(TOTAL_KEY)+ " 0.00 €");

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
        lblTotal.setText(tr(TOTAL_KEY)+ " 0.00 €");
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
            ItemEntry entry = askForPrice(i);
            if (entry == null) {
                showInfo(String.format(tr("msgCancelled"), (i - 1)));
                break;
            }
            double price = entry.getPrice();
            int qty = entry.getQuantity();
            for (int q = 0; q < qty; q++) {
                prices.add(price);
                listItems.getItems().add(
                        String.format("%s %d: %.2f €", tr(ITEM_WORD_KEY), i, price)
                );
            }
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
        lblTotal.setText(tr(TOTAL_KEY)+ String.format(" %.2f €", total));

        if (btnSaveToDb != null) {
            btnSaveToDb.setDisable(false);
        }
    }

    @FXML
    private ItemEntry askForPrice(int index) {
        Double price = askForSinglePrice(index);
        if (price == null) return null;

        Integer qty = askForQuantity(index);
        if (qty == null) return null;

        return new ItemEntry(price, qty);
    }
    @FXML
    private Double askForSinglePrice(int index) {
        TextInputDialog d = createTextDialog(
                String.format(tr("promptPriceFor"), index), tr("ok"), tr("cancel")
        );

        while (true) {
            var res = d.showAndWait();
            if (res.isEmpty()) return null;

            try {
                double price = Double.parseDouble(res.get().trim().replace(',', '.'));
                if (price < 0) throw new NumberFormatException();
                return price;
            } catch (NumberFormatException ex) {
                showInfo(tr("errInvalidPrice"));
                d.getEditor().setText("");
            }
        }
    }

    @FXML
    private Integer askForQuantity(int index) {
        TextInputDialog d = createTextDialog(
                tr("itemsCountPlaceholder"), tr("ok"), tr("cancel")
        );
        d.getEditor().setText("1");

        while (true) {
            var res = d.showAndWait();
            if (res.isEmpty()) return null;

            try {
                int qty = Integer.parseInt(res.get().trim());
                if (qty <= 0) throw new NumberFormatException();
                return qty;
            } catch (NumberFormatException ex) {
                showInfo(tr("errInvalidCount"));
                d.getEditor().setText("");
            }
        }
    }

    @FXML
    private TextInputDialog createTextDialog(String content, String ok, String cancel) {
        TextInputDialog d = new TextInputDialog();
        d.setHeaderText(null);
        d.setContentText(content);
        d.getDialogPane().lookupButton(ButtonType.OK);
        d.getDialogPane().lookupButton(ButtonType.CANCEL);
        d.getDialogPane().getStylesheets().add(
                getClass().getResource(DIALOG_CSS).toExternalForm()
        );
        return d;
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
        // dialog stylesheet
        DialogPane dp = a.getDialogPane();
        dp.getStylesheets().add(
                getClass().getResource(DIALOG_CSS).toExternalForm()
        );
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
