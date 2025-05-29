package com.example.expensemanager.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Budget;
import com.example.expensemanager.models.Transaction;
import com.example.expensemanager.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetAlertService {
    private Context context;
    private DatabaseHelper databaseHelper;
    private NotificationHelper notificationHelper;
    private SharedPreferences sharedPreferences;

    public BudgetAlertService(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.notificationHelper = new NotificationHelper(context);
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public void checkAllAlerts(int userId) {
        // Kiểm tra nếu thông báo được bật
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        if (!notificationsEnabled) {
            return;
        }

        checkBudgetAlerts(userId);
        checkUnusualSpending(userId);
        checkDailySpendingLimit(userId);
    }

    private void checkBudgetAlerts(int userId) {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        List<Budget> budgets = databaseHelper.getBudgets(userId, currentMonth, currentYear);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = String.format(Locale.getDefault(), "%d-%02d-01", currentYear, currentMonth);
        
        calendar.set(currentYear, currentMonth - 1, 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(calendar.getTime());

        for (Budget budget : budgets) {
            double spentAmount = databaseHelper.getTotalAmountByCategory(
                userId, budget.getCategoryId(), "EXPENSE", startDate, endDate);
            
            double percentage = (spentAmount / budget.getAmount()) * 100;
            
            // Kiểm tra các mức cảnh báo
            String alertKey = "budget_alert_" + budget.getId() + "_" + currentMonth + "_" + currentYear;
            
            if (percentage >= 100) {
                // Vượt ngân sách
                if (!hasAlertBeenShown(alertKey + "_exceeded")) {
                    notificationHelper.showBudgetExceeded(budget.getCategoryName(), spentAmount, budget.getAmount());
                    markAlertAsShown(alertKey + "_exceeded");
                }
            } else if (percentage >= 80) {
                // Cảnh báo 80%
                if (!hasAlertBeenShown(alertKey + "_80")) {
                    notificationHelper.showBudgetAlert(budget.getCategoryName(), spentAmount, budget.getAmount(), percentage);
                    markAlertAsShown(alertKey + "_80");
                }
            }
        }
    }

    private void checkUnusualSpending(int userId) {
        // Lấy giao dịch hôm nay
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = dateFormat.format(today.getTime());
        
        List<Transaction> todayTransactions = databaseHelper.getTransactions(userId, todayStr, todayStr);
        
        // Lấy dữ liệu 30 ngày trước để tính trung bình
        Calendar thirtyDaysAgo = Calendar.getInstance();
        thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30);
        String startDate = dateFormat.format(thirtyDaysAgo.getTime());
        
        List<Transaction> recentTransactions = databaseHelper.getTransactions(userId, startDate, todayStr);
        
        // Tính trung bình chi tiêu theo danh mục
        Map<Integer, Double> categoryAverages = calculateCategoryAverages(recentTransactions);
        
        // Kiểm tra giao dịch hôm nay có bất thường không
        for (Transaction transaction : todayTransactions) {
            if (transaction.getType().equals("EXPENSE")) {
                Double averageAmount = categoryAverages.get(transaction.getCategoryId());
                if (averageAmount != null && transaction.getAmount() > averageAmount * 2) {
                    // Chi tiêu gấp đôi mức bình thường
                    String alertKey = "unusual_spending_" + transaction.getId();
                    if (!hasAlertBeenShown(alertKey)) {
                        notificationHelper.showUnusualSpending(
                            transaction.getAmount(), 
                            transaction.getCategoryName(), 
                            averageAmount
                        );
                        markAlertAsShown(alertKey);
                    }
                }
            }
        }
    }

    private void checkDailySpendingLimit(int userId) {
        // Lấy giới hạn chi tiêu hàng ngày từ settings
        float dailyLimit = sharedPreferences.getFloat("daily_spending_limit", 0);
        if (dailyLimit <= 0) {
            return; // Không có giới hạn được thiết lập
        }

        // Tính tổng chi tiêu hôm nay
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = dateFormat.format(today.getTime());
        
        double todaySpending = databaseHelper.getTotalAmount(userId, "EXPENSE", todayStr, todayStr);
        
        if (todaySpending > dailyLimit) {
            String alertKey = "daily_limit_" + todayStr;
            if (!hasAlertBeenShown(alertKey)) {
                notificationHelper.showDailySpendingLimit(todaySpending, dailyLimit);
                markAlertAsShown(alertKey);
            }
        }
    }

    private Map<Integer, Double> calculateCategoryAverages(List<Transaction> transactions) {
        Map<Integer, Double> categoryTotals = new HashMap<>();
        Map<Integer, Integer> categoryCounts = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("EXPENSE")) {
                int categoryId = transaction.getCategoryId();
                categoryTotals.put(categoryId, 
                    categoryTotals.getOrDefault(categoryId, 0.0) + transaction.getAmount());
                categoryCounts.put(categoryId, 
                    categoryCounts.getOrDefault(categoryId, 0) + 1);
            }
        }
        
        Map<Integer, Double> categoryAverages = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
            int categoryId = entry.getKey();
            double total = entry.getValue();
            int count = categoryCounts.get(categoryId);
            categoryAverages.put(categoryId, total / count);
        }
        
        return categoryAverages;
    }

    private boolean hasAlertBeenShown(String alertKey) {
        return sharedPreferences.getBoolean(alertKey, false);
    }

    private void markAlertAsShown(String alertKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(alertKey, true);
        editor.apply();
    }

    public void resetDailyAlerts() {
        // Reset các alert hàng ngày (gọi vào đầu ngày mới)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Map<String, ?> allPrefs = sharedPreferences.getAll();
        
        for (String key : allPrefs.keySet()) {
            if (key.startsWith("daily_limit_") || key.startsWith("unusual_spending_")) {
                editor.remove(key);
            }
        }
        editor.apply();
    }
}