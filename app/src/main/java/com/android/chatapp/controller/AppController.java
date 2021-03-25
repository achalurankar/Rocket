package com.android.chatapp.controller;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

/**
 * Controller class to check if the app is in background or not
 */

public class AppController extends Application implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d("AppController", "Foreground");
        isAppInBackground(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d("AppController", "Background");
        isAppInBackground(true);
    }

    public interface ValueChangeListener {
        void onChanged(Boolean value);
    }

    private ValueChangeListener visibilityChangeListener;

    public void setOnVisibilityChangeListener(ValueChangeListener listener) {
        this.visibilityChangeListener = listener;
    }

    private void isAppInBackground(Boolean isBackground) {
        if (null != visibilityChangeListener) {
            visibilityChangeListener.onChanged(isBackground);
        }
    }

    private static AppController mInstance;

    public static AppController getInstance() {
        return mInstance;
    }

    public static final String LAST_SEEN_CHANNEL_ID = "last_seen_channel_id";
    public static final String NOTIFICATION_CHECKER_FOREGROUND_CHANNEL_ID = "notification_foreground_channel_id";
    public static final String RECEIVED_MESSAGE_NOTIFICATION_CHANNEL_ID = "received_message_notification_channel_id";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel foregroundChannel = new NotificationChannel(LAST_SEEN_CHANNEL_ID, "Sirius", NotificationManager.IMPORTANCE_NONE);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHECKER_FOREGROUND_CHANNEL_ID, "Rigel", NotificationManager.IMPORTANCE_NONE);
            NotificationChannel messageChannel = new NotificationChannel(RECEIVED_MESSAGE_NOTIFICATION_CHANNEL_ID, "Betelgeuse", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(foregroundChannel);
            manager.createNotificationChannel(notificationChannel);
            manager.createNotificationChannel(messageChannel);
        }
        // addObserver
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }
}
