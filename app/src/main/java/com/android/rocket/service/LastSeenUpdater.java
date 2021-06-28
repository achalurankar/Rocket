package com.android.rocket.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.rocket.R;
import com.android.rocket.controller.AppController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LastSeenUpdater extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean input = intent.getBooleanExtra("isAppBackground", true);
        //foreground notification
        Notification notification = new NotificationCompat.Builder(this, AppController.LAST_SEEN_CHANNEL_ID)
                .setContentTitle("Updating messages...")
                .setSmallIcon(R.drawable.rocket_vector)
                .build();
        startForeground(1, notification);
        //set last seen status
        setUserStatus(input);
        return START_NOT_STICKY;
    }

    private void setUserStatus(boolean input) {
        //if user is logged in
        if (true) {
            DateFormat df = new SimpleDateFormat("h:mm aa dd/MM/yy");
            Date obj = new Date();
            final Map<String, String> map = new HashMap<>();
            if (input)
                map.put("status", "" + df.format(obj));
            else
                map.put("status", "online");
            //update last seen
        } else
            stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}