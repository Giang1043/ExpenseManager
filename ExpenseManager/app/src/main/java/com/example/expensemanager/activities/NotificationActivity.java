package com.example.expensemanager.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.adapters.NotificationAdapter;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {
    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private DatabaseHelper databaseHelper;
    private List<Notification> notificationList;
    private TextView textViewEmpty;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông báo");
        }

        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        databaseHelper = new DatabaseHelper(this);
        
        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        textViewEmpty = findViewById(R.id.textViewEmpty);
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList, this);
        notificationAdapter.setOnNotificationClickListener(this);
        
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(notificationAdapter);
    }

    private void loadNotifications() {
        notificationList.clear();
        notificationList.addAll(databaseHelper.getNotifications(userId));
        
        if (notificationList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewNotifications.setVisibility(View.VISIBLE);
        }
        
        notificationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read if not already read
        if (!notification.isRead()) {
            databaseHelper.markNotificationAsRead(notification.getId());
            notification.setRead(true);
            notificationAdapter.notifyDataSetChanged();
        }

        // Show detailed notification dialog
        showNotificationDetails(notification);
    }

    @Override
    public void onNotificationLongClick(Notification notification) {
        showNotificationOptions(notification);
    }

    private void showNotificationDetails(Notification notification) {
        new AlertDialog.Builder(this)
                .setTitle(notification.getTitle())
                .setMessage(notification.getMessage())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showNotificationOptions(Notification notification) {
        String[] options = {"Xóa thông báo", "Đánh dấu chưa đọc"};
        
        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            deleteNotification(notification);
                            break;
                        case 1:
                            markAsUnread(notification);
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNotification(Notification notification) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    databaseHelper.deleteNotification(notification.getId());
                    loadNotifications();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void markAsUnread(Notification notification) {
        databaseHelper.markNotificationAsUnread(notification.getId());
        notification.setRead(false);
        notificationAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_mark_all_read) {
            markAllAsRead();
            return true;
        } else if (item.getItemId() == R.id.action_delete_all) {
            deleteAllNotifications();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    private void markAllAsRead() {
        databaseHelper.markAllNotificationsAsRead(userId);
        loadNotifications();
    }

    private void deleteAllNotifications() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn xóa tất cả thông báo?")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    databaseHelper.deleteAllNotifications(userId);
                    loadNotifications();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
