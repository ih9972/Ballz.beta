package com.example.ballzbeta.objects;

import java.util.List;

public class Order {
    private int orderId;
    private int sender;
    private int receiver;
    private String remark;
    private String dateTime;
    private boolean done;
    private boolean seen;
    private List<OrderItem> orderItemList;

    public Order() {}

    public Order(int orderId, int sender, int receiver, String remark, String dateTime,
                 boolean done, boolean seen, List<OrderItem> orderItemList) {
        this.orderId = orderId;
        this.sender = sender;
        this.receiver = receiver;
        this.remark = remark;
        this.dateTime = dateTime;
        this.done = done;
        this.seen = seen;
        this.orderItemList = orderItemList;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}