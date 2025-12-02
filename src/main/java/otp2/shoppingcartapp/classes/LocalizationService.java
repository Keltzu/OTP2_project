package otp2.shoppingcartapp.classes;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.util.*;

/**
 * Service responsible for loading localized strings from the database.
 * <p>
 * Configuration (DB host, port, name, user, password) is read from:
 * <ol>
 *     <li>Real environment variables ({@link System#getenv(String)})</li>
 *     <li>A <code>.env</code> file (via dotenv)</li>
 *     <li>Hard-coded defaults, if neither is set</li>
 * </ol>
 * The localized strings are stored in the <code>localization_strings</code> table.
 */
public class LocalizationService {

    /**
     * Dotenv configuration used to read values from a local .env file.
     * <p>
     * if the file is not present (e.g. in production).
     */
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    /**
     * Returns a configuration value from environment variables or .env file.
     * <p>
     * The lookup order is:
     * <ol>
     *     <li>System environment variable</li>
     *     <li>.env file value</li>
     *     <li>Provided default value</li>
     * </ol>
     *
     * @param key          the environment variable key
     * @param defaultValue the default value to use if none is defined
     * @return the resolved value
     */
    private static String getEnv(String key, String defaultValue) {
        // 1) oikein ympäristömuuttujat (esim. Docker, OS)
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isEmpty()) {
            return fromEnv;
        }
        // 2) .env-tiedosto
        String fromFile = DOTENV.get(key);
        if (fromFile != null && !fromFile.isEmpty()) {
            return fromFile;
        }
        // 3) oletusarvo
        return defaultValue;
    }

    /** Database name used for loading localization strings. */
    private static final String DB_NAME     = getEnv("DB_NAME", "shopping_cart_db");
    /** Database username. */
    private static final String DB_USER     = getEnv("DB_USER", "root");
    /** Database password. */
    private static final String DB_PASSWORD = getEnv("DB_PASSWORD", "");
    /** Database host, typically {@code localhost}. */
    private static final String DB_HOST     = getEnv("DB_HOST", "localhost");
    /** Database port, e.g. {@code 3306}. */
    private static final String DB_PORT     = getEnv("DB_PORT", "3306");

    /**
     * Builds the JDBC URL for the MariaDB database, including common flags.
     *
     * @return JDBC URL string
     */
    private static String getDatabaseUrl() {
        return "jdbc:mariadb://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                + "?useSSL=false&allowPublicKeyRetrieval=true";
    }

    /**
     * Loads localized strings for the given locale from the database.
     * <p>
     * The method queries the <code>localization_strings</code> table
     * and returns all key–value pairs for the given language code.
     *
     * @param locale the locale whose language will be used (e.g. "en", "fr")
     * @return a map of localization key to translated value;
     *         empty map if the database is not reachable or no rows are found
     */
    public static Map<String, String> getLocalizedStrings(Locale locale) {
        Map<String, String> strings = new HashMap<>();
        String lang = locale.getLanguage();
        String dbUrl = getDatabaseUrl();

        try (Connection conn = DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD)) {
            String query = "SELECT `key`, value FROM localization_strings WHERE language = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, lang);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    strings.put(rs.getString("key"), rs.getString("value"));
                }
            }
            System.out.println("✅ Loaded localization strings for language: " + lang + " from " + dbUrl);
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + dbUrl);
        }

        return strings;
    }
}
