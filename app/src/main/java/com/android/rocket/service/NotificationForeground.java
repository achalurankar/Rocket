package com.android.rocket.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.rocket.R;
import com.android.rocket.activity.RootChatsActivity;
import com.android.rocket.controller.AppController;

/**
 * Foreground service for notification when the app is in background
 * */
public class NotificationForeground extends Service {

    private NotificationManagerCompat notificationManager;
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals(ACTION_STOP_SERVICE)){
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
        }
        notificationManager = NotificationManagerCompat.from(this);
        PendingIntent rootChatIntent = PendingIntent.getActivity(this, 1, new Intent(this, RootChatsActivity.class), 0);
        Intent serviceIntent = new Intent(this, NotificationForeground.class);
        serviceIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent stopServicePendingIntent = PendingIntent.getService(this, 1, serviceIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, AppController.NOTIFICATION_CHECKER_FOREGROUND_CHANNEL_ID)
                .setContentTitle("Checking for new messages...")
                .setSmallIcon(R.drawable.rocket_vector)
                .setContentIntent(rootChatIntent)
                .addAction(R.drawable.rocket_vector,"DISMISS", stopServicePendingIntent)
                .build();
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
