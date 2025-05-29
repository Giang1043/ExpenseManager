package com.example.expensemanager.models;

public class Transaction {
    private int id;
    private double amount;
    private String type; // INCOME hoặc EXPENSE
    private int categoryId;
    private String categoryName;
    private String description;
    private String date;
    private int userId;

    public Transaction() {}

    public Transaction(double amount, String type, int categoryId, String description, String date, int userId) {
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.description = description;
        this.date = date;
        this.userId = userId;
    }

    public Transaction(int transactionId,double amount, String type, int categoryId, String description, String date, int userId) {
        this.id = transactionId;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.description = description;
        this.date = date;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}