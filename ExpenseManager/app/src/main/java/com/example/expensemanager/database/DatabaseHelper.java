package com.example.expensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.expensemanager.models.Category;
import com.example.expensemanager.models.Notification;
import com.example.expensemanager.models.Transaction;
import com.example.expensemanager.models.User;
import com.example.expensemanager.models.Budget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ExpenseManager.db";
    private static final int DATABASE_VERSION = 1;

    // Bảng Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_USER_PASSWORD = "user_password";
    private static final String COLUMN_USER_CREATED_AT = "created_at";

    // Bảng Categories
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_CATEGORY_NAME = "category_name";
    private static final String COLUMN_CATEGORY_TYPE = "category_type"; // INCOME hoặc EXPENSE
    private static final String COLUMN_CATEGORY_USER_ID = "user_id";

    // Bảng Transactions
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COLUMN_TRANSACTION_ID = "transaction_id";
    private static final String COLUMN_TRANSACTION_AMOUNT = "amount";
    private static final String COLUMN_TRANSACTION_TYPE = "transaction_type"; // INCOME hoặc EXPENSE
    private static final String COLUMN_TRANSACTION_CATEGORY_ID = "category_id";
    private static final String COLUMN_TRANSACTION_DESCRIPTION = "description";
    private static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    private static final String COLUMN_TRANSACTION_USER_ID = "user_id";

    // Bảng Budgets
    private static final String TABLE_BUDGETS = "budgets";
    private static final String COLUMN_BUDGET_ID = "budget_id";
    private static final String COLUMN_BUDGET_CATEGORY_ID = "category_id";
    private static final String COLUMN_BUDGET_AMOUNT = "budget_amount";
    private static final String COLUMN_BUDGET_MONTH = "budget_month";
    private static final String COLUMN_BUDGET_YEAR = "budget_year";
    private static final String COLUMN_BUDGET_USER_ID = "user_id";

    //Bảng Notifications
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_NOTIFICATION_ID = "id";
    public static final String COLUMN_NOTIFICATION_TITLE = "title";
    public static final String COLUMN_NOTIFICATION_MESSAGE = "message";
    public static final String COLUMN_NOTIFICATION_TYPE = "type";
    public static final String COLUMN_NOTIFICATION_DATE = "date";
    public static final String COLUMN_NOTIFICATION_IS_READ = "is_read";
    public static final String COLUMN_NOTIFICATION_USER_ID = "user_id";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_NAME + " TEXT NOT NULL,"
                + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COLUMN_USER_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Tạo bảng Categories
        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + "("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
                + COLUMN_CATEGORY_TYPE + " TEXT NOT NULL,"
                + COLUMN_CATEGORY_USER_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_CATEGORY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";

        // Tạo bảng Transactions
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL,"
                + COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL,"
                + COLUMN_TRANSACTION_CATEGORY_ID + " INTEGER,"
                + COLUMN_TRANSACTION_DESCRIPTION + " TEXT,"
                + COLUMN_TRANSACTION_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_TRANSACTION_USER_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_TRANSACTION_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + "),"
                + "FOREIGN KEY(" + COLUMN_TRANSACTION_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";

        // Tạo bảng Budgets
        String CREATE_BUDGETS_TABLE = "CREATE TABLE " + TABLE_BUDGETS + "("
                + COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BUDGET_CATEGORY_ID + " INTEGER,"
                + COLUMN_BUDGET_AMOUNT + " REAL NOT NULL,"
                + COLUMN_BUDGET_MONTH + " INTEGER NOT NULL,"
                + COLUMN_BUDGET_YEAR + " INTEGER NOT NULL,"
                + COLUMN_BUDGET_USER_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_BUDGET_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + "),"
                + "FOREIGN KEY(" + COLUMN_BUDGET_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        //Tạo ban Notifications
          final String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTIFICATION_TITLE + " TEXT NOT NULL,"
                + COLUMN_NOTIFICATION_MESSAGE + " TEXT NOT NULL,"
                + COLUMN_NOTIFICATION_TYPE + " TEXT NOT NULL,"
                + COLUMN_NOTIFICATION_DATE + " TEXT NOT NULL,"
                + COLUMN_NOTIFICATION_IS_READ + " INTEGER DEFAULT 0,"
                + COLUMN_NOTIFICATION_USER_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_NOTIFICATION_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        db.execSQL(CREATE_BUDGETS_TABLE);

        // Thêm categories mặc định
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        // Categories cho chi tiêu
        String[] expenseCategories = {"Ăn uống", "Giao thông", "Mua sắm", "Giải trí", "Y tế", "Giáo dục", "Khác"};
        // Categories cho thu nhập
        String[] incomeCategories = {"Lương", "Đầu tư", "Thưởng", "Khác"};

        for (String category : expenseCategories) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_NAME, category);
            values.put(COLUMN_CATEGORY_TYPE, "EXPENSE");
            values.put(COLUMN_CATEGORY_USER_ID, 0); // Default categories
            db.insert(TABLE_CATEGORIES, null, values);
        }

        for (String category : incomeCategories) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_NAME, category);
            values.put(COLUMN_CATEGORY_TYPE, "INCOME");
            values.put(COLUMN_CATEGORY_USER_ID, 0); // Default categories
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    // User methods
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_USER_EMAIL};
        String selection = COLUMN_USER_EMAIL + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        User user = null;

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_USER_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_USER_NAME);
            int emailIndex = cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL);

            user = new User();
            user.setId(cursor.getInt(idIndex));
            user.setName(cursor.getString(nameIndex));
            user.setEmail(cursor.getString(emailIndex));
        }

        cursor.close();
        db.close();
        return user;
    }


    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    // Category methods
    public long addCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, category.getName());
        values.put(COLUMN_CATEGORY_TYPE, category.getType());
        values.put(COLUMN_CATEGORY_USER_ID, category.getUserId());

        long id = db.insert(TABLE_CATEGORIES, null, values);
        db.close();
        return id;
    }

    public List<Category> getCategories(int userId, String type) {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = "(" + COLUMN_CATEGORY_USER_ID + " = ? OR " + COLUMN_CATEGORY_USER_ID + " = 0) AND " + COLUMN_CATEGORY_TYPE + " = ?";
        String[] selectionArgs = {String.valueOf(userId), type};

        Cursor cursor = db.query(TABLE_CATEGORIES, null, selection, selectionArgs, null, null, COLUMN_CATEGORY_NAME);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)));
                category.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)));
                category.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_TYPE)));
                category.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_USER_ID)));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    // Transaction methods
    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_AMOUNT, transaction.getAmount());
        values.put(COLUMN_TRANSACTION_TYPE, transaction.getType());
        values.put(COLUMN_TRANSACTION_CATEGORY_ID, transaction.getCategoryId());
        values.put(COLUMN_TRANSACTION_DESCRIPTION, transaction.getDescription());
        values.put(COLUMN_TRANSACTION_DATE, transaction.getDate());
        values.put(COLUMN_TRANSACTION_USER_ID, transaction.getUserId());

        long id = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return id;
    }

    public List<Transaction> getTransactions(int userId, String startDate, String endDate) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT t.*, c." + COLUMN_CATEGORY_NAME + " FROM " + TABLE_TRANSACTIONS + " t " +
                      "LEFT JOIN " + TABLE_CATEGORIES + " c ON t." + COLUMN_TRANSACTION_CATEGORY_ID + " = c." + COLUMN_CATEGORY_ID +
                      " WHERE t." + COLUMN_TRANSACTION_USER_ID + " = ? AND t." + COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ?" +
                      " ORDER BY t." + COLUMN_TRANSACTION_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), startDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)));
                transaction.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY_ID)));
                transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DESCRIPTION)));
                transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE)));
                transaction.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_USER_ID)));
                transaction.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)));
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactions;
    }

    public double getTotalAmount(int userId, String type, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_TRANSACTION_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                      " WHERE " + COLUMN_TRANSACTION_USER_ID + " = ? AND " + COLUMN_TRANSACTION_TYPE + " = ?" +
                      " AND " + COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), type, startDate, endDate});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    // Budget methods
    public long addBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BUDGET_CATEGORY_ID, budget.getCategoryId());
        values.put(COLUMN_BUDGET_AMOUNT, budget.getAmount());
        values.put(COLUMN_BUDGET_MONTH, budget.getMonth());
        values.put(COLUMN_BUDGET_YEAR, budget.getYear());
        values.put(COLUMN_BUDGET_USER_ID, budget.getUserId());

        long id = db.insert(TABLE_BUDGETS, null, values);
        db.close();
        return id;
    }

    public List<Budget> getBudgets(int userId, int month, int year) {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT b.*, c." + COLUMN_CATEGORY_NAME + " FROM " + TABLE_BUDGETS + " b " +
                      "LEFT JOIN " + TABLE_CATEGORIES + " c ON b." + COLUMN_BUDGET_CATEGORY_ID + " = c." + COLUMN_CATEGORY_ID +
                      " WHERE b." + COLUMN_BUDGET_USER_ID + " = ? AND b." + COLUMN_BUDGET_MONTH + " = ? AND b." + COLUMN_BUDGET_YEAR + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(month), String.valueOf(year)});

        if (cursor.moveToFirst()) {
            do {
                Budget budget = new Budget();
                budget.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_ID)));
                budget.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_CATEGORY_ID)));
                budget.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_AMOUNT)));
                budget.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_MONTH)));
                budget.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_YEAR)));
                budget.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_USER_ID)));
                budget.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)));
                budgets.add(budget);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return budgets;
    }

    // Thêm các phương thức này vào DatabaseHelper.java

    public double getTotalAmountByCategory(int userId, int categoryId, String type, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_TRANSACTION_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_TRANSACTION_USER_ID + " = ? AND " + COLUMN_TRANSACTION_CATEGORY_ID + " = ?" +
                " AND " + COLUMN_TRANSACTION_TYPE + " = ? AND " + COLUMN_TRANSACTION_DATE + " BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(categoryId), type, startDate, endDate});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if category is being used in transactions
        String checkQuery = "SELECT COUNT(*) FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TRANSACTION_CATEGORY_ID + " = ?";
        Cursor cursor = db.rawQuery(checkQuery, new String[]{String.valueOf(categoryId)});

        boolean hasTransactions = false;
        if (cursor.moveToFirst()) {
            hasTransactions = cursor.getInt(0) > 0;
        }
        cursor.close();

        if (hasTransactions) {
            db.close();
            return false; // Cannot delete category with existing transactions
        }

        int result = db.delete(TABLE_CATEGORIES, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
        return result > 0;
    }

    public boolean updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, category.getName());
        values.put(COLUMN_CATEGORY_TYPE, category.getType());

        int result = db.update(TABLE_CATEGORIES, values, COLUMN_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(category.getId())});
        db.close();
        return result > 0;
    }

    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TRANSACTIONS, COLUMN_TRANSACTION_ID + " = ?",
                new String[]{String.valueOf(transactionId)});
        db.close();
        return result > 0;
    }

    public boolean updateTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_AMOUNT, transaction.getAmount());
        values.put(COLUMN_TRANSACTION_TYPE, transaction.getType());
        values.put(COLUMN_TRANSACTION_CATEGORY_ID, transaction.getCategoryId());
        values.put(COLUMN_TRANSACTION_DESCRIPTION, transaction.getDescription());
        values.put(COLUMN_TRANSACTION_DATE, transaction.getDate());

        int result = db.update(TABLE_TRANSACTIONS, values, COLUMN_TRANSACTION_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())});
        db.close();
        return result > 0;
    }

    public boolean updateBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BUDGET_CATEGORY_ID, budget.getCategoryId());
        values.put(COLUMN_BUDGET_AMOUNT, budget.getAmount());
        values.put(COLUMN_BUDGET_MONTH, budget.getMonth());
        values.put(COLUMN_BUDGET_YEAR, budget.getYear());

        int result = db.update(TABLE_BUDGETS, values, COLUMN_BUDGET_ID + " = ?",
                new String[]{String.valueOf(budget.getId())});
        db.close();
        return result > 0;
    }

    public boolean deleteBudget(int budgetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BUDGETS, COLUMN_BUDGET_ID + " = ?",
                new String[]{String.valueOf(budgetId)});
        db.close();
        return result > 0;
    }

    // Method to check budget alerts
    public List<Budget> getBudgetAlerts(int userId) {
        List<Budget> alertBudgets = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        List<Budget> budgets = getBudgets(userId, currentMonth, currentYear);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = String.format(Locale.getDefault(), "%d-%02d-01", currentYear, currentMonth);

        calendar.set(currentYear, currentMonth - 1, 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dateFormat.format(calendar.getTime());

        for (Budget budget : budgets) {
            double spentAmount = getTotalAmountByCategory(userId, budget.getCategoryId(), "EXPENSE", startDate, endDate);
            double percentage = (spentAmount / budget.getAmount()) * 100;

            if (percentage >= 80) { // Alert when 80% or more of budget is used
                alertBudgets.add(budget);
            }
        }

        return alertBudgets;
    }

    // Methods for notifications
    public long addNotification(Notification notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOTIFICATION_TITLE, notification.getTitle());
        values.put(COLUMN_NOTIFICATION_MESSAGE, notification.getMessage());
        values.put(COLUMN_NOTIFICATION_TYPE, notification.getType());
        values.put(COLUMN_NOTIFICATION_DATE, notification.getDate());
        values.put(COLUMN_NOTIFICATION_IS_READ, notification.isRead() ? 1 : 0);
        values.put(COLUMN_NOTIFICATION_USER_ID, notification.getUserId());

        long result = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
        return result;
    }

    public List<Notification> getNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NOTIFICATIONS +
                " WHERE " + COLUMN_NOTIFICATION_USER_ID + " = ? " +
                " ORDER BY " + COLUMN_NOTIFICATION_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Notification notification = new Notification(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_MESSAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_IS_READ)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_USER_ID))
                );
                notifications.add(notification);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notifications;
    }

    public void markNotificationAsRead(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_IS_READ, 1);

        db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)});
        db.close();
    }

    public void markNotificationAsUnread(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_IS_READ, 0);

        db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)});
        db.close();
    }

    public void markAllNotificationsAsRead(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_IS_READ, 1);

        db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_NOTIFICATION_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
    }

    public void deleteNotification(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS,
                COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)});
        db.close();
    }

    public void deleteAllNotifications(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS,
                COLUMN_NOTIFICATION_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
    }

    public int getUnreadNotificationCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS +
                " WHERE " + COLUMN_NOTIFICATION_USER_ID + " = ? AND " +
                COLUMN_NOTIFICATION_IS_READ + " = 0";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public double getSpentAmountForBudget(Budget budget) {
        SQLiteDatabase db = this.getReadableDatabase();
        double spentAmount = 0;

        // Tạo ngày bắt đầu và kết thúc của tháng
        String startDate = String.format(Locale.getDefault(), "%d-%02d-01", budget.getYear(), budget.getMonth());

        // Tính ngày cuối tháng
        Calendar calendar = Calendar.getInstance();
        calendar.set(budget.getYear(), budget.getMonth() - 1, 1); // month - 1 vì Calendar.MONTH bắt đầu từ 0
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        String endDate = String.format(Locale.getDefault(), "%d-%02d-%02d", budget.getYear(), budget.getMonth(), lastDayOfMonth);

        String query = "SELECT SUM(" + COLUMN_TRANSACTION_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_TRANSACTION_USER_ID + " = ? AND " +
                COLUMN_TRANSACTION_CATEGORY_ID + " = ? AND " +
                COLUMN_TRANSACTION_TYPE + " = 'EXPENSE' AND " +
                "DATE(" + COLUMN_TRANSACTION_DATE + ") >= ? AND " +
                "DATE(" + COLUMN_TRANSACTION_DATE + ") <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(budget.getUserId()),
                String.valueOf(budget.getCategoryId()),
                startDate,
                endDate
        });

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            spentAmount = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return spentAmount;
    }
}