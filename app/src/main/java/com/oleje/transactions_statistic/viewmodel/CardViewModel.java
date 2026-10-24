package com.oleje.transactions_statistic.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oleje.transactions_statistic.data.MyRoomDatabase;
import com.oleje.transactions_statistic.model.Card;
import com.oleje.transactions_statistic.model.CardWithExpensesRoom;
import com.oleje.transactions_statistic.repository.Repository;

import java.util.List;

public class CardViewModel extends AndroidViewModel {
    private final Repository repo;
    public final LiveData<List<CardWithExpensesRoom>> cardsWithExpenses;

    public CardViewModel(@NonNull Application application) {
        super(application);
        var db = MyRoomDatabase.getInstance(application);
        repo = new Repository(db.getTransactionRoomDao(), db.getCardRoomDao());
        cardsWithExpenses = repo.getCardsWithExpenses();
    }

    public void addCard(Card card) {
        repo.addCard(card);
    }

    public Card getCardByNumber(int cardNum) {
        return repo.getCardByNumber(cardNum);
    }

    public void deleteCard(int cardNum) {
        repo.deleteCard(cardNum);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).collection("cards").document(String.valueOf(cardNum)).delete();
        }
    }

    public void updateCard(Card card) {
        repo.updateCard(card);
    }

    public List<Card> readAllDataSync() {
        return repo.readAllCardsSync();
    }
}