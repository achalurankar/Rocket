package com.android.chatapp;

/**
 * Modal class for message details
 * */

public class Message {
    String MessageId, SenderId, ReceiverId, Text, Date, Time;

    public Message() {
    }

    public Message(String messageId, String senderId, String receiverId, String text, String date, String time) {
        MessageId = messageId;
        SenderId = senderId;
        ReceiverId = receiverId;
        Text = text;
        Date = date;
        Time = time;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public void setMessageId(String messageId) {
        MessageId = messageId;
    }

    public void setSenderId(String senderId) {
        SenderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        ReceiverId = receiverId;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getMessageId() {
        return MessageId;
    }

    public String getSenderId() {
        return SenderId;
    }

    public String getReceiverId() {
        return ReceiverId;
    }

    public String getText() {
        return Text;
    }
}
