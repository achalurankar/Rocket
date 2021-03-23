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

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        // addObserver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "ForegroundChannel",
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_NONE
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }
}
