package com.quickbite.models;

public class CartItem {
    private int id;
    private int userId;
    private int menuItemId;
    private int quantity;
    
    // Virtual fields
    private String menuItemName;
    private double price;
    private double totalPrice;

    public CartItem() {}

    public CartItem(int id, int userId, int menuItemId, int quantity) {
        this.id = id;
        this.userId = userId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMenuItemId() { return menuItemId; }
    public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getMenuItemName() { return menuItemName; }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { 
        this.price = price; 
        this.totalPrice = this.price * this.quantity;
    }

    public double getTotalPrice() { return totalPrice; }
}
