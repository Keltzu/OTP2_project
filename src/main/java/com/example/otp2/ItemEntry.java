package com.example.otp2;


public class ItemEntry {
    private final double price;
    private final int quantity;

    public ItemEntry(double price, int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}

