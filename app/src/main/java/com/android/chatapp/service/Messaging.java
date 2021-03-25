package com.android.chatapp.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.chatapp.R;
import com.android.chatapp.activity.RootChatsActivity;
import com.android.chatapp.controller.AppController;
import com.android.chatapp.util.TokenRefresher;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 *  Messaging service to receive msg from the user
 * */
public class Messaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        TokenRefresher.updateToken(s);
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("Title");
        String message = remoteMessage.getData().get("Message");
        //intent for opening activity from notification
        Intent intent = new Intent(getApplicationContext(), RootChatsActivity.class);
        //wrapper intent
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, AppController.RECEIVED_MESSAGE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.rocket_vector);
        NotificationManager manager;
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
        super.onMessageReceived(remoteMessage);
    }
}
