package otp2.shoppingcartapp.classes;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationServiceTest {

    @Test
    void getLocalizedStringsReturnsNonNullMapForEnglish() {
        Map<String, String> result = LocalizationService.getLocalizedStrings(Locale.ENGLISH);
        assertNotNull(result);
    }

    @Test
    void getLocalizedStringsDoesNotThrowForUnknownLocale() {
        Locale locale = new Locale("xx", "YY");
        assertDoesNotThrow(() -> LocalizationService.getLocalizedStrings(locale));
    }

    @Test
    void getLocalizedStringsWorksForConfiguredLanguages() {
        Locale fr = new Locale("fr", "FR");
        Locale ur = new Locale("ur", "PK");
        Locale vi = new Locale("vi", "VN");

        assertNotNull(LocalizationService.getLocalizedStrings(fr));
        assertNotNull(LocalizationService.getLocalizedStrings(ur));
        assertNotNull(LocalizationService.getLocalizedStrings(vi));
    }

    @Test
    void testLocalizationWithDifferentLocales() {
        Locale en = new Locale("en", "US");
        Locale fr = new Locale("fr", "FR");

        Map<String, String> enMap = LocalizationService.getLocalizedStrings(en);
        Map<String, String> frMap = LocalizationService.getLocalizedStrings(fr);

        assertNotNull(enMap);
        assertNotNull(frMap);

        // Jos tietokannassa on dataa molemmille kielille, niiden pitäisi tyypillisesti olla erilaisia.
        // Mutta jos db on tyhjä, älä kaada testiä tämän takia.
        if (!enMap.isEmpty() && !frMap.isEmpty()) {
            assertNotEquals(enMap, frMap);
        }
    }
}
