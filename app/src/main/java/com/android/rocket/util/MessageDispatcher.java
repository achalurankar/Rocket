package com.android.rocket.util;

import com.android.rocket.modal.Message;

public class MessageDispatcher {
    private static MessageListener listener;

    public static void setMessageListener(MessageListener listener) {
        MessageDispatcher.listener = listener;
    }

    public static void dispatchMessage(Message message) {
        MessageDispatcher.listener.onMessageReceived(message);
    }

    public interface MessageListener {
        void onMessageReceived(Message message);
    }
}
