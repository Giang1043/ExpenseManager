package com.example.expensemanager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.expensemanager.R;
import com.example.expensemanager.activities.CategoryManagementActivity;
import com.example.expensemanager.activities.MainActivity;
import com.example.expensemanager.database.DatabaseHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private TextView textViewBalance, textViewIncome, textViewExpense;
    private TextView textViewWelcome;
    private CardView cardViewManageCategories, cardViewQuickAddTransaction;
    private DatabaseHelper databaseHelper;
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        databaseHelper = mainActivity.getDatabaseHelper();
        userId = mainActivity.getUserId();

        initViews(view);
        setupClickListeners();
        loadDashboardData();

        return view;
    }

    private void initViews(View view) {
        textViewWelcome = view.findViewById(R.id.textViewWelcome);
        textViewBalance = view.findViewById(R.id.textViewBalance);
        textViewIncome = view.findViewById(R.id.textViewIncome);
        textViewExpense = view.findViewById(R.id.textViewExpense);
        cardViewManageCategories = view.findViewById(R.id.cardViewManageCategories);
        cardViewQuickAddTransaction = view.findViewById(R.id.cardViewQuickAddTransaction);
    }

    private void setupClickListeners() {
        cardViewManageCategories.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CategoryManagementActivity.class));
        });

        cardViewQuickAddTransaction.setOnClickListener(v -> {
            // Switch to transaction tab
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                // You can implement this method in MainActivity to switch tabs
                // mainActivity.switchToTransactionTab();
            }
        });
    }

    private void loadDashboardData() {
        // Lấy dữ liệu tháng hiện tại
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Ngày đầu tháng
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dateFormat.format(calendar.getTime());

        // Ngày cuối tháng
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(calendar.getTime());

        double totalIncome = databaseHelper.getTotalAmount(userId, "INCOME", startDate, endDate);
        double totalExpense = databaseHelper.getTotalAmount(userId, "EXPENSE", startDate, endDate);
        double balance = totalIncome - totalExpense;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        textViewBalance.setText(currencyFormat.format(balance));
        textViewIncome.setText(currencyFormat.format(totalIncome));
        textViewExpense.setText(currencyFormat.format(totalExpense));

        // Đổi màu số dư
        if (balance >= 0) {
            textViewBalance.setTextColor(getResources().getColor(R.color.colorIncome));
        } else {
            textViewBalance.setTextColor(getResources().getColor(R.color.colorExpense));
        }
    }
}