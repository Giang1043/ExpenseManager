package com.example.expensemanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.expensemanager.R;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.fragments.DashboardFragment;
import com.example.expensemanager.fragments.TransactionFragment;
import com.example.expensemanager.fragments.ReportFragment;
import com.example.expensemanager.fragments.BudgetFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private DatabaseHelper databaseHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra đăng nhập
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = sharedPreferences.getInt("userId", -1);
        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupBottomNavigation();

        // Load dashboard fragment by default
        loadFragment(new DashboardFragment());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý chi tiêu");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                String title = "";

                if (item.getItemId() == R.id.nav_dashboard) {
                    fragment = new DashboardFragment();
                    title = "Tổng quan";
                } else if (item.getItemId() == R.id.nav_transaction) {
                    fragment = new TransactionFragment();
                    title = "Giao dịch";
                } else if (item.getItemId() == R.id.nav_report) {
                    fragment = new ReportFragment();
                    title = "Báo cáo";
                } else if (item.getItemId() == R.id.nav_budget) {
                    fragment = new BudgetFragment();
                    title = "Ngân sách";
                }


                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }

                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_categories) {
            startActivity(new Intent(this, CategoryManagementActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            // TODO: Implement settings
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            showLogoutDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

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
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public int getUserId() {
        return userId;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}