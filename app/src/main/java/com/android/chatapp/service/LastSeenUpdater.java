package com.android.chatapp.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.chatapp.R;
import com.android.chatapp.activity.RootChatsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LastSeenUpdater extends Service {

    private static final String CHANNEL_ID = "ForegroundChannel";

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean input = intent.getBooleanExtra("isAppBackground", true);
        System.out.println("intent extra " + input);
        Intent notificationIntent = new Intent(this, RootChatsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Rocket")
                .setContentText("Updating messages...")
                .setSmallIcon(R.drawable.rocket_vector)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
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
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                FirebaseFirestore.getInstance().collection("user_status")
                                        .document(documentSnapshot.get("id").toString())
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
                    });
        }
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