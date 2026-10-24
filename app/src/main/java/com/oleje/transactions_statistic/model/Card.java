package com.oleje.transactions_statistic.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "card_table")
public class Card {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int card; //останні 4 цифри
    public byte[] imageBlob;
    public int orderIndex;
}

