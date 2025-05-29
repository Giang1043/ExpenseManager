package com.example.expensemanager.models;

public class Notification {
    private int id;
    private String title;
    private String message;
    private String type; // BUDGET_WARNING, BUDGET_EXCEEDED, UNUSUAL_SPENDING, DAILY_LIMIT
    private String date;
    private boolean isRead;
    private int userId;

    public Notification() {}

    public Notification(String title, String message, String type, String date, int userId) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.date = date;
        this.userId = userId;
        this.isRead = false;
    }

    public Notification(int id, String title, String message, String type, String date, boolean isRead, int userId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.date = date;
        this.isRead = isRead;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
