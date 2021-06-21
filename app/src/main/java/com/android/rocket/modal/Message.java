package com.android.rocket.modal;

/**
 * Modal class for message details
 */

public class Message {

    public String messageId;
    public String conversationId;
    public int senderId;
    public int receiverId;
    public String picture;
    public String text;
    public String type;
    public boolean seen;
    public String dateSent;
    public String dateUpdated;

    public Message() {
//        empty constructor
    }

    public Message(String messageId, String conversationId, int senderId, int receiverId, String picture, String text, String type, boolean seen, String dateSent, String dateUpdated) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.picture = picture;
        this.text = text;
        this.type = type;
        this.seen = seen;
        this.dateSent = dateSent;
        this.dateUpdated = dateUpdated;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getDateSent() {
        return dateSent;
    }

    public void setDateSent(String dateSent) {
        this.dateSent = dateSent;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
