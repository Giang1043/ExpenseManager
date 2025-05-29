package com.example.expensemanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.R;
import com.example.expensemanager.models.Notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;
    private Context context;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onNotificationLongClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        holder.textViewTitle.setText(notification.getTitle());
        holder.textViewMessage.setText(notification.getMessage());
        holder.textViewDate.setText(formatDate(notification.getDate()));

        // Set icon based on notification type
        int iconRes = getIconForType(notification.getType());
        holder.imageViewIcon.setImageResource(iconRes);

        // Set background color based on read status
        if (notification.isRead()) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            holder.textViewTitle.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.unread_notification_bg));
            holder.textViewTitle.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNotificationLongClick(notification);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    private int getIconForType(String type) {
        switch (type) {
            case "BUDGET_WARNING":
                return R.drawable.ic_warning;
            case "BUDGET_EXCEEDED":
                return R.drawable.ic_error;
            case "UNUSUAL_SPENDING":
                return R.drawable.ic_trending_up;
            case "DAILY_LIMIT":
                return R.drawable.ic_calendar;
            default:
                return R.drawable.ic_notifications;
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewMessage, textViewDate;
        ImageView imageViewIcon;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
        }
    }
}
