package com.android.rocket.service;

import android.util.Log;

import com.android.rocket.modal.Message;
import com.android.rocket.util.Constants;
import com.android.rocket.util.Session;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MessageListener {

    private static final String TAG = "MessageListener";
    private boolean CONTINUE = true;

    public static MessageListener messageListener;

    public interface Listener {
        void onChange(String responseData);
    }

    public void stop(){
        this.CONTINUE = false;
    }

    public void listenMessages(final int userId, final int friendId, final Listener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient mClient = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.HOURS)
                        .writeTimeout(10, TimeUnit.HOURS)
                        .readTimeout(10, TimeUnit.HOURS)
                        .build();
                Request request;
                request = new Request.Builder()
                        .url(Constants.host + "/message/" + userId + "/" + friendId)
                        .build();
                try {
                    Response response = mClient.newCall(request).execute();
                    if(response.isSuccessful()){
                        String responseData = response.body().string();
                        listener.onChange(responseData);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                request = new Request.Builder()
                        .url(Constants.host + "/message/listener/" + userId + "/" + friendId)
                        .build();
                while (CONTINUE) {
                    try {
                        Log.e(TAG, String.format("run: listening messages for %s/%s", userId, friendId));
                        Response response = mClient.newCall(request).execute();
                        if (response.isSuccessful()) {
                            //dispatch message
                            String responseData = response.body().string();
                            listener.onChange(responseData);
                            Log.e(TAG, String.format("run: message dispatched for %s/%s", userId, friendId));
                        } else {
                            Log.e(TAG, String.format("run: unsuccessful response for %s/%s", userId, friendId));
                            CONTINUE = false;
                        }
                    } catch (IOException e) {
                        if (e instanceof SocketTimeoutException) {
                        } else {
                            CONTINUE = false;
                            e.printStackTrace();
                        }
                    }
                }
                Log.e(TAG, String.format("run: stopped listening messages for %s/%s", userId, friendId));
            }
        }).start();
    }
}
