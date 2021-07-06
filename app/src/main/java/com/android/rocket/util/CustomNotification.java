package com.android.rocket.util;

import com.android.rocket.model.Packet;
import com.android.rocket.model.ResponseBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CustomNotification {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAjz_R3MU:APA91bGE22fNZZyHlO4icIjB1ZpyJWvG5jxeOkQ0UGbvzXC6U8e0YqGz1VsEWRj9gEP91BMK2AUth-LVdUL7FIu5Q-fOFTsMoXXmIHP7iOwwLEDCiXrOwS2V6Nv_fVv6GUaGcQaTSje5"
            }
    )

    @POST("fcm/send")
    Call<ResponseBody> sendNotification(@Body Packet body);
}
