package otp2.shoppingcartapp.classes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

/**
 * Controller for the JavaFX Shopping Cart view.
 * <p>
 * This class handles:
 * <ul>
 *     <li>Language selection and localization of UI texts</li>
 *     <li>Input of item count and item prices</li>
 *     <li>Calculation of the total price</li>
 *     <li>Saving the shopping cart result into the database</li>
 * </ul>
 */

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

    /**
     * List of item prices currently entered by the user.
     */
    private final List<Double> prices = new ArrayList<>();

    /**
     * Resource bundle used for loading localized strings from properties files.
     */
    private ResourceBundle rb;

    /**
     * Localized strings loaded from the database, if available.
     * Keys are message identifiers, values are translated texts.
     */
    private Map<String, String> dbStrings = Collections.emptyMap();

    /**
     * Last calculated total value of the shopping cart.
     */
    private double lastTotal = 0.0;

    /**
     * The current language code used for localization (e.g. {@code "en"}, {@code "fr"}).
     */
    private String currentLanguageCode = "en";

    /**
     * Returns a localized string for the given key.
     * <p>
     * The method first checks the strings loaded from the database.
     * If no value is found there, it falls back to the {@link ResourceBundle}.
     * If the key is still not found, the key itself is returned.
     *
     * @param key the message key
     * @return the localized string, or the key if no translation is found
     */

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

    /**
     * Initializes the controller after the FXML fields have been injected.
     * <p>
     * Sets the default language, initializes the language combo box and
     * disables buttons that require user input such as "Calculate" and "Save to DB".
     */

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

    /**
     * Handles the language confirmation action.
     * <p>
     * Reads the selected language from the combo box and updates the UI texts
     * and layout direction accordingly.
     *
     * @param e the action event fired by the "Confirm Language" button
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
     * Updates the current language and reloads all localized UI texts.
     * <p>
     * This method:
     * <ul>
     *     <li>Loads the {@link ResourceBundle} for the given locale</li>
     *     <li>Loads additional localized strings from the database</li>
     *     <li>Updates window title and all visible labels and buttons</li>
     *     <li>Switches layout direction for right-to-left languages</li>
     * </ul>
     *
     * @param lang    the ISO language code (e.g. "en", "fr", "ur")
     * @param country the country code used when loading the locale (e.g. "US", "FR")
     */

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

    /**
     * Handles the action when the user confirms how many items they want to enter.
     * <p>
     * This method:
     * <ul>
     *     <li>Validates the entered item count</li>
     *     <li>Asks the user to input the price for each item</li>
     *     <li>Populates the list of prices and updates the items list view</li>
     * </ul>
     *
     * @param e the action event fired by the "Enter Items" button
     */

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

    /**
     * Handles the action for calculating the total price of all entered items.
     * <p>
     * If no items have been entered, an information dialog is shown.
     * Otherwise, the total is calculated, displayed and stored for saving to DB.
     *
     * @param e the action event fired by the "Calculate" button
     */

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

    /**
     * Shows a dialog to ask the user to enter the price for one item.
     * <p>
     * The dialog validates that the input is a non-negative number.
     * If the user cancels the dialog, {@code null} is returned.
     *
     * @param index the index of the item (1-based), used in the dialog title and message
     * @return the entered price, or {@code null} if the user cancels
     */

    private Double askForPrice(int index) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(String.format("%s %d", tr("itemWord"), index));
        dialog.setHeaderText(null);
        dialog.setContentText(String.format(tr("promptPriceFor"), index));
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        okBtn.setText(tr("ok"));
        cancelBtn.setText(tr("cancel"));
        DialogPane dp = dialog.getDialogPane();
        dp.getStylesheets().add(
                getClass().getResource("/otp2/shoppingcartapp/ui/dialog.css").toExternalForm()
        );

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

    /**
     * Calculates the total sum of all prices in the given list.
     *
     * @param prices list of item prices
     * @return the sum of all values in the list
     */

    private double calculateTotal(List<Double> prices) {
        double sum = 0;
        for (double p : prices) {
            sum += p;
        }
        return sum;
    }

    /**
     * Shows a simple information dialog with a localized title and the given message.
     * <p>
     * The dialog uses a custom stylesheet for consistent styling.
     *
     * @param msg the message to display to the user
     */

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(tr("messageTitle"));
        a.setHeaderText(null);
        a.setContentText(msg);
        // dialog stylesheet
        DialogPane dp = a.getDialogPane();
        dp.getStylesheets().add(
                getClass().getResource("/otp2/shoppingcartapp/ui/dialog.css").toExternalForm()
        );
        a.showAndWait();
    }

    /**
     * Handles saving the current shopping cart result to the database.
     * <p>
     * If there are no items or the total is zero or less, an error message is shown.
     * Otherwise, the cart result is passed to {@link ShoppingCartResultService}
     * and a confirmation message is displayed. The "Save to DB" button is then disabled.
     *
     * @param e the action event fired by the "Save to DB" button
     */

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
