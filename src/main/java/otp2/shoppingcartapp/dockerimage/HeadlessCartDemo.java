package otp2.shoppingcartapp.dockerimage;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Headless (console) demo for the Shopping Cart app WITHOUT database.
 * Shows language switching using MessagesBundle and a simple cart total.
 *
 * Running a full JavaFX GUI inside Linux containers on Windows caused graphics pipeline / toolkit errors (QuantumRenderer).
 * For Docker I implemented a headless version that reuses the same localization resources and cart logic
 * , which is a realistic way to containerize a desktop app’s core functionality.
 */

public class HeadlessCartDemo {

    public static void main(String[] args) {
        System.out.println("=== Shopping Cart Demo ===");

        // 1) Show how languages work using the same MessagesBundle as the GUI
        Locale[] locales = {
                new Locale("en", "US"),
                new Locale("fr", "FR"),
                new Locale("ur", "PK"),
                new Locale("vi", "VN")
        };

        for (Locale locale : locales) {
            ResourceBundle rb = ResourceBundle.getBundle("MessagesBundle", locale);

            System.out.println("\n--- " + locale.toLanguageTag() + " ---");
            System.out.println("title: " + rb.getString("title"));
            System.out.println("selectLanguage: " + rb.getString("selectLanguage"));
            System.out.println("enterItemsCount: " + rb.getString("enterItemsCount"));
        }

        // 2) Simulate the cart logic (3 items with prices)
        List<Double> prices = Arrays.asList(1.99, 2.49, 3.50);
        double total = 0.0;
        for (double p : prices) {
            total += p;
        }

        System.out.println("\nCart items:");
        for (int i = 0; i < prices.size(); i++) {
            System.out.printf("  Item %d: %.2f €%n", i + 1, prices.get(i));
        }
        System.out.printf("Total: %.2f €%n", total);

        System.out.println("\n=== End of demo ===");
    }
}
