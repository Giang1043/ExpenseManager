package com.example.expensemanager.models;

public class NotificationItem {
    private int id;
    private String title;
    private String message;
    private String type; // BUDGET_WARNING, BUDGET_EXCEEDED, UNUSUAL_SPENDING, DAILY_LIMIT
    private String timestamp;
    private boolean isRead;
    private int priority; // 1: High, 2: Medium, 3: Low

    public NotificationItem() {}

    public NotificationItem(String title, String message, String type, String timestamp, int priority) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.priority = priority;
        this.isRead = false;
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

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getIcon() {
        switch (type) {
            case "BUDGET_WARNING":
                return "⚠️";
            case "BUDGET_EXCEEDED":
                return "🚨";
            case "UNUSUAL_SPENDING":
                return "💰";
            case "DAILY_LIMIT":
                return "📊";
            default:
                return "🔔";
        }
    }

    public int getColorResource() {
        switch (priority) {
            case 1: // High
                return android.R.color.holo_red_light;
            case 2: // Medium
                return android.R.color.holo_orange_light;
            case 3: // Low
                return android.R.color.holo_blue_light;
            default:
                return android.R.color.darker_gray;
        }
    }
}