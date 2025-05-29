package com.example.expensemanager.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.expensemanager.R;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Category;
import com.example.expensemanager.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {
    private EditText editTextAmount, editTextDescription, editTextDate;
    private RadioGroup radioGroupType;
    private RadioButton radioIncome, radioExpense;
    private Spinner spinnerCategory;
    private Button buttonSave, buttonCancel;
    private Toolbar toolbar;
    
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private int userId, transactionId;
    private Calendar selectedDate;
    private List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        initViews();
        setupToolbar();
        loadTransactionData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        radioGroupType = findViewById(R.id.radioGroupType);
        radioIncome = findViewById(R.id.radioIncome);
        radioExpense = findViewById(R.id.radioExpense);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);
        
        selectedDate = Calendar.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chỉnh sửa giao dịch");
        }
    }

    private void loadTransactionData() {
        Intent intent = getIntent();
        transactionId = intent.getIntExtra("transaction_id", -1);
        double amount = intent.getDoubleExtra("amount", 0);
        String type = intent.getStringExtra("type");
        int categoryId = intent.getIntExtra("category_id", -1);
        String description = intent.getStringExtra("description");
        String date = intent.getStringExtra("date");

        // Set amount
        editTextAmount.setText(String.valueOf((int)amount));
        
        // Set type
        if ("INCOME".equals(type)) {
            radioIncome.setChecked(true);
            loadCategories("INCOME");
        } else {
            radioExpense.setChecked(true);
            loadCategories("EXPENSE");
        }
        
        // Set description
        editTextDescription.setText(description);
        
        // Set date
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            selectedDate.setTime(dateFormat.parse(date));
            updateDateDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Set category after loading categories
        setCategorySelection(categoryId);
    }

    private void loadCategories(String type) {
        categoryList = databaseHelper.getCategories(userId, type);
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setCategorySelection(int categoryId) {
        if (categoryList != null) {
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getId() == categoryId) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupClickListeners() {
        editTextDate.setOnClickListener(v -> showDateTimePicker());
        
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioIncome) {
                loadCategories("INCOME");
            } else {
                loadCategories("EXPENSE");
            }
        });
        
        buttonSave.setOnClickListener(v -> saveTransaction());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (timeView, hourOfDay, minute) -> {
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);
                        updateDateDisplay();
                    }, selectedDate.get(Calendar.HOUR_OF_DAY), selectedDate.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        editTextDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void saveTransaction() {
        String amountStr = editTextAmount.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (amountStr.isEmpty()) {
            editTextAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        if (categoryList == null || categoryList.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = radioIncome.isChecked() ? "INCOME" : "EXPENSE";
        int categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();
        
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = dbDateFormat.format(selectedDate.getTime());

        Transaction transaction = new Transaction(transactionId, amount, type, categoryId, description, date, userId);
        boolean result = databaseHelper.updateTransaction(transaction);

        if (result) {
            Toast.makeText(this, "Cập nhật giao dịch thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Cập nhật giao dịch thất bại!", Toast.LENGTH_SHORT).show();
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
