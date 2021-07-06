package com.android.rocket.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.rocket.R;
import com.android.rocket.controller.AppController;
import com.android.rocket.util.Client;
import com.android.rocket.util.Constants;
import com.android.rocket.util.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LastSeenUpdater extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isAppBackground = intent.getBooleanExtra("isAppBackground", true);
        //foreground notification
        Notification notification = new NotificationCompat.Builder(this, AppController.LAST_SEEN_CHANNEL_ID)
                .setContentTitle("Updating messages...")
                .setSmallIcon(R.drawable.rocket_vector)
                .build();
        startForeground(1, notification);
        //set last seen status
        setUserStatus(isAppBackground ? Constants.OFFLINE : Constants.ONLINE);
        return START_NOT_STICKY;
    }

    public void setUserStatus(int type) {
        //if user is logged in
        if (Session.LoggedInUser != null) {
            //update last seen
            @SuppressLint("SimpleDateFormat")
            DateFormat df = new SimpleDateFormat("h:mm aa dd/MM/yy");
            Date obj = new Date();
            try {
                final JSONObject map = new JSONObject();
                map.put("userId", Session.LoggedInUser.getUserId());
                switch (type) {
                    case Constants.OFFLINE:
                        map.put("status", "" + df.format(obj));
                        break;
                    case Constants.ONLINE:
                        map.put("status", "online");
                        break;
                    case Constants.TYPING:
                        String status = "typing_" + Session.SelectedUser.getUserId();
                        map.put("status", status);
                        break;
                }
                RequestBody requestBody = RequestBody.create(Client.JSON, String.valueOf(map));
                final Request request = new Request.Builder()
                        .method("POST", requestBody)
                        .url(Constants.host + "/user/status")
                        .addHeader("Content-Type", "application/json")
                        .build();

                final OkHttpClient client = new OkHttpClient.Builder().build();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.newCall(request).execute();
                            stopSelf();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
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