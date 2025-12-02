package otp2.shoppingcartapp.classes;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ShoppingCartResultServiceTest {
    @Test
    void testSaveCartNullCustomer() {
        assertDoesNotThrow(() ->
                ShoppingCartResultService.saveCartResult(
                        List.of(1.0, 2.0), 3.0, "en", null
                )
        );
    }

    @Test
    void saveCartResultDoesNotThrowWithNullCustomerId() {
        assertDoesNotThrow(() ->
                ShoppingCartResultService.saveCartResult(
                        Arrays.asList(1.0, 2.0, 3.0),
                        6.0,
                        "en",
                        null          // haaran: customerId == null
                )
        );
    }

    @Test
    void saveCartResultDoesNotThrowWithCustomerId() {
        assertDoesNotThrow(() ->
                ShoppingCartResultService.saveCartResult(
                        Arrays.asList(10.0, 20.0),
                        30.0,
                        "fr",
                        123           // haaran: customerId != null
                )
        );
    }

    @Test
    void saveCartResultDoesNotThrowWithEmptyPrices() {
        assertDoesNotThrow(() ->
                ShoppingCartResultService.saveCartResult(
                        Collections.emptyList(), // item_count = 0
                        0.0,
                        "en",
                        null
                )
        );
    }
}
