package com.example.expensemanager.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.expensemanager.R;
import com.example.expensemanager.activities.MainActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "expense_alerts";
    private static final String CHANNEL_NAME = "Cảnh báo chi tiêu";
    private static final String CHANNEL_DESCRIPTION = "Thông báo về ngân sách và chi tiêu bất thường";
    
    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showBudgetAlert(String categoryName, double spentAmount, double budgetAmount, double percentage) {
        String title = "⚠️ Cảnh báo ngân sách";
        String message = String.format("Danh mục '%s' đã chi %.0f%% ngân sách (%.0f/%.0f VND)", 
            categoryName, percentage, spentAmount, budgetAmount);

        showNotification(1, title, message);
    }

    public void showBudgetExceeded(String categoryName, double spentAmount, double budgetAmount) {
        String title = "🚨 Vượt ngân sách";
        String message = String.format("Danh mục '%s' đã vượt ngân sách! Chi %.0f/%.0f VND", 
            categoryName, spentAmount, budgetAmount);

        showNotification(2, title, message);
    }

    public void showUnusualSpending(double amount, String categoryName, double averageAmount) {
        String title = "💰 Chi tiêu bất thường";
        String message = String.format("Chi tiêu %.0f VND cho '%s' cao hơn bình thường (TB: %.0f VND)", 
            amount, categoryName, averageAmount);

        showNotification(3, title, message);
    }

    public void showDailySpendingLimit(double todaySpending, double dailyLimit) {
        String title = "📊 Giới hạn chi tiêu hàng ngày";
        String message = String.format("Hôm nay đã chi %.0f VND, vượt giới hạn %.0f VND", 
            todaySpending, dailyLimit);

        showNotification(4, title, message);
    }

    private void showNotification(int notificationId, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }
}