package com.oleje.transactions_statistic.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.oleje.transactions_statistic.databinding.ItemTransactionBinding;
import com.oleje.transactions_statistic.model.Transaction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList = new ArrayList<>();
    private final Set<Transaction> selectedItems = new HashSet<>();
    private boolean isSelectionMode = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction, int position);
        void onTransactionLongClick(Transaction transaction, int position);
    }

    private OnTransactionClickListener listener;

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction current = transactionList.get(position);
        holder.binding.transactionReceiver.setText(current.receiver);
        holder.binding.transactionValue.setText(String.format("%.2f ₴", current.value));
        holder.binding.transactionDate.setText(dateFormat.format(current.date));

        // Highlight selected items
        if (selectedItems.contains(current)) {
            holder.itemView.setAlpha(0.5f);
            holder.binding.getRoot().setChecked(true);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.binding.getRoot().setChecked(false);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(current, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTransactionLongClick(current, position);
                return true;
            }
            return false;
        });
    }

    public void toggleSelection(Transaction transaction) {
        if (selectedItems.contains(transaction)) {
            selectedItems.remove(transaction);
        } else {
            selectedItems.add(transaction);
        }
        notifyItemChanged(transactionList.indexOf(transaction));
    }

    public void clearSelection() {
        selectedItems.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    public List<Transaction> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public int getSelectedCount() {
        return selectedItems.size();
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void setData(List<Transaction> transactions) {
        this.transactionList = transactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;
        TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
