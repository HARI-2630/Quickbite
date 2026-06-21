package com.quickbite.models;

public class Restaurant {
    private int id;
    private String name;
    private String cuisine;
    private int ownerId;
    private String status; // OPEN, CLOSED

    public Restaurant() {}

    public Restaurant(int id, String name, String cuisine, int ownerId, String status) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.ownerId = ownerId;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
