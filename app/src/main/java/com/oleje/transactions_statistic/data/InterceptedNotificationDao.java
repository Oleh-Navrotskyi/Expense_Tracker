package com.oleje.transactions_statistic.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.oleje.transactions_statistic.model.InterceptedNotification;
import java.util.List;

@Dao
public interface InterceptedNotificationDao {
    @Insert
    void insert(InterceptedNotification notification);

    @Query("SELECT * FROM intercepted_notifications ORDER BY date DESC LIMIT 20")
    LiveData<List<InterceptedNotification>> getAll();

    @Query("DELETE FROM intercepted_notifications")
    void deleteAll();
}
