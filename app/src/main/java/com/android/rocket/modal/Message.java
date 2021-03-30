package com.android.rocket.modal;

/**
 * Modal class for message details
 */

public class Message {
    String ReceiverId, ReplyTo = "", ReplyToOwner = ""; //extra attributes for personal messaging
    String MessageId, Date, Time, Text, SenderId, Type, PicUrl, SenderName; //common attributes
    String GroupId, SenderPicUrl; //extra attributes for group messaging

    public Message() {
//        empty constructor
    }

    //personal messaging constructor
    public Message(String type, String picUrl, String replyTo, String replyToOwner, String messageId, String senderName, String senderId, String receiverId, String text, String date, String time) {
        Type = type;
        MessageId = messageId;
        SenderId = senderId;
        ReceiverId = receiverId;
        SenderName = senderName;
        Text = text;
        Date = date;
        Time = time;
        ReplyTo = replyTo;
        ReplyToOwner = replyToOwner;
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

    public String getReplyTo() {
        return ReplyTo;
    }

    public void setReplyTo(String replyTo) {
        ReplyTo = replyTo;
    }

    public String getReplyToOwner() {
        return ReplyToOwner;
    }

    public void setReplyToOwner(String replyToOwner) {
        ReplyToOwner = replyToOwner;
    }
}
