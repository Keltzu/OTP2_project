package otp2.shoppingcartapp.classes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX application entry point for the Shopping Cart application.
 * <p>
 * This class:
 * <ul>
 *     <li>Loads the main FXML layout</li>
 *     <li>Applies the global CSS stylesheet</li>
 *     <li>Creates and shows the primary stage</li>
 * </ul>
 */
public class ShoppingCartApplication extends Application {

    /**
     * Initializes and displays the primary JavaFX stage.
     * <p>
     * The layout is defined in {@code cart-view.fxml} and styled
     * using {@code cart.css}.
     *
     * @param stage the primary stage provided by the JavaFX runtime
     * @throws IOException if loading the FXML fails
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                ShoppingCartApplication.class.getResource("/otp2/shoppingcartapp/ui/cart-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        ShoppingCartApplication.class.getResource("/otp2/shoppingcartapp/ui/cart.css")
                ).toExternalForm()
        );
        stage.setTitle("Shopping Cart App");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Standard Java main method that launches the JavaFX application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        launch();
    }
}
