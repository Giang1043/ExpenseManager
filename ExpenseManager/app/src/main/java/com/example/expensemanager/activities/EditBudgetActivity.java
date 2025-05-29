package com.example.expensemanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.expensemanager.R;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Budget;
import com.example.expensemanager.models.Category;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditBudgetActivity extends AppCompatActivity {
    private EditText editTextAmount;
    private Spinner spinnerCategory, spinnerMonth, spinnerYear;
    private Button buttonSave, buttonCancel;
    private Toolbar toolbar;
    
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private int userId, budgetId;
    private List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_budget);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        initViews();
        setupToolbar();
        setupSpinners();
        loadBudgetData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        editTextAmount = findViewById(R.id.editTextAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chỉnh sửa ngân sách");
        }
    }

    private void setupSpinners() {
        // Setup category spinner
        categoryList = databaseHelper.getCategories(userId, "EXPENSE");
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Setup month spinner
        String[] months = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Setup year spinner
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 1; i <= currentYear + 2; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void loadBudgetData() {
        Intent intent = getIntent();
        budgetId = intent.getIntExtra("budget_id", -1);
        int categoryId = intent.getIntExtra("category_id", -1);
        double amount = intent.getDoubleExtra("amount", 0);
        int month = intent.getIntExtra("month", 1);
        int year = intent.getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR));

        // Set amount
        editTextAmount.setText(String.valueOf((int)amount));
        
        // Set category
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId() == categoryId) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        
        // Set month
        spinnerMonth.setSelection(month - 1);
        
        // Set year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int yearIndex = year - (currentYear - 1);
        if (yearIndex >= 0 && yearIndex < spinnerYear.getCount()) {
            spinnerYear.setSelection(yearIndex);
        }
    }

    private void setupClickListeners() {
        buttonSave.setOnClickListener(v -> saveBudget());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void saveBudget() {
        String amountStr = editTextAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            editTextAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        if (categoryList == null || categoryList.isEmpty()) {
            Toast.makeText(this, "Không có danh mục nào", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = Integer.parseInt(spinnerYear.getSelectedItem().toString());

        Budget budget = new Budget(budgetId, categoryId, amount, month, year, userId);
        boolean result = databaseHelper.updateBudget(budget);

        if (result) {
            Toast.makeText(this, "Cập nhật ngân sách thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Cập nhật ngân sách thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
