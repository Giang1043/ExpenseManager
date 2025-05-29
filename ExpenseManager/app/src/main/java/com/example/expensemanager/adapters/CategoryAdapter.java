package com.example.expensemanager.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.activities.CategoryManagementActivity;
import com.example.expensemanager.database.DatabaseHelper;
import com.example.expensemanager.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categoryList;
    private Context context;
    private DatabaseHelper databaseHelper;

    public CategoryAdapter(List<Category> categoryList, Context context, DatabaseHelper databaseHelper) {
        this.categoryList = categoryList;
        this.context = context;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        
        holder.textViewCategoryName.setText(category.getName());
        
        String typeText = category.getType().equals("INCOME") ? "Thu nhập" : "Chi tiêu";
        holder.textViewCategoryType.setText(typeText);
        
        // Set type color
        if (category.getType().equals("INCOME")) {
            holder.textViewCategoryType.setTextColor(context.getResources().getColor(R.color.colorIncome));
        } else {
            holder.textViewCategoryType.setTextColor(context.getResources().getColor(R.color.colorExpense));
        }

        // Hide delete button for default categories (userId = 0)
        if (category.getUserId() == 0) {
            holder.buttonDelete.setVisibility(View.GONE);
        } else {
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonDelete.setOnClickListener(v -> showDeleteConfirmation(category, position));
        }
    }

    private void showDeleteConfirmation(Category category, int position) {
        new AlertDialog.Builder(context)
            .setTitle("Xóa danh mục")
            .setMessage("Bạn có chắc chắn muốn xóa danh mục \"" + category.getName() + "\"?")
            .setPositiveButton("Xóa", (dialog, which) -> deleteCategory(category, position))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteCategory(Category category, int position) {
        boolean result = databaseHelper.deleteCategory(category.getId());
        if (result) {
            categoryList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Xóa danh mục thành công!", Toast.LENGTH_SHORT).show();
            
            if (context instanceof CategoryManagementActivity) {
                ((CategoryManagementActivity) context).refreshCategories();
            }
        } else {
            Toast.makeText(context, "Không thể xóa danh mục này!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategoryName, textViewCategoryType;
        ImageButton buttonDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.textViewCategoryName);
            textViewCategoryType = itemView.findViewById(R.id.textViewCategoryType);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}