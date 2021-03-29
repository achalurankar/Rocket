package com.android.chatapp.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.chatapp.R;
import com.android.chatapp.activity.RootChatsActivity;
import com.android.chatapp.activity.SplashScreen;
import com.android.chatapp.controller.AppController;
import com.android.chatapp.modal.Message;
import com.android.chatapp.util.MessageDispatcher;
import com.android.chatapp.util.TokenRefresher;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.EventListener;
import java.util.Map;

/**
 * Messaging service to receive msg from the user
 */
public class Messaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        TokenRefresher.updateToken(s);
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> map = remoteMessage.getData();
        String type, picUrl, replyTo, replyToOwner, messageId, senderName, senderId, receiverId, text, date, time;
        type = map.get("Type");
        picUrl = map.get("PicUrl");
        replyTo = map.get("ReplyTo");
        replyToOwner = map.get("ReplyToOwner");
        messageId = map.get("MessageId");
        senderName = map.get("SenderName");
        senderId = map.get("SenderId");
        receiverId = map.get("ReceiverId");
        text = map.get("Text");
        date = map.get("Date");
        time = map.get("Time");
        Message message = new Message(type, picUrl, replyTo, replyToOwner, messageId, senderName, senderId, receiverId, text, date, time);
        MessageDispatcher.dispatchMessage(message);
        SharedPreferences preferences = getSharedPreferences("scutiPreferences", Context.MODE_PRIVATE);
        String currentRecipient = preferences.getString("currentlyOpenedRecipient","0");
        if(currentRecipient != null && !currentRecipient.equals(senderId))
            generateNotification(senderName, text);
        super.onMessageReceived(remoteMessage);
    }

    private void generateNotification(String senderName, String text) {
        //intent for opening activity from notification
        Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
        intent.setAction("Notification");
        //wrapper intent
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, AppController.RECEIVED_MESSAGE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(senderName)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(senderName)
                .setSmallIcon(R.drawable.rocket_vector);
        NotificationManager manager;
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
    }
}

//        SharedPreferences preferences = getSharedPreferences("NotificationIdCounter",Context.MODE_PRIVATE);
//        int count = preferences.getInt("count",0);
//        preferences.edit().putInt("count", count).apply();