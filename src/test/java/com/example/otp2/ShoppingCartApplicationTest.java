package com.example.otp2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShoppingCartApplicationTest {

    @Test
    void applicationCanBeConstructedWithoutJavaFxLaunch() {
        assertDoesNotThrow(() -> {
            ShoppingCartApplication app = new ShoppingCartApplication();
            assertNotNull(app);
        });
    }

    // ÄLÄ testaa main()-metodia tässä, koska se käynnistää JavaFX:n ja jumiutuu CI:ssä
}
