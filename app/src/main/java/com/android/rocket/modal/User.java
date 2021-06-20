package com.android.rocket.modal;

/**
 * Modal class for user details
 * */

public class User {
    String Id, Name, Email,PicUrl;
    int userId;
    String username, emailId, picture;
    public User() {

    }

    public User(int userId, String username, String emailId, String picture) {
        this.userId = userId;
        this.username = username;
        this.emailId = emailId;
        this.picture = picture;
    }

    public User(String id, String name, String username, String email, String picUrl) {
        Id = id;
        Name = name;
        this.username = username;
        Email = email;
        PicUrl = picUrl;
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

    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return Email;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
