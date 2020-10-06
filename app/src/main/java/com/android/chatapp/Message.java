package com.android.chatapp;

/**
 * Modal class for message details
 */

public class Message {
    String ReceiverId, Seen; //extra attributes for personal messaging
    String MessageId, Date, Time, Text, SenderId, Type, PicUrl; //common attributes
    String GroupId, SenderName, SenderPicUrl; //extra attributes for group messaging

    public Message() {
//        empty constructor
    }

    //personal messaging constructor
    public Message(String type, String picUrl, String messageId, String senderId, String receiverId, String text, String date, String time, String seen) {
        Type = type;
        MessageId = messageId;
        SenderId = senderId;
        ReceiverId = receiverId;
        Text = text;
        Date = date;
        Time = time;
        Seen = seen;
        PicUrl = picUrl;
    }

    //group messaging constructor
    public Message(String type, String picUrl, String messageId, String groupId, String senderName, String senderId, String senderPicUrl, String date, String time, String text) {
        Type = type;
        MessageId = messageId;
        Text = text;
        Date = date;
        Time = time;
        SenderId = senderId;
        GroupId = groupId;
        SenderPicUrl = senderPicUrl;
        SenderName = senderName;
        PicUrl = picUrl;
    }

    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getType() {
        return Type;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    public void setSenderPicUrl(String senderPicUrl) {
        SenderPicUrl = senderPicUrl;
    }

    public String getSenderPicUrl() {
        return SenderPicUrl;
    }

    public String getSenderName() {
        return SenderName;
    }

    public void setSenderName(String senderName) {
        SenderName = senderName;
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

    public String getSeen() {
        return Seen;
    }

    public void setSeen(String seen) {
        Seen = seen;
    }
}
