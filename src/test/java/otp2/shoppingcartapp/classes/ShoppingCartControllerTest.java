package otp2.shoppingcartapp.classes;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShoppingCartControllerTest {

    @Test
    void controllerCanBeConstructed() {
        ShoppingCartController controller = new ShoppingCartController();
        assertNotNull(controller);
    }

    @Test
    void calculateTotalReturnsZeroForEmptyList() throws Exception {
        ShoppingCartController controller = new ShoppingCartController();

        Method m = ShoppingCartController.class
                .getDeclaredMethod("calculateTotal", List.class);
        m.setAccessible(true);

        double result = (double) m.invoke(controller, Collections.emptyList());

        assertEquals(0.0, result, 0.0001);
    }

    @Test
    void calculateTotalSumsListCorrectly() throws Exception {
        ShoppingCartController controller = new ShoppingCartController();

        Method m = ShoppingCartController.class
                .getDeclaredMethod("calculateTotal", List.class);
        m.setAccessible(true);

        double result = (double) m.invoke(controller, Arrays.asList(1.0, 2.5, 3.5));

        assertEquals(7.0, result, 0.0001);
    }
}
