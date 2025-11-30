package com.example.otp2;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationServiceTest {

    @Test
    void getLocalizedStringsReturnsNonNullMapForEnglish() {
        Locale locale = Locale.ENGLISH;

        Map<String, String> result = LocalizationService.getLocalizedStrings(locale);

        assertNotNull(result, "Palautetun mapin tulee olla ei-null");
        // Emme tee oletuksia siitä, onko tietokanta olemassa vai ei,
        // riittää että metodi ei heitä poikkeusta.
    }

    @Test
    void getLocalizedStringsDoesNotThrowForUnknownLocale() {
        Locale locale = new Locale("xx", "YY");

        assertDoesNotThrow(() -> LocalizationService.getLocalizedStrings(locale));
    }
}
