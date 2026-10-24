package com.oleje.transactions_statistic.repository;

import androidx.lifecycle.LiveData;
import com.oleje.transactions_statistic.data.CardRoomDao;
import com.oleje.transactions_statistic.data.TransactionRoomDao;
import com.oleje.transactions_statistic.model.Card;
import com.oleje.transactions_statistic.model.CardWithExpensesRoom;
import com.oleje.transactions_statistic.model.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {
    private final TransactionRoomDao transactionDao;
    private final CardRoomDao cardDao;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public Repository(TransactionRoomDao transactionDao, CardRoomDao cardDao) {
        this.transactionDao = transactionDao;
        this.cardDao = cardDao;
    }

    public void addTransaction(Transaction transaction) {
        executorService.execute(() -> transactionDao.addTransaction(transaction));
    }

    public void deleteTransactions(List<Transaction> transactions) {
        executorService.execute(() -> transactionDao.deleteTransactions(transactions));
    }

    public void updateTransaction(Transaction transaction) {
        executorService.execute(() -> transactionDao.updateTransaction(transaction));
    }
    public LiveData<Double> getTotalExpensesForCard(int cardNum) {
        return transactionDao.getTotalExpensesForCard(cardNum);
    }

    public LiveData<List<Transaction>> getTransactionsForMonth(int cardNum, long start, long end) {
        return transactionDao.getTransactionsForMonth(cardNum, start, end);
    }

    public LiveData<List<Transaction>> getAllTransactionsForCard(int cardNum) {
        return transactionDao.getAllTransactionsForCard(cardNum);
    }

    public LiveData<List<Transaction>> getAllTransactionsSync() {
        return transactionDao.readAllDataSync();
    }

    public List<Transaction> getAllTransactionsListSync() {
        return transactionDao.readAllTransactionsList();
    }

    public LiveData<List<CardWithExpensesRoom>> getCardsWithExpenses() {
        return transactionDao.getCardsWithExpenses();
    }

    public LiveData<CardWithExpensesRoom> getCardWithExpenses(int cardNum) {
        return transactionDao.getCardWithExpenses(cardNum);
    }

    public void addCard(Card card) {
        executorService.execute(() -> cardDao.addCard(card));
    }

    public Card getCardByNumber(int cardNum) {
        return cardDao.getCardByNumber(cardNum);
    }

    public void deleteCard(int cardNum) {
        executorService.execute(() -> cardDao.deleteByCardNumber(cardNum));
    }

    public void updateCard(Card card) {
        executorService.execute(() -> cardDao.updateCard(card));
    }

    public LiveData<List<Card>> readAllCards() {
        return cardDao.readAllData();
    }

    public List<Card> readAllCardsSync() {
        return cardDao.readAllDataSync();
    }
}
