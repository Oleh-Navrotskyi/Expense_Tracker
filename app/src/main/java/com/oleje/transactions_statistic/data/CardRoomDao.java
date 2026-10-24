package com.oleje.transactions_statistic.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.oleje.transactions_statistic.model.Card;

import java.util.List;

@Dao
public interface CardRoomDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addCard(Card card);
    @Update
    void updateCard(Card card);
    @Delete
    void deleteCard(Card card);
    @Query("SELECT * FROM card_table ORDER BY orderIndex ASC")
    LiveData<List<Card>> readAllData();

    @Query("SELECT * FROM card_table ORDER BY orderIndex ASC")
    List<Card> readAllDataSync();

    @Query("SELECT * FROM card_table WHERE card = :cardNum LIMIT 1")
    Card getCardByNumber(int cardNum);

    @Query("DELETE FROM card_table WHERE card = :cardNum")
    void deleteByCardNumber(int cardNum);
}
