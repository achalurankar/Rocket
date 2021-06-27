package com.android.rocket.modal;

/**
 * Modal class for user details
 * */

public class User {
    int userId;
    String username, emailId, picture;
    long pictureVersion;
    public User() {

    }

    public User(int userId, String username, String emailId, String picture, long pictureVersion) {
        this.userId = userId;
        this.username = username;
        this.emailId = emailId;
        this.picture = picture;
        this.pictureVersion = pictureVersion;
    }

    public long getPictureVersion() {
        return pictureVersion;
    }

    public void setPictureVersion(long pictureVersion) {
        this.pictureVersion = pictureVersion;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
