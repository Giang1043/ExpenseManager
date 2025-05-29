package com.example.expensemanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.activities.MainActivity;
import com.example.expensemanager.adapters.BudgetAdapter;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Budget;
import com.example.expensemanager.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetFragment extends Fragment {
    private RecyclerView recyclerViewBudgets;
    private FloatingActionButton fabAddBudget;
    private BudgetAdapter budgetAdapter;
    private DatabaseHelper databaseHelper;
    private int userId;
    private List<Budget> budgetList;

    // Add budget views
    private View addBudgetLayout;
    private Spinner spinnerCategory, spinnerMonth, spinnerYear;
    private EditText editTextBudgetAmount;
    private Button buttonSaveBudget, buttonCancelBudget;
    private List<Category> categoryList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        databaseHelper = mainActivity.getDatabaseHelper();
        userId = mainActivity.getUserId();

        initViews(view);
        setupRecyclerView();
        setupSpinners();
        loadBudgets();

        fabAddBudget.setOnClickListener(v -> showAddBudgetForm());

        return view;
    }

    private void initViews(View view) {
        recyclerViewBudgets = view.findViewById(R.id.recyclerViewBudgets);
        fabAddBudget = view.findViewById(R.id.fabAddBudget);
        addBudgetLayout = view.findViewById(R.id.addBudgetLayout);
        
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerMonth = view.findViewById(R.id.spinnerMonth);
        spinnerYear = view.findViewById(R.id.spinnerYear);
        editTextBudgetAmount = view.findViewById(R.id.editTextBudgetAmount);
        buttonSaveBudget = view.findViewById(R.id.buttonSaveBudget);
        buttonCancelBudget = view.findViewById(R.id.buttonCancelBudget);

        buttonSaveBudget.setOnClickListener(v -> saveBudget());
        buttonCancelBudget.setOnClickListener(v -> hideAddBudgetForm());
    }

    private void setupRecyclerView() {
        budgetList = new ArrayList<>();
        budgetAdapter = new BudgetAdapter(budgetList, getContext(), databaseHelper, userId);
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBudgets.setAdapter(budgetAdapter);
    }

    private void setupSpinners() {
        // Month spinner
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                          "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Year spinner
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 2; i <= currentYear + 2; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Set current month and year
        Calendar calendar = Calendar.getInstance();
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        spinnerYear.setSelection(2); // Current year is in the middle
    }

    private void loadCategories() {
        categoryList = databaseHelper.getCategories(userId, "EXPENSE");
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void loadBudgets() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        int currentYear = calendar.get(Calendar.YEAR);

        budgetList.clear();
        budgetList.addAll(databaseHelper.getBudgets(userId, currentMonth, currentYear));
        budgetAdapter.notifyDataSetChanged();
    }

    private void showAddBudgetForm() {
        addBudgetLayout.setVisibility(View.VISIBLE);
        fabAddBudget.setVisibility(View.GONE);
        loadCategories();
    }

    private void hideAddBudgetForm() {
        addBudgetLayout.setVisibility(View.GONE);
        fabAddBudget.setVisibility(View.VISIBLE);
        clearForm();
    }

    private void saveBudget() {
        String amountStr = editTextBudgetAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            editTextBudgetAmount.setError("Vui lòng nhập số tiền ngân sách");
            return;
        }

        if (categoryList.isEmpty()) {
            Toast.makeText(getContext(), "Không có danh mục nào để thiết lập ngân sách", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        int categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();
        int month = spinnerMonth.getSelectedItemPosition() + 1; // Convert to 1-based
        int year = Integer.parseInt(spinnerYear.getSelectedItem().toString());

        Budget budget = new Budget(categoryId, amount, month, year, userId);
        long result = databaseHelper.addBudget(budget);

        if (result != -1) {
            Toast.makeText(getContext(), "Thiết lập ngân sách thành công!", Toast.LENGTH_SHORT).show();
            hideAddBudgetForm();
            loadBudgets();
        } else {
            Toast.makeText(getContext(), "Thiết lập ngân sách thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editTextBudgetAmount.setText("");
        Calendar calendar = Calendar.getInstance();
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        spinnerYear.setSelection(2);
    }
}