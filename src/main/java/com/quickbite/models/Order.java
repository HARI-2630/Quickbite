package com.quickbite.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private int restaurantId;
    private double total;
    private String status; // PLACED, PREPARING, OUT_FOR_DELIVERY, ARRIVING, DELIVERED
    private Timestamp createdAt;
    
    // Virtual fields
    private String restaurantName;
    private String customerName;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(int id, int userId, int restaurantId, double total, String status, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRestaurantId() { return restaurantId; }
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getRestaurantName() { return restaurantName; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
