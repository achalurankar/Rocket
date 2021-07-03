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

    public interface Listener {
        void onChange(String responseData);
    }

    public void stop(){
        Log.e(TAG, "stop requested");
        this.CONTINUE = false;
    }

    public void listenMessages(final int userId, final int friendId, final Listener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient mClient = new OkHttpClient.Builder()
                        .build();
                Request request;
                request = new Request.Builder()
                        .url(Constants.host + "/message/" + userId + "/" + friendId)
                        .build();
                String oldData = null;
                while (CONTINUE) {
                    try {
//                        Log.e(TAG, String.format("run: listening messages for %s/%s", userId, friendId));
                        Response response = mClient.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            if(oldData == null){
                                oldData = responseData;
                                //dispatch message
                                listener.onChange(responseData);
                            } else {
                                if(!oldData.equals(responseData)){
                                    //dispatch message
                                    listener.onChange(responseData);
//                                    Log.e(TAG, String.format("run: message dispatched for %s/%s", userId, friendId));
                                    oldData = responseData;
                                }

                            }
                        } else {
//                            Log.e(TAG, String.format("run: unsuccessful response for %s/%s", userId, friendId));
                            CONTINUE = false;
                        }
                        Thread.sleep(1000);
                    } catch (IOException | InterruptedException e) {
                        if (e instanceof InterruptedException) {
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
