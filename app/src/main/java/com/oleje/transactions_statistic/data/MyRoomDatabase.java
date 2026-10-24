package com.oleje.transactions_statistic.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.TypeConverters;

import com.oleje.transactions_statistic.model.Card;
import com.oleje.transactions_statistic.model.InterceptedNotification;
import com.oleje.transactions_statistic.model.Transaction;
import com.oleje.transactions_statistic.util.Converters;

@Database(entities = {Transaction.class, Card.class, InterceptedNotification.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MyRoomDatabase extends androidx.room.RoomDatabase {
    private static volatile MyRoomDatabase INSTANCE;
    public abstract TransactionRoomDao getTransactionRoomDao();
    public abstract CardRoomDao getCardRoomDao();
    public abstract InterceptedNotificationDao getInterceptedNotificationDao();

    public static synchronized MyRoomDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MyRoomDatabase.class,
                    "app_database"
            ).fallbackToDestructiveMigration(true) //ПРИБРАТИ ЦЕ ПОТІМ
            .build();
        }
        return INSTANCE;
    }
}