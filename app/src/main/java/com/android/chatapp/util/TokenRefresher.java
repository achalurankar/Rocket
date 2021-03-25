package com.android.chatapp.util;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class TokenRefresher {

    public static void updateToken(final String id){
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
