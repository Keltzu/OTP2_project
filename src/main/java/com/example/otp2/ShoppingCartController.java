package com.example.otp2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller for the Shopping Cart application.
 * Handles:
 *  - Language selection via ComboBox and ResourceBundles.
 *  - Item entry and price collection.
 *  - Total price calculation and display.
 */
public class ShoppingCartController {

    // ------------------------------
    // FXML UI Elements (injected from shopping-view.fxml)
    // ------------------------------

    // --- Language selection controls ---
    @FXML private Label lblSelectLanguage;          // "Select the language:" label
    @FXML private ComboBox<String> comboLanguage;   // Dropdown menu for language selection (EN, FR, UR, VI)
    @FXML private Button btnConfirmLanguage;        // "Confirm Language" button

    // --- Main shopping UI controls ---
    @FXML private Label lblPrompt;                  // Label prompting user to enter item count
    @FXML private TextField txtItemCount;           // Field where user enters number of items
    @FXML private Button btnEnterItems;             // Button to trigger item entry dialogs
    @FXML private Button btnCalculate;              // Button to calculate the total price
    @FXML private ListView<String> listItems;       // Displays list of entered item prices
    @FXML private Label lblTotal;                   // Displays calculated total amount

    // ------------------------------
    // Variables used internally
    // ------------------------------

    private final List<Double> prices = new ArrayList<>();  // Stores all item prices entered by the user
    private ResourceBundle rb;                              // Holds localized text strings
    // ------------------------------
    // Initialization
    // ------------------------------

    /**
     * Called automatically when the FXML view is loaded.
     * Sets up default language and initializes the UI state.
     */
    @FXML
    public void initialize() {
        // Populate ComboBox with available language codes
        if (comboLanguage != null) {
            comboLanguage.getItems().setAll("EN", "FR", "UR", "VI");
            comboLanguage.getSelectionModel().select("EN"); // Default language
        }

        // Load default English texts
        setLanguage("en", "US");

        // Initialize labels and button states
        lblTotal.setText("Total: 0.00 €");
        btnCalculate.setDisable(true);  // Disabled until items are entered
    }

    // ------------------------------
    // Language Selection Logic
    // ------------------------------

    /**
     * Triggered when the "Confirm Language" button is clicked.
     * Loads the correct ResourceBundle based on selected language code.
     */
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

    /**
     * Updates all UI labels and buttons with localized text
     * according to the selected language.
     */
    private void setLanguage(String lang, String country) {
        // Tracks currently active locale
        Locale currentLocale = new Locale(lang, country);
        rb = ResourceBundle.getBundle("MessagesBundle", currentLocale);

        // Update window title
        if (lblPrompt != null && lblPrompt.getScene() != null) {
            Stage stage = (Stage) lblPrompt.getScene().getWindow();
            if (stage != null) stage.setTitle(rb.getString("title"));
        }

        // Update top section (language selection)
        lblSelectLanguage.setText(rb.getString("selectLanguage"));
        btnConfirmLanguage.setText(rb.getString("confirmLanguage"));

        // Update main shopping UI text
        lblPrompt.setText(rb.getString("enterItemsCount"));
        txtItemCount.setPromptText(rb.getString("itemsCountPlaceholder"));
        btnEnterItems.setText(rb.getString("enterItems"));
        btnCalculate.setText(rb.getString("calculateTotal"));
    }

    // ------------------------------
    // Main Shopping Cart Logic
    // ------------------------------

    /**
     * Triggered when "Enter Items" button is pressed.
     *  - Reads number of items from input field
     *  - Prompts user to enter price for each item
     *  - Populates list with entered prices
     */
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

        // Ask for prices for each item
        for (int i = 1; i <= count; i++) {
            Double price = askForPrice(i);
            if (price == null) { // User cancelled input
                showInfo(String.format(rb.getString("msgCancelled"), (i - 1)));
                break;
            }

            prices.add(price);
            listItems.getItems().add(
                    String.format("%s %d: %.2f €", rb.getString("itemWord"), i, price)
            );
        }

        btnCalculate.setDisable(prices.isEmpty());
    }

    /**
     * Triggered when "Calculate Total" is pressed.
     * Adds up all entered prices and updates the total label.
     */
    @FXML
    public void onCalculate(ActionEvent e) {
        if (prices.isEmpty()) {
            showInfo(rb.getString("errNoItems"));
            return;
        }

        double total = calculateTotal(prices);
        lblTotal.setText(String.format("Total: %.2f €", total));
    }

    // ------------------------------
    // Helper Methods
    // ------------------------------

    /**
     * Prompts the user to enter a price for a specific item.
     * @param index The item number currently being entered.
     * @return The price entered by the user, or null if cancelled.
     */
    private Double askForPrice(int index) {
        TextInputDialog dialog = new TextInputDialog();

        // Localized title and prompt
        dialog.setTitle(String.format("%s %d", rb.getString("itemWord"), index));
        dialog.setHeaderText(null);
        dialog.setContentText(String.format(rb.getString("promptPriceFor"), index));

        // Localize OK/Cancel button labels
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        okBtn.setText(rb.getString("ok"));
        cancelBtn.setText(rb.getString("cancel"));

        // Input validation loop
        while (true) {
            var res = dialog.showAndWait();
            if (res.isEmpty()) return null; // cancelled

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

    /**
     * Sums all prices entered by the user.
     * @param prices List of item prices
     * @return Total of all prices
     */
    private double calculateTotal(List<Double> prices) {
        double sum = 0;
        for (double p : prices) sum += p;
        return sum;
    }

    /**
     * Displays an information dialog with a given message.
     * Localized title and text are used when available.
     */
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(rb != null ? rb.getString("messageTitle") : "Message");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
