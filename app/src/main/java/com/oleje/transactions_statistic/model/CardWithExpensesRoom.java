package com.oleje.transactions_statistic.model;

import androidx.room.Embedded;

public class CardWithExpensesRoom {
    @Embedded
    public Card card;
    public double totalExpenses;
}
