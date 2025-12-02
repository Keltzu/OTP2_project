package otp2.shoppingcartapp.classes;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartLogicTest {

    @Test
    void testCalculateTotal() {
        double total = CartLogic.calculateTotal(List.of(1.0, 2.5, 3.5));
        assertEquals(7.0, total, 0.0001);
    }

    @Test
    void testCalculateTotalEmpty() {
        double total = CartLogic.calculateTotal(List.of());
        assertEquals(0.0, total);
    }

    @Test
    void testValidCount() {
        assertTrue(CartLogic.isValidCount("3"));
        assertFalse(CartLogic.isValidCount("-1"));
        assertFalse(CartLogic.isValidCount("abc"));
    }
}
