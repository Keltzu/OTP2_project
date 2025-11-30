package com.example.otp2;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ShoppingCartResultServiceTest {

    @Test
    void saveCartResultDoesNotThrowEvenWithoutDatabase() {
        assertDoesNotThrow(() ->
                ShoppingCartResultService.saveCartResult(
                        Arrays.asList(1.0, 2.0, 3.0),
                        6.0,
                        "en",
                        null
                )
        );
    }
}
