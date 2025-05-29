package com.example.expensemanager.models;

public class Budget {
    private int id;
    private int categoryId;
    private String categoryName;
    private double amount;
    private int month;
    private int year;
    private int userId;

    public Budget() {}

    public Budget(int categoryId, double amount, int month, int year, int userId) {
        this.categoryId = categoryId;
        this.amount = amount;
        this.month = month;
        this.year = year;
        this.userId = userId;
    }

    public Budget(int budgetId,int categoryId, double amount, int month, int year, int userId) {
        this.id = budgetId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.month = month;
        this.year = year;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}