package com.example.ballzbeta.objects;

public class WarehouseItem {
    private int id;
    private Item item;
    private int present;
    private int total;

    public WarehouseItem() {}

    public WarehouseItem(int id, Item item, int present, int total) {
        this.id = id;
        this.item = item;
        this.present = present;
        this.total = total;
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

    public int getPresent() {
        return present;
    }

    public void setPresent(int present) {
        this.present = present;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
