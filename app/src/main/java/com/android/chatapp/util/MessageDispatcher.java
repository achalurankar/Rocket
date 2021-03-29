package com.android.chatapp.util;

import com.android.chatapp.modal.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageDispatcher {
    static List<MessageListener> listeners = new ArrayList<>();

    public static void setMessageListener(MessageListener listener) {
        MessageDispatcher.listeners.add(listener);
    }

    public static void dispatchMessage(Message message) {
        for (MessageListener messageListener : MessageDispatcher.listeners) {
            messageListener.onMessageReceived(message);
        }
    }
}
