package com.android.chatapp.util;

import com.android.chatapp.modal.Message;

public interface MessageListener {
    void onMessageReceived(Message message);
}

