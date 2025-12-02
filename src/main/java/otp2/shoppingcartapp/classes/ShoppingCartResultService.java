package otp2.shoppingcartapp.classes;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.List;

/**
 * Service responsible for persisting shopping cart results into the database.
 * <p>
 * Configuration is loaded from environment variables or a <code>.env</code> file.
 * The data is written to the tables:
 * <ul>
 *     <li><code>cart_results</code> – one row per saved cart</li>
 *     <li><code>cart_items</code> – one row per item in a cart</li>
 * </ul>
 */
public class ShoppingCartResultService {

    /**
     * Dotenv configuration used for reading DB configuration from a .env file.
     */
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    /**
     * Resolves configuration values in the following order:
     * <ol>
     *     <li>System environment variable</li>
     *     <li>.env file</li>
     *     <li>Default value</li>
     * </ol>
     *
     * @param key          the configuration key
     * @param defaultValue default value if no other value is found
     * @return resolved configuration value
     */
    private static String getEnv(String key, String defaultValue) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isEmpty()) {
            return fromEnv;
        }
        String fromFile = DOTENV.get(key);
        if (fromFile != null && !fromFile.isEmpty()) {
            return fromFile;
        }
        return defaultValue;
    }

    /** Database host name. */
    private static final String DB_HOST = getEnv("DB_HOST", "localhost");
    /** Database port. */
    private static final String DB_PORT = getEnv("DB_PORT", "3306");
    /** Database name used to store shopping cart results. */
    private static final String DB_NAME = getEnv("DB_NAME", "shopping_cart_db");
    /** Database username. */
    private static final String DB_USER = getEnv("DB_USER", "root");
    /** Database password. */
    private static final String DB_PASSWORD = getEnv("DB_PASSWORD", "");

    /**
     * JDBC URL used for connecting to the MariaDB database.
     */
    private static final String DB_URL =
            "jdbc:mariadb://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    /**
     * Persists a completed shopping cart into the database.
     * <p>
     * The method:
     * <ol>
     *     <li>Inserts one row into <code>cart_results</code> with the total price,
     *     language and item count</li>
     *     <li>Retrieves the generated cart ID</li>
     *     <li>Inserts one row per item into <code>cart_items</code></li>
     * </ol>
     *
     * @param prices      list of individual item prices
     * @param totalPrice  total price of the cart
     * @param language    language code in which the cart was created (e.g. "en")
     * @param customerId  optional customer ID; may be {@code null} for anonymous carts
     */
    public static void saveCartResult(List<Double> prices,
                                      double totalPrice,
                                      String language,
                                      Integer customerId) {

        String insertResultSql =
                "INSERT INTO cart_results (customer_id, total_price, language, item_count) " +
                        "VALUES (?, ?, ?, ?)";

        String insertItemSql =
                "INSERT INTO cart_items (cart_result_id, item_index, price) " +
                        "VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // luodaan ostoskoritulos
            int cartResultId;

            // Insert root cart result and get its generated ID
            try (PreparedStatement stmt = conn.prepareStatement(
                    insertResultSql, Statement.RETURN_GENERATED_KEYS)) {

                if (customerId != null) {
                    stmt.setInt(1, customerId);
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }

                stmt.setDouble(2, totalPrice);
                stmt.setString(3, language);
                stmt.setInt(4, prices.size());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        cartResultId = rs.getInt(1);
                    } else {
                        throw new SQLException("No generated key for cart_results");
                    }
                }
            }

            // tallennetaan jokainen itemi cart_items-tauluun
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                for (int i = 0; i < prices.size(); i++) {
                    itemStmt.setInt(1, cartResultId);
                    itemStmt.setInt(2, i + 1);
                    itemStmt.setDouble(3, prices.get(i));
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            System.out.println("✅ Shopping cart result + items saved to database at " + DB_URL);

        } catch (SQLException e) {
            System.err.println("❌ Failed to connect/save shopping cart at " + DB_URL);
        }
    }
}
