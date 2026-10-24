package com.oleje.transactions_statistic.data;
import com.oleje.transactions_statistic.model.CardWithExpensesRoom;
import com.oleje.transactions_statistic.model.Transaction;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface TransactionRoomDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addTransaction(Transaction transaction);

    @Delete
    void deleteTransactions(List<Transaction> transactions);

    @Update
    void updateTransaction(Transaction transaction);
    @Query("SELECT SUM(value) FROM transaction_table WHERE card = :cardNum")
    LiveData<Double> getTotalExpensesForCard(int cardNum);

    @Query("SELECT * FROM transaction_table WHERE card = :cardNum AND date >= :startOfMonth AND date <= :endOfMonth ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsForMonth(int cardNum, long startOfMonth, long endOfMonth);

    @Query("SELECT * FROM transaction_table WHERE card = :cardNum ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactionsForCard(int cardNum);

    @Query("SELECT * FROM transaction_table ORDER BY date DESC")
    List<Transaction> readAllTransactionsList();

    @Query("SELECT * FROM transaction_table ORDER BY date DESC")
    LiveData<List<Transaction>> readAllDataSync();

    @Query("SELECT card_table.*, (SELECT SUM(value) FROM transaction_table WHERE transaction_table.card = card_table.card) as totalExpenses FROM card_table ORDER BY orderIndex ASC")
    LiveData<List<CardWithExpensesRoom>> getCardsWithExpenses();

    @Query("SELECT card_table.*, (SELECT SUM(value) FROM transaction_table WHERE transaction_table.card = card_table.card) as totalExpenses FROM card_table WHERE card = :cardNum LIMIT 1")
    LiveData<CardWithExpensesRoom> getCardWithExpenses(int cardNum);
}
