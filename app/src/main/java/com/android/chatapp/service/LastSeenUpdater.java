package com.android.chatapp.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.chatapp.R;
import com.android.chatapp.controller.AppController;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LastSeenUpdater extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean input = intent.getBooleanExtra("isAppBackground", true);
        //foreground notification
        Notification notification = new NotificationCompat.Builder(this, AppController.LAST_SEEN_CHANNEL_ID)
                .setContentTitle("Updating messages...")
                .setSmallIcon(R.drawable.rocket_vector)
                .build();
        startForeground(1, notification);
        //set last seen status
        setUserStatus(input);
        return START_NOT_STICKY;
    }

    private void setUserStatus(boolean input) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DateFormat df = new SimpleDateFormat("h:mm aa dd/MM/yy");
            Date obj = new Date();
            final Map<String, String> map = new HashMap<>();
            if (input)
                map.put("status", "" + df.format(obj));
            else
                map.put("status", "online");
            String Email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("email", Email)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            final String[] id = new String[1];
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                id[0] = documentSnapshot.get("id").toString();
                                FirebaseFirestore.getInstance().collection("user_status")
                                        .document(id[0])
                                        .set(map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                stopSelf();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            stopSelf();
                        }
                    }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    stopSelf();
                }
            });
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