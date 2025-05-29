package com.example.expensemanager.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.expensemanager.R;
import com.example.expensemanager.activities.MainActivity;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Category;
import com.example.expensemanager.models.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportFragment extends Fragment {
    private Spinner spinnerReportType, spinnerTimePeriod;
    private PieChart pieChart;
    private LineChart lineChart;
    private TextView textViewTotalIncome, textViewTotalExpense, textViewNetIncome;
    private DatabaseHelper databaseHelper;
    private int userId;
    private NumberFormat currencyFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        databaseHelper = mainActivity.getDatabaseHelper();
        userId = mainActivity.getUserId();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initViews(view);
        setupSpinners();
        loadReportData();

        return view;
    }

    private void initViews(View view) {
        spinnerReportType = view.findViewById(R.id.spinnerReportType);
        spinnerTimePeriod = view.findViewById(R.id.spinnerTimePeriod);
        pieChart = view.findViewById(R.id.pieChart);
        lineChart = view.findViewById(R.id.lineChart);
        textViewTotalIncome = view.findViewById(R.id.textViewTotalIncome);
        textViewTotalExpense = view.findViewById(R.id.textViewTotalExpense);
        textViewNetIncome = view.findViewById(R.id.textViewNetIncome);
    }

    private void setupSpinners() {
        // Report type spinner
        String[] reportTypes = {"Chi tiêu theo danh mục", "Thu nhập theo danh mục", "Xu hướng theo thời gian"};
        ArrayAdapter<String> reportTypeAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, reportTypes);
        reportTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportType.setAdapter(reportTypeAdapter);

        // Time period spinner
        String[] timePeriods = {"Tháng này", "Tháng trước", "3 tháng gần đây", "6 tháng gần đây", "Năm này"};
        ArrayAdapter<String> timePeriodAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, timePeriods);
        timePeriodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimePeriod.setAdapter(timePeriodAdapter);

        spinnerReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadReportData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadReportData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadReportData() {
        String[] dateRange = getDateRange();
        String startDate = dateRange[0];
        String endDate = dateRange[1];

        // Load summary data
        double totalIncome = databaseHelper.getTotalAmount(userId, "INCOME", startDate, endDate);
        double totalExpense = databaseHelper.getTotalAmount(userId, "EXPENSE", startDate, endDate);
        double netIncome = totalIncome - totalExpense;

        textViewTotalIncome.setText(currencyFormat.format(totalIncome));
        textViewTotalExpense.setText(currencyFormat.format(totalExpense));
        textViewNetIncome.setText(currencyFormat.format(netIncome));

        // Set color for net income
        if (netIncome >= 0) {
            textViewNetIncome.setTextColor(getResources().getColor(R.color.colorIncome));
        } else {
            textViewNetIncome.setTextColor(getResources().getColor(R.color.colorExpense));
        }

        // Load chart data based on selected report type
        int reportType = spinnerReportType.getSelectedItemPosition();
        switch (reportType) {
            case 0: // Chi tiêu theo danh mục
                loadExpenseByCategoryChart(startDate, endDate);
                break;
            case 1: // Thu nhập theo danh mục
                loadIncomeByCategoryChart(startDate, endDate);
                break;
            case 2: // Xu hướng theo thời gian
                loadTrendChart(startDate, endDate);
                break;
        }
    }

    private String[] getDateRange() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        int timePeriod = spinnerTimePeriod.getSelectedItemPosition();
        String startDate, endDate;

        switch (timePeriod) {
            case 0: // Tháng này
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = dateFormat.format(calendar.getTime());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = dateFormat.format(calendar.getTime());
                break;
            case 1: // Tháng trước
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = dateFormat.format(calendar.getTime());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = dateFormat.format(calendar.getTime());
                break;
            case 2: // 3 tháng gần đây
                calendar.add(Calendar.MONTH, -3);
                startDate = dateFormat.format(calendar.getTime());
                calendar = Calendar.getInstance();
                endDate = dateFormat.format(calendar.getTime());
                break;
            case 3: // 6 tháng gần đây
                calendar.add(Calendar.MONTH, -6);
                startDate = dateFormat.format(calendar.getTime());
                calendar = Calendar.getInstance();
                endDate = dateFormat.format(calendar.getTime());
                break;
            case 4: // Năm này
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                startDate = dateFormat.format(calendar.getTime());
                calendar = Calendar.getInstance();
                endDate = dateFormat.format(calendar.getTime());
                break;
            default:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = dateFormat.format(calendar.getTime());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = dateFormat.format(calendar.getTime());
                break;
        }

        return new String[]{startDate, endDate};
    }

    private void loadExpenseByCategoryChart(String startDate, String endDate) {
        pieChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);

        List<Transaction> transactions = databaseHelper.getTransactions(userId, startDate, endDate);
        Map<String, Double> categoryExpenses = new HashMap<>();

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("EXPENSE")) {
                String categoryName = transaction.getCategoryName();
                categoryExpenses.put(categoryName, 
                    categoryExpenses.getOrDefault(categoryName, 0.0) + transaction.getAmount());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Chi tiêu theo danh mục");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void loadIncomeByCategoryChart(String startDate, String endDate) {
        pieChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);

        List<Transaction> transactions = databaseHelper.getTransactions(userId, startDate, endDate);
        Map<String, Double> categoryIncomes = new HashMap<>();

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("INCOME")) {
                String categoryName = transaction.getCategoryName();
                categoryIncomes.put(categoryName, 
                    categoryIncomes.getOrDefault(categoryName, 0.0) + transaction.getAmount());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryIncomes.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Thu nhập theo danh mục");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void loadTrendChart(String startDate, String endDate) {
        pieChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.VISIBLE);

        // This is a simplified trend chart - you can enhance it further
        List<Entry> incomeEntries = new ArrayList<>();
        List<Entry> expenseEntries = new ArrayList<>();

        // Sample data for demonstration
        for (int i = 0; i < 30; i++) {
            incomeEntries.add(new Entry(i, (float) (Math.random() * 1000000)));
            expenseEntries.add(new Entry(i, (float) (Math.random() * 800000)));
        }

        LineDataSet incomeDataSet = new LineDataSet(incomeEntries, "Thu nhập");
        incomeDataSet.setColor(Color.GREEN);
        incomeDataSet.setLineWidth(2f);

        LineDataSet expenseDataSet = new LineDataSet(expenseEntries, "Chi tiêu");
        expenseDataSet.setColor(Color.RED);
        expenseDataSet.setLineWidth(2f);

        LineData lineData = new LineData(incomeDataSet, expenseDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }
}