package com.oleje.transactions_statistic.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oleje.transactions_statistic.data.MyRoomDatabase;
import com.oleje.transactions_statistic.model.CardWithExpensesRoom;
import com.oleje.transactions_statistic.model.Transaction;
import com.oleje.transactions_statistic.repository.Repository;

import java.util.List;

public class TransactionViewModel extends AndroidViewModel {
    private final Repository repo;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        var db = MyRoomDatabase.getInstance(application);
        repo = new Repository(db.getTransactionRoomDao(), db.getCardRoomDao());
    }

    public LiveData<List<Transaction>> getTransactionsForMonth(int cardNum, long start, long end) {
        return repo.getTransactionsForMonth(cardNum, start, end);
    }

    public LiveData<List<Transaction>> getAllTransactionsForCard(int cardNum) {
        return repo.getAllTransactionsForCard(cardNum);
    }
    public LiveData<List<Transaction>> getAllTransactionsSync() {
        return repo.getAllTransactionsSync();
    }

    public List<Transaction> getAllTransactionsListSync() {
        return repo.getAllTransactionsListSync();
    }
    public LiveData<CardWithExpensesRoom> getCardWithExpenses(int cardNum) {
        return repo.getCardWithExpenses(cardNum);
    }
    public void addTransaction(Transaction transaction) {
        repo.addTransaction(transaction);
    }

    public void deleteTransactions(List<Transaction> transactions) {
        repo.deleteTransactions(transactions);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            for (Transaction t : transactions) {
                // Same ID logic as in startSync
                String docId = t.date.getTime() + "_" + t.card + "_" + Double.doubleToLongBits(t.value * 100);
                db.collection("users").document(uid).collection("transactions").document(docId).delete();
            }
        }
    }
}