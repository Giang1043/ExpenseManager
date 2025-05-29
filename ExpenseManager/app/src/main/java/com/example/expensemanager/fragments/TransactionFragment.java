package com.example.expensemanager.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.activities.MainActivity;
import com.example.expensemanager.activities.NotificationActivity;
import com.example.expensemanager.adapters.TransactionAdapter;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Category;
import com.example.expensemanager.models.Transaction;
import com.example.expensemanager.services.BudgetAlertService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionFragment extends Fragment {
    private RecyclerView recyclerViewTransactions;
    private FloatingActionButton fabAddTransaction;
    private TransactionAdapter transactionAdapter;
    private DatabaseHelper databaseHelper;
    private int userId;
    private List<Transaction> transactionList;

    // Add transaction views
    private View addTransactionLayout;
    private EditText editTextAmount, editTextDescription, editTextDate;
    private RadioGroup radioGroupType;
    private RadioButton radioIncome, radioExpense;
    private Spinner spinnerCategory;
    private Button buttonSave, buttonCancel;
    private Calendar selectedDate;
    private List<Category> categoryList;
    private BudgetAlertService budgetAlertService;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        databaseHelper = mainActivity.getDatabaseHelper();
        userId = mainActivity.getUserId();

        // Initialize SharedPreferences
        sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        budgetAlertService = new BudgetAlertService(getContext());

        initViews(view);
        setupRecyclerView();
        loadTransactions();

        fabAddTransaction.setOnClickListener(v -> showAddTransactionForm());

        return view;
    }

    private void initViews(View view) {
        recyclerViewTransactions = view.findViewById(R.id.recyclerViewTransactions);
        fabAddTransaction = view.findViewById(R.id.fabAddTransaction);
        addTransactionLayout = view.findViewById(R.id.addTransactionLayout);

        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextDate = view.findViewById(R.id.editTextDate);
        radioGroupType = view.findViewById(R.id.radioGroupType);
        radioIncome = view.findViewById(R.id.radioIncome);
        radioExpense = view.findViewById(R.id.radioExpense);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        selectedDate = Calendar.getInstance();
        updateDateDisplay();

        editTextDate.setOnClickListener(v -> showDateTimePicker());
        buttonSave.setOnClickListener(v -> saveTransaction());
        buttonCancel.setOnClickListener(v -> hideAddTransactionForm());

        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioIncome) {
                loadCategories("INCOME");
            } else {
                loadCategories("EXPENSE");
            }
        });
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList, getContext());
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTransactions.setAdapter(transactionAdapter);
    }

    private void loadTransactions() {
        // Load transactions for current month
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dateFormat.format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(calendar.getTime());

        transactionList.clear();
        transactionList.addAll(databaseHelper.getTransactions(userId, startDate, endDate));
        transactionAdapter.notifyDataSetChanged();
    }

    private void showAddTransactionForm() {
        addTransactionLayout.setVisibility(View.VISIBLE);
        fabAddTransaction.setVisibility(View.GONE);
        loadCategories("EXPENSE"); // Default to expense
    }

    private void hideAddTransactionForm() {
        addTransactionLayout.setVisibility(View.GONE);
        fabAddTransaction.setVisibility(View.VISIBLE);
        clearForm();
    }

    private void loadCategories(String type) {
        categoryList = databaseHelper.getCategories(userId, type);
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
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
            Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = radioIncome.isChecked() ? "INCOME" : "EXPENSE";
        int categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();

        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = dbDateFormat.format(selectedDate.getTime());

        Transaction transaction = new Transaction(amount, type, categoryId, description, date, userId);
        long result = databaseHelper.addTransaction(transaction);

        if (result != -1) {
            Toast.makeText(getContext(), "Thêm giao dịch thành công!", Toast.LENGTH_SHORT).show();

            // Kiểm tra cảnh báo ngay sau khi thêm giao dịch
            if (type.equals("EXPENSE")) {
                budgetAlertService.checkAllAlerts(userId);
            }

            hideAddTransactionForm();
            loadTransactions();
        } else {
            Toast.makeText(getContext(), "Thêm giao dịch thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editTextAmount.setText("");
        editTextDescription.setText("");
        radioExpense.setChecked(true);
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
    }

    private void showAlertSettingsDialog() {
        String[] options = {
                "Thiết lập giới hạn chi tiêu hàng ngày",
                "Cài đặt ngưỡng cảnh báo ngân sách",
                "Xem lịch sử thông báo"
        };

        new AlertDialog.Builder(requireContext())
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
                            startActivity(new Intent(getContext(), NotificationActivity.class));
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDailyLimitDialog() {
        EditText editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setHint("Nhập số tiền (VND)");

        float currentLimit = sharedPreferences.getFloat("daily_spending_limit", 0);
        if (currentLimit > 0) {
            editText.setText(String.valueOf((int)currentLimit));
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Giới hạn chi tiêu hàng ngày")
                .setMessage("Thiết lập giới hạn chi tiêu tối đa trong một ngày:")
                .setView(editText)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String input = editText.getText().toString().trim();
                    if (!input.isEmpty()) {
                        float limit = Float.parseFloat(input);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat("daily_spending_limit", limit);
                        editor.apply();

                        Toast.makeText(getContext(), "Đã thiết lập giới hạn hàng ngày", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa giới hạn", (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("daily_spending_limit");
                    editor.apply();
                    Toast.makeText(getContext(), "Đã xóa giới hạn hàng ngày", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showBudgetThresholdDialog() {
        String[] thresholds = {"70%", "80%", "90%", "95%"};
        int currentThreshold = sharedPreferences.getInt("budget_warning_threshold", 80);

        // Tìm index của threshold hiện tại
        int selectedIndex = 1; // Default 80%
        switch (currentThreshold) {
            case 70: selectedIndex = 0; break;
            case 80: selectedIndex = 1; break;
            case 90: selectedIndex = 2; break;
            case 95: selectedIndex = 3; break;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Ngưỡng cảnh báo ngân sách")
                .setMessage("Chọn mức phần trăm để nhận cảnh báo khi chi tiêu đạt ngưỡng này:")
                .setSingleChoiceItems(thresholds, selectedIndex, (dialog, which) -> {
                    int newThreshold = 80; // Default
                    switch (which) {
                        case 0: newThreshold = 70; break;
                        case 1: newThreshold = 80; break;
                        case 2: newThreshold = 90; break;
                        case 3: newThreshold = 95; break;
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("budget_warning_threshold", newThreshold);
                    editor.apply();

                    Toast.makeText(getContext(), "Đã thiết lập ngưỡng cảnh báo " + newThreshold + "%", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}