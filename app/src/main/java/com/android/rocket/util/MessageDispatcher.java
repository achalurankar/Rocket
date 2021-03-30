package com.android.rocket.util;

import com.android.rocket.modal.Message;

public class MessageDispatcher {
    static MessageListener listener;

    public static void setMessageListener(MessageListener listener) {
        MessageDispatcher.listener = listener;
    }

    public static void dispatchMessage(Message message) {
        MessageDispatcher.listener.onMessageReceived(message);
    }
}
