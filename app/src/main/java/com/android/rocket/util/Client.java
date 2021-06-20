package com.android.rocket.util;

import okhttp3.MediaType;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String url) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
}
