package com.oleje.transactions_statistic.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.oleje.transactions_statistic.databinding.ItemInterceptedNotificationBinding;
import com.oleje.transactions_statistic.model.InterceptedNotification;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InterceptedNotificationAdapter extends RecyclerView.Adapter<InterceptedNotificationAdapter.ViewHolder> {

    private List<InterceptedNotification> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd.MM", Locale.getDefault());
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(InterceptedNotification item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInterceptedNotificationBinding binding = ItemInterceptedNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InterceptedNotification item = items.get(position);
        holder.binding.notifPackageName.setText(item.packageName);
        holder.binding.notifTitle.setText(item.title);
        holder.binding.notifText.setText(item.text);
        holder.binding.notifDate.setText(dateFormat.format(item.date));

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setData(List<InterceptedNotification> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemInterceptedNotificationBinding binding;
        public ViewHolder(@NonNull ItemInterceptedNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
