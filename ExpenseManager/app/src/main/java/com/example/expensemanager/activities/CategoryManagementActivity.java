package com.example.expensemanager.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.adapters.CategoryAdapter;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerViewCategories;
    private FloatingActionButton fabAddCategory;
    private CategoryAdapter categoryAdapter;
    private DatabaseHelper databaseHelper;
    private int userId;
    private List<Category> categoryList;
    private LinearLayout emptyStateLayout;

    // Add category views
    private View addCategoryLayout;
    private EditText editTextCategoryName;
    private RadioGroup radioGroupCategoryType;
    private RadioButton radioIncome, radioExpense;
    private Button buttonSaveCategory, buttonCancelCategory;
    private ImageButton buttonCloseForm;
    private Spinner spinnerCategoryFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);
        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterSpinner();
        loadCategories();

        fabAddCategory.setOnClickListener(v -> showAddCategoryForm());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        fabAddCategory = findViewById(R.id.fabAddCategory);
        addCategoryLayout = findViewById(R.id.addCategoryLayout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        
        editTextCategoryName = findViewById(R.id.editTextCategoryName);
        radioGroupCategoryType = findViewById(R.id.radioGroupCategoryType);
        radioIncome = findViewById(R.id.radioIncome);
        radioExpense = findViewById(R.id.radioExpense);
        buttonSaveCategory = findViewById(R.id.buttonSaveCategory);
        buttonCancelCategory = findViewById(R.id.buttonCancelCategory);
        buttonCloseForm = findViewById(R.id.buttonCloseForm);
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);

        buttonSaveCategory.setOnClickListener(v -> saveCategory());
        buttonCancelCategory.setOnClickListener(v -> hideAddCategoryForm());
        buttonCloseForm.setOnClickListener(v -> hideAddCategoryForm());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, this, databaseHelper);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void setupFilterSpinner() {
        String[] filterOptions = {"Tất cả", "Thu nhập", "Chi tiêu"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(adapter);

        spinnerCategoryFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadCategories();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadCategories() {
        categoryList.clear();
        
        int filterPosition = spinnerCategoryFilter.getSelectedItemPosition();
        switch (filterPosition) {
            case 0: // Tất cả
                categoryList.addAll(databaseHelper.getCategories(userId, "INCOME"));
                categoryList.addAll(databaseHelper.getCategories(userId, "EXPENSE"));
                break;
            case 1: // Thu nhập
                categoryList.addAll(databaseHelper.getCategories(userId, "INCOME"));
                break;
            case 2: // Chi tiêu
                categoryList.addAll(databaseHelper.getCategories(userId, "EXPENSE"));
                break;
        }
        
        categoryAdapter.notifyDataSetChanged();
        
        // Show/hide empty state
        if (categoryList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerViewCategories.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewCategories.setVisibility(View.VISIBLE);
        }
    }

    private void showAddCategoryForm() {
        addCategoryLayout.setVisibility(View.VISIBLE);
        fabAddCategory.setVisibility(View.GONE);
    }

    private void hideAddCategoryForm() {
        addCategoryLayout.setVisibility(View.GONE);
        fabAddCategory.setVisibility(View.VISIBLE);
        clearForm();
    }

    private void saveCategory() {
        String categoryName = editTextCategoryName.getText().toString().trim();

        if (categoryName.isEmpty()) {
            editTextCategoryName.setError("Vui lòng nhập tên danh mục");
            return;
        }

        String type = radioIncome.isChecked() ? "INCOME" : "EXPENSE";
        Category category = new Category(categoryName, type, userId);
        long result = databaseHelper.addCategory(category);

        if (result != -1) {
            Toast.makeText(this, "Thêm danh mục thành công!", Toast.LENGTH_SHORT).show();
            hideAddCategoryForm();
            loadCategories();
        } else {
            Toast.makeText(this, "Thêm danh mục thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editTextCategoryName.setText("");
        radioExpense.setChecked(true);
    }

    public void refreshCategories() {
        loadCategories();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (addCategoryLayout.getVisibility() == View.VISIBLE) {
            hideAddCategoryForm();
        } else {
            super.onBackPressed();
        }
    }
}