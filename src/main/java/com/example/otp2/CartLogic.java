package com.example.otp2;

import java.util.List;

public class CartLogic {

    public static double calculateTotal(List<Double> prices) {
        double sum = 0;
        for (double p : prices) {
            sum += p;
        }
        return sum;
    }

    public static boolean isValidCount(String input) {
        try {
            int n = Integer.parseInt(input);
            return n > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
