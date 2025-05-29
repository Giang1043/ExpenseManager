package com.example.expensemanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.example.expensemanager.R;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.activities.CategoryManagementActivity;
import com.example.expensemanager.activities.LoginActivity;
import com.example.expensemanager.activities.NotificationActivity;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView textViewUserName, textViewUserEmail, textViewCurrentCurrency;
    private LinearLayout layoutManageCategories, layoutCurrencySettings, layoutNotificationSettings;
    private LinearLayout layoutExportData, layoutBackupData, layoutAboutApp, layoutPrivacyPolicy;
    private SwitchCompat switchNotifications;
    private Button buttonLogout;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private LinearLayout layoutAlertSettings;
    private TextView textViewDailyLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);
        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        loadUserInfo();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewCurrentCurrency = findViewById(R.id.textViewCurrentCurrency);

        layoutManageCategories = findViewById(R.id.layoutManageCategories);
        layoutCurrencySettings = findViewById(R.id.layoutCurrencySettings);
        layoutNotificationSettings = findViewById(R.id.layoutNotificationSettings);
        layoutExportData = findViewById(R.id.layoutExportData);
        layoutBackupData = findViewById(R.id.layoutBackupData);
        layoutAboutApp = findViewById(R.id.layoutAboutApp);
        layoutPrivacyPolicy = findViewById(R.id.layoutPrivacyPolicy);

        switchNotifications = findViewById(R.id.switchNotifications);
        buttonLogout = findViewById(R.id.buttonLogout);
        layoutAlertSettings = findViewById(R.id.layoutAlertSettings);
        textViewDailyLimit = findViewById(R.id.textViewDailyLimit);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadUserInfo() {
        String userName = sharedPreferences.getString("userName", "Người dùng");
        String userEmail = sharedPreferences.getString("userEmail", "email@example.com");

        textViewUserName.setText(userName);
        textViewUserEmail.setText(userEmail);

        // Load notification preference
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);

        // Load và hiển thị daily limit
        float dailyLimit = sharedPreferences.getFloat("daily_spending_limit", 0);
        updateDailyLimitDisplay(dailyLimit);

        // Load currency setting
        String[] currencies = {"Việt Nam Đồng (VND)", "US Dollar (USD)", "Euro (EUR)"};
        int currencySelection = sharedPreferences.getInt("currency_selection", 0);
        textViewCurrentCurrency.setText(currencies[currencySelection]);
    }

    private void setupClickListeners() {
        // Manage Categories
        layoutManageCategories.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryManagementActivity.class));
        });

        // Currency Settings
        layoutCurrencySettings.setOnClickListener(v -> {
            showCurrencyDialog();
        });

        // Notification Settings
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();

            String message = isChecked ? "Đã bật thông báo" : "Đã tắt thông báo";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Export Data
        layoutExportData.setOnClickListener(v -> {
            showExportDialog();
        });

        // Backup Data
        layoutBackupData.setOnClickListener(v -> {
            showBackupDialog();
        });

        // About App
        layoutAboutApp.setOnClickListener(v -> {
            showAboutDialog();
        });

        // Privacy Policy
        layoutPrivacyPolicy.setOnClickListener(v -> {
            showPrivacyDialog();
        });

        // Logout
        buttonLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });

        layoutAlertSettings.setOnClickListener(v -> {
            showAlertSettingsDialog();
        });
    }

    private void showCurrencyDialog() {
        String[] currencies = {"Việt Nam Đồng (VND)", "US Dollar (USD)", "Euro (EUR)"};
        int currentSelection = sharedPreferences.getInt("currency_selection", 0);

        new AlertDialog.Builder(this)
                .setTitle("Chọn đơn vị tiền tệ")
                .setSingleChoiceItems(currencies, currentSelection, (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("currency_selection", which);
                    editor.apply();

                    textViewCurrentCurrency.setText(currencies[which]);
                    Toast.makeText(this, "Đã thay đổi đơn vị tiền tệ", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showExportDialog() {
        String[] options = {"Xuất Excel", "Xuất PDF"};

        new AlertDialog.Builder(this)
                .setTitle("Xuất dữ liệu")
                .setItems(options, (dialog, which) -> {
                    String format = which == 0 ? "Excel" : "PDF";
                    Toast.makeText(this, "Tính năng xuất " + format + " đang được phát triển", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showBackupDialog() {
        String[] options = {"Sao lưu dữ liệu", "Khôi phục dữ liệu"};

        new AlertDialog.Builder(this)
                .setTitle("Quản lý sao lưu")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Backup
                        Toast.makeText(this, "Tính năng sao lưu đang được phát triển", Toast.LENGTH_SHORT).show();
                    } else {
                        // Restore
                        Toast.makeText(this, "Tính năng khôi phục đang được phát triển", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Về ứng dụng")
                .setMessage("Ứng dụng Quản lý Chi tiêu\n\n" +
                        "Phiên bản: 1.0.0\n" +
                        "Phát triển bởi: Your Name\n\n" +
                        "Ứng dụng giúp bạn quản lý thu chi cá nhân một cách hiệu quả.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chính sách bảo mật")
                .setMessage("Ứng dụng cam kết bảo vệ thông tin cá nhân của bạn.\n\n" +
                        "• Dữ liệu được lưu trữ cục bộ trên thiết bị\n" +
                        "• Không chia sẻ thông tin với bên thứ ba\n" +
                        "• Bạn có toàn quyền kiểm soát dữ liệu của mình")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlertSettingsDialog() {
        String[] options = {
                "Thiết lập giới hạn chi tiêu hàng ngày",
                "Cài đặt ngưỡng cảnh báo ngân sách",
                "Cài đặt hệ số chi tiêu bất thường",
                "Bật/tắt các loại cảnh báo",
                "Xem lịch sử thông báo"
        };

        new AlertDialog.Builder(this)
                .setTitle("Cài đặt cảnh báo")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showDailyLimitDialog();
                            break;
                        case 1:
                            showBudgetThresholdDialog();
                            break;
                        case 2:
                            showUnusualSpendingDialog();
                            break;
                        case 3:
                            showToggleAlertsDialog();
                            break;
                        case 4:
                            startActivity(new Intent(this, NotificationActivity.class));
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDailyLimitDialog() {
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setHint("Nhập số tiền (VND)");

        float currentLimit = sharedPreferences.getFloat("daily_spending_limit", 0);
        if (currentLimit > 0) {
            editText.setText(String.valueOf((int)currentLimit));
        }

        new AlertDialog.Builder(this)
                .setTitle("Giới hạn chi tiêu hàng ngày")
                .setMessage("Thiết lập giới hạn chi tiêu tối đa trong một ngày:")
                .setView(editText)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String input = editText.getText().toString().trim();
                    if (!input.isEmpty()) {
                        try {
                            float limit = Float.parseFloat(input);
                            if (limit > 0) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putFloat("daily_spending_limit", limit);
                                editor.apply();

                                updateDailyLimitDisplay(limit);
                                Toast.makeText(this, "Đã thiết lập giới hạn hàng ngày", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa giới hạn", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("daily_spending_limit");
                    editor.apply();
                    updateDailyLimitDisplay(0);
                    Toast.makeText(this, "Đã xóa giới hạn hàng ngày", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void updateDailyLimitDisplay(float limit) {
        if (limit > 0) {
            textViewDailyLimit.setText(String.format("%.0f VND/ngày", limit));
        } else {
            textViewDailyLimit.setText("Chưa thiết lập");
        }
    }

    // Thêm method này vào SettingsActivity.java

    private void showBudgetThresholdDialog() {
        String[] thresholds = {"70%", "80%", "90%", "95%"};
        int[] thresholdValues = {70, 80, 90, 95};

        int currentThreshold = sharedPreferences.getInt("budget_warning_threshold", 80);

        // Tìm index của threshold hiện tại
        int selectedIndex = 1; // Default 80%
        for (int i = 0; i < thresholdValues.length; i++) {
            if (thresholdValues[i] == currentThreshold) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Ngưỡng cảnh báo ngân sách")
                .setMessage("Chọn mức phần trăm để nhận cảnh báo khi chi tiêu đạt ngưỡng này:")
                .setSingleChoiceItems(thresholds, selectedIndex, (dialog, which) -> {
                    int newThreshold = thresholdValues[which];

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("budget_warning_threshold", newThreshold);
                    editor.apply();

                    Toast.makeText(this, "Đã thiết lập ngưỡng cảnh báo " + newThreshold + "%", Toast.LENGTH_SHORT).show();

                    // Cập nhật hiển thị nếu có TextView để hiển thị threshold hiện tại
                    updateBudgetThresholdDisplay(newThreshold);

                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Mặc định (80%)", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("budget_warning_threshold", 80);
                    editor.apply();

                    Toast.makeText(this, "Đã đặt lại ngưỡng cảnh báo về 80%", Toast.LENGTH_SHORT).show();
                    updateBudgetThresholdDisplay(80);
                })
                .show();
    }

    // Method để cập nhật hiển thị threshold hiện tại (tùy chọn)
    private void updateBudgetThresholdDisplay(int threshold) {
        // Hiện tại chỉ hiển thị toast, không cần TextView riêng
        // Nếu sau này bạn muốn thêm TextView để hiển thị threshold trong layout
        // thì có thể uncomment và thêm TextView vào layout

        /*
        TextView textViewBudgetThreshold = findViewById(R.id.textViewBudgetThreshold);
        if (textViewBudgetThreshold != null) {
            textViewBudgetThreshold.setText("Ngưỡng cảnh báo: " + threshold + "%");
        }
        */
    }

    // Method để hiển thị dialog cài đặt hệ số chi tiêu bất thường
    private void showUnusualSpendingDialog() {
        String[] multipliers = {"1.5x", "2.0x", "2.5x", "3.0x"};
        float[] multiplierValues = {1.5f, 2.0f, 2.5f, 3.0f};

        float currentMultiplier = sharedPreferences.getFloat("unusual_spending_multiplier", 2.0f);

        // Tìm index của multiplier hiện tại
        int selectedIndex = 1; // Default 2.0x
        for (int i = 0; i < multiplierValues.length; i++) {
            if (Math.abs(multiplierValues[i] - currentMultiplier) < 0.1f) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Hệ số chi tiêu bất thường")
                .setMessage("Chọn hệ số để xác định chi tiêu bất thường (so với mức trung bình):")
                .setSingleChoiceItems(multipliers, selectedIndex, (dialog, which) -> {
                    float newMultiplier = multiplierValues[which];

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("unusual_spending_multiplier", newMultiplier);
                    editor.apply();

                    Toast.makeText(this, "Đã thiết lập hệ số " + multipliers[which], Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Mặc định (2.0x)", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putFloat("unusual_spending_multiplier", 2.0f);
                    editor.apply();

                    Toast.makeText(this, "Đã đặt lại hệ số về 2.0x", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showToggleAlertsDialog() {
        String[] options = {
                "Bật/tắt cảnh báo ngân sách",
                "Bật/tắt cảnh báo chi tiêu bất thường",
                "Bật/tắt cảnh báo giới hạn hàng ngày"
        };

        new AlertDialog.Builder(this)
                .setTitle("Bật/tắt cảnh báo")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            toggleBudgetAlerts();
                            break;
                        case 1:
                            toggleUnusualSpendingAlerts();
                            break;
                        case 2:
                            toggleDailyLimitAlerts();
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void toggleBudgetAlerts() {
        boolean currentState = sharedPreferences.getBoolean("budget_alerts_enabled", true);
        boolean newState = !currentState;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("budget_alerts_enabled", newState);
        editor.apply();

        String message = newState ? "Đã bật cảnh báo ngân sách" : "Đã tắt cảnh báo ngân sách";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void toggleUnusualSpendingAlerts() {
        boolean currentState = sharedPreferences.getBoolean("unusual_spending_alerts", true);
        boolean newState = !currentState;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("unusual_spending_alerts", newState);
        editor.apply();

        String message = newState ? "Đã bật cảnh báo chi tiêu bất thường" : "Đã tắt cảnh báo chi tiêu bất thường";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void toggleDailyLimitAlerts() {
        boolean currentState = sharedPreferences.getBoolean("daily_limit_alerts", true);
        boolean newState = !currentState;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("daily_limit_alerts", newState);
        editor.apply();

        String message = newState ? "Đã bật cảnh báo giới hạn hàng ngày" : "Đã tắt cảnh báo giới hạn hàng ngày";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}