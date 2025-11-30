package com.example.otp2;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.util.*;

public class LocalizationService {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing() // jos .env puuttuu, ei kaadu
            .load();

    private static String getEnv(String key, String defaultValue) {
        // 1) oikeat ympäristömuuttujat (esim. Docker, OS)
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

    private static final String DB_NAME     = getEnv("DB_NAME", "shopping_cart_db");
    private static final String DB_USER     = getEnv("DB_USER", "root");
    private static final String DB_PASSWORD = getEnv("DB_PASSWORD", "");
    private static final String DB_HOST     = getEnv("DB_HOST", "localhost");
    private static final String DB_PORT     = getEnv("DB_PORT", "3306");

    private static String getDatabaseUrl() {
        return "jdbc:mariadb://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                + "?useSSL=false&allowPublicKeyRetrieval=true";
    }

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

