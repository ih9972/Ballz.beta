package com.example.ballzbeta.objects;

public class OrderItem {
    private int id;
    private Item item;
    private int amount;
    private int fulfilledAmount;

    public OrderItem() {}

    public OrderItem(int id, Item item, int amount) {
        this.id = id;
        this.item = item;
        this.amount = amount;
    }
    public int getFulfilledAmount() {
        return fulfilledAmount;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getAmount() {
        return amount;
    }

    public void setFulfilledAmount(int fulfilledAmount) {
        this.fulfilledAmount = fulfilledAmount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
