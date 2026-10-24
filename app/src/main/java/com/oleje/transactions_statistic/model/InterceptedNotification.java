package com.oleje.transactions_statistic.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "intercepted_notifications")
public class InterceptedNotification {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String packageName;
    public String title;
    public String text;
    public Date date;
}
