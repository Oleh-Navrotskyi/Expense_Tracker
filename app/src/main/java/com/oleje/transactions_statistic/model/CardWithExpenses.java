package com.oleje.transactions_statistic.model;

public class CardWithExpenses {
    public Card card;
    public double totalExpenses;

    public CardWithExpenses(Card card, double totalExpenses) {
        this.card = card;
        this.totalExpenses = totalExpenses;
    }
}
