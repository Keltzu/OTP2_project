package com.example.otp2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ShoppingCartApplicationTest {

    @Test
    void applicationCanBeConstructed() {
        ShoppingCartApplication app = new ShoppingCartApplication();
        assertNotNull(app);
    }

    @Test
    void mainMethodDoesNotThrowImmediately() {
        // EI kutsuta JavaFX Application.launchia suoraan testistä,
        // mutta varmistetaan että main-metodi on turvallinen kutsua.
        assertDoesNotThrow(() -> ShoppingCartApplication.main(new String[0]));
    }
}
