package com.android.rocket.service;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomListener {

    private static final String TAG = "MessageListener";
    private boolean CONTINUE = true;

    public interface EventListener {
        void onChange(String responseData);
    }

    public void stop(){
        Log.e(TAG, "stop requested");
        this.CONTINUE = false;
    }

    public void listenMessages(final String url, final EventListener eventListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient mClient = new OkHttpClient.Builder()
                        .build();
                Request request;
                request = new Request.Builder()
                        .url(url)
                        .build();
                String oldData = null;
                while (CONTINUE) {
                    try {
                        Response response = mClient.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            if(oldData == null){
                                oldData = responseData;
                                //dispatch message
                                eventListener.onChange(responseData);
                            } else {
                                if(!oldData.equals(responseData)){
                                    //dispatch message
                                    eventListener.onChange(responseData);
                                    oldData = responseData;
                                }

                            }
                        } else {
                            Log.e(TAG, "run: unsuccessful response for url=" + url);
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
                Log.e(TAG, "run: stopped listening messages for url=" + url);
            }
        }).start();
    }
}
