package com.example.ballzbeta.objects;

import java.util.List;

public class Warehouse {
    private int id;
    private List<WarehouseItem> warehouseItemList;

    public Warehouse() {}

    public Warehouse(int id, List<WarehouseItem> warehouseItemList) {
        this.id = id;
        this.warehouseItemList = warehouseItemList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<WarehouseItem> getWarehouseItemList() {
        return warehouseItemList;
    }

    public void setWarehouseItemList(List<WarehouseItem> warehouseItemList) {
        this.warehouseItemList = warehouseItemList;
    }
}
