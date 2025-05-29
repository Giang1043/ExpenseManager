package com.example.expensemanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Budget;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<Budget> budgetList;
    private Context context;
    private DatabaseHelper databaseHelper;
    private int userId;
    private NumberFormat currencyFormat;

    public BudgetAdapter(List<Budget> budgetList, Context context, DatabaseHelper databaseHelper, int userId) {
        this.budgetList = budgetList;
        this.context = context;
        this.databaseHelper = databaseHelper;
        this.userId = userId;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);

        holder.textViewCategory.setText(budget.getCategoryName());
        holder.textViewBudgetAmount.setText(currencyFormat.format(budget.getAmount()));

        // Calculate spent amount for this budget
        String startDate = String.format(Locale.getDefault(), "%d-%02d-01", budget.getYear(), budget.getMonth());

        Calendar calendar = Calendar.getInstance();
        calendar.set(budget.getYear(), budget.getMonth() - 1, 1); // Calendar.MONTH is 0-based
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String endDate = dateFormat.format(calendar.getTime());

        double spentAmount = getSpentAmountForCategory(budget.getCategoryId(), startDate, endDate);
        holder.textViewSpentAmount.setText(currencyFormat.format(spentAmount));

        // Calculate progress
        int progress = (int) ((spentAmount / budget.getAmount()) * 100);
        holder.progressBar.setProgress(Math.min(progress, 100));

        // Set progress bar color based on usage
        if (progress >= 100) {
            holder.progressBar.setProgressTintList(context.getResources().getColorStateList(android.R.color.holo_red_dark));
            holder.textViewStatus.setText("Vượt ngân sách");
            holder.textViewStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else if (progress >= 80) {
            holder.progressBar.setProgressTintList(context.getResources().getColorStateList(android.R.color.holo_orange_dark));
            holder.textViewStatus.setText("Gần đạt ngân sách");
            holder.textViewStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.progressBar.setProgressTintList(context.getResources().getColorStateList(android.R.color.holo_green_dark));
            holder.textViewStatus.setText("Trong ngân sách");
            holder.textViewStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }

        holder.textViewProgress.setText(progress + "%");

        // Display month/year
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                          "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        holder.textViewPeriod.setText(months[budget.getMonth() - 1] + " " + budget.getYear());
    }

    private double getSpentAmountForCategory(int categoryId, String startDate, String endDate) {
        // This method should be implemented in DatabaseHelper
        // For now, we'll use a simplified approach
        return databaseHelper.getTotalAmountByCategory(userId, categoryId, "EXPENSE", startDate, endDate);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategory, textViewBudgetAmount, textViewSpentAmount,
                 textViewProgress, textViewStatus, textViewPeriod;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewBudgetAmount = itemView.findViewById(R.id.textViewBudgetAmount);
            textViewSpentAmount = itemView.findViewById(R.id.textViewSpentAmount);
            textViewProgress = itemView.findViewById(R.id.textViewProgress);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewPeriod = itemView.findViewById(R.id.textViewPeriod);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}