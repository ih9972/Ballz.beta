package com.example.ballzbeta.objects;

public class StockItem {
    private String name;
    private int amount;
    private int imageResId;

    public StockItem(String name, int amount, int imageResId) {
        this.name = name;
        this.amount = amount;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public int getAmount() { return amount; }
    public int getImageResId() { return imageResId; }
}
