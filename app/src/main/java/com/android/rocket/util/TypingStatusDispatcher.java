package com.android.rocket.util;

import com.android.rocket.modal.Message;

public class TypingStatusDispatcher {
    private static TypingStatusListener mListener;

    public static void setTypingStatusListener(TypingStatusListener listener){
        mListener = listener;
    }

    public static void dispatchStatus(Message message){
        mListener.onStatusReceived(message);
    }

    public interface TypingStatusListener {
        void onStatusReceived(Message message);
    }
}
