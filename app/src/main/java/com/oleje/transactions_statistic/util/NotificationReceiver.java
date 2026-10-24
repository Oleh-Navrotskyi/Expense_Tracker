package com.oleje.transactions_statistic.util;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.Context;
import android.content.SharedPreferences;
import com.oleje.transactions_statistic.data.MyRoomDatabase;
import com.oleje.transactions_statistic.model.Transaction;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationReceiver extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String targetApp = prefs.getString("target_app", "Google Wallet");
        boolean recordNotifications = prefs.getBoolean("record_notifications", false);
        
        String packageName = sbn.getPackageName();

        // Name of the merchant is usually in the title
        CharSequence titleCs = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence textCs = sbn.getNotification().extras.getCharSequence("android.text");
        
        String title = titleCs != null ? titleCs.toString() : "";
        String text = textCs != null ? textCs.toString() : "";
        
        if (recordNotifications) {
            saveInterceptedNotification(packageName, title, text);
        }

        boolean isFromTarget = packageName.toLowerCase().contains(targetApp.toLowerCase().replace(" ", "")) 
                || title.toLowerCase().contains(targetApp.toLowerCase());

        if (isFromTarget && !text.isEmpty()) {
            parseAndSave(this, title, text);
        }
    }

    private void saveInterceptedNotification(String packageName, String title, String text) {
        com.oleje.transactions_statistic.model.InterceptedNotification notification = new com.oleje.transactions_statistic.model.InterceptedNotification();
        notification.packageName = packageName;
        notification.title = title;
        notification.text = text;
        notification.date = new Date();

        Executors.newSingleThreadExecutor().execute(() -> {
            MyRoomDatabase.getInstance(getApplicationContext()).getInterceptedNotificationDao().insert(notification);
        });
    }

    public static void parseAndSave(Context context, String title, String message) {
        // Pattern for: "399,99 грн - Visa PrivatBank **3108"
        // Group 1: Value, Group 2: Card Description, Group 3: Last 4 digits of card
        Pattern pattern = Pattern.compile("([\\d,.]+)\\s*грн\\s*-\\s*(.*?)\\s*\\*\\*(\\d{4})");
        Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            try {
                String valueStr = matcher.group(1).replace(",", ".");
                double value = Double.parseDouble(valueStr);
                int cardNum = Integer.parseInt(matcher.group(3));

                Transaction transaction = new Transaction();
                transaction.card = cardNum;
                transaction.value = value;
                // Merchant name is in the title
                transaction.receiver = title;
                transaction.date = new Date();

                Executors.newSingleThreadExecutor().execute(() -> {
                    MyRoomDatabase.getInstance(context.getApplicationContext()).getTransactionRoomDao().addTransaction(transaction);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
