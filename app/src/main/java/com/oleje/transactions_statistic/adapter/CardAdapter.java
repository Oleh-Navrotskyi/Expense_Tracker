package com.oleje.transactions_statistic.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.oleje.transactions_statistic.R;
import com.oleje.transactions_statistic.databinding.ItemCardBinding;
import com.oleje.transactions_statistic.model.CardWithExpensesRoom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<CardWithExpensesRoom> cardList = new ArrayList<>();
    private final OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(int cardNum);
        void onOrderChanged(List<CardWithExpensesRoom> cards);
    }

    public CardAdapter(OnCardClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardWithExpensesRoom current = cardList.get(position);
        holder.binding.cardNumber.setText("**** " + current.card.card);
        holder.binding.totalExpenses.setText(String.format("%.2f ₴", current.totalExpenses));
        
        if (current.card.imageBlob != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(current.card.imageBlob, 0, current.card.imageBlob.length);
            holder.binding.cardImage.setImageBitmap(bitmap);
        } else {
            holder.binding.cardImage.setImageResource(R.drawable.ic_action_name);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onCardClick(current.card.card));
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public void setData(List<CardWithExpensesRoom> cards) {
        this.cardList = new ArrayList<>(cards);
        notifyDataSetChanged();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(cardList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void onDragFinished() {
        listener.onOrderChanged(new ArrayList<>(cardList));
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        final ItemCardBinding binding;
        CardViewHolder(ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
