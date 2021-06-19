package com.android.rocket.util;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class TokenRefresher {

    private static final String TAG = "TokenRefresher";

    public static void updateToken(final String id){
        Log.e(TAG, "updateToken() called for id " + id);
        Task<String> tokenTask = FirebaseMessaging.getInstance().getToken();
        tokenTask.addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                HashMap<String, String> map = new HashMap<>();
                map.put("token", s);
                FirebaseFirestore.getInstance().collection("token")
                        .document(id)
                        .set(map);
            }
        });
    }
}
