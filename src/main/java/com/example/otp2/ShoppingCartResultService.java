package com.example.otp2;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.List;

public class ShoppingCartResultService {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

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

    private static final String DB_HOST = getEnv("DB_HOST", "localhost");
    private static final String DB_PORT = getEnv("DB_PORT", "3306");
    private static final String DB_NAME = getEnv("DB_NAME", "shopping_cart_db");
    private static final String DB_USER = getEnv("DB_USER", "root");
    private static final String DB_PASSWORD = getEnv("DB_PASSWORD", "12345");

    private static final String DB_URL =
            "jdbc:mariadb://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;


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

            //  luodaan ostoskoritulos
            int cartResultId;

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
