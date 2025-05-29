package com.example.expensemanager.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Transaction;
import com.example.expensemanager.activities.EditTransactionActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;
    private Context context;
    private DatabaseHelper databaseHelper;
    private OnTransactionChangeListener listener;

    public interface OnTransactionChangeListener {
        void onTransactionChanged();
    }

    public TransactionAdapter(List<Transaction> transactions, Context context) {
        this.transactions = transactions;
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
    }

    public void setOnTransactionChangeListener(OnTransactionChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set category name
        holder.textViewCategory.setText(transaction.getCategoryName());

        // Set description
        if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
            holder.textViewDescription.setText(transaction.getDescription());
            holder.textViewDescription.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDescription.setVisibility(View.GONE);
        }

        // Format amount with NumberFormat
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount;
        int amountColor;

        if (transaction.getType().equals("INCOME")) {
            formattedAmount = "+" + formatter.format(transaction.getAmount());
            amountColor = ContextCompat.getColor(context, R.color.income_color);
            holder.textViewType.setText("Thu nhập");
            holder.textViewType.setBackgroundResource(R.drawable.income_badge_background);
            holder.imageViewTransactionIcon.setImageResource(R.drawable.ic_income);
        } else {
            formattedAmount = "-" + formatter.format(transaction.getAmount());
            amountColor = ContextCompat.getColor(context, R.color.expense_color);
            holder.textViewType.setText("Chi tiêu");
            holder.textViewType.setBackgroundResource(R.drawable.expense_badge_background);
            holder.imageViewTransactionIcon.setImageResource(R.drawable.ic_expense);
        }

        holder.textViewAmount.setText(formattedAmount);
        holder.textViewAmount.setTextColor(amountColor);

        // Format date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(transaction.getDate());
            holder.textViewDateTime.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.textViewDateTime.setText(transaction.getDate());
        }

        // Set click listeners
        holder.imageViewMenu.setOnClickListener(v -> showTransactionOptions(transaction, position));

        holder.itemView.setOnClickListener(v -> showTransactionDetails(transaction));

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteConfirmation(transaction, position);
            return true;
        });
    }

    private void showTransactionOptions(Transaction transaction, int position) {
        String[] options = {"Xem chi tiết", "Chỉnh sửa", "Xóa"};

        new AlertDialog.Builder(context)
                .setTitle("Tùy chọn giao dịch")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showTransactionDetails(transaction);
                            break;
                        case 1:
                            editTransaction(transaction);
                            break;
                        case 2:
                            showDeleteConfirmation(transaction, position);
                            break;
                    }
                })
                .show();
    }

    private void showTransactionDetails(Transaction transaction) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(transaction.getAmount());

        String details = "Số tiền: " + formattedAmount + "\n" +
                "Loại: " + (transaction.getType().equals("INCOME") ? "Thu nhập" : "Chi tiêu") + "\n" +
                "Danh mục: " + transaction.getCategoryName() + "\n" +
                "Mô tả: " + (transaction.getDescription() != null ? transaction.getDescription() : "Không có") + "\n" +
                "Ngày: " + transaction.getDate();

        new AlertDialog.Builder(context)
                .setTitle("Chi tiết giao dịch")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .setNeutralButton("Chỉnh sửa", (dialog, which) -> editTransaction(transaction))
                .show();
    }

    private void editTransaction(Transaction transaction) {
        Intent intent = new Intent(context, EditTransactionActivity.class);
        intent.putExtra("transaction_id", transaction.getId());
        intent.putExtra("amount", transaction.getAmount());
        intent.putExtra("type", transaction.getType());
        intent.putExtra("category_id", transaction.getCategoryId());
        intent.putExtra("description", transaction.getDescription());
        intent.putExtra("date", transaction.getDate());
        context.startActivity(intent);
    }

    private void showDeleteConfirmation(Transaction transaction, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTransaction(transaction, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTransaction(Transaction transaction, int position) {
        boolean success = databaseHelper.deleteTransaction(transaction.getId());
        if (success) {
            transactions.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, transactions.size());
            Toast.makeText(context, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onTransactionChanged();
            }
        } else {
            Toast.makeText(context, "Lỗi khi xóa giao dịch", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategory, textViewDescription, textViewDateTime, textViewAmount, textViewType;
        ImageView imageViewTransactionIcon, imageViewMenu;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewType = itemView.findViewById(R.id.textViewType);
            imageViewTransactionIcon = itemView.findViewById(R.id.imageViewTransactionIcon);
            imageViewMenu = itemView.findViewById(R.id.imageViewMenu);
        }
    }
}