package com.android.chatapp;

/**
 * Modal class for user details
 * */

public class User {
    String Id, Name, Username, Email,PicUrl;

    public User() {

    }

    public User(String id, String name, String username, String email, String picUrl) {
        Id = id;
        Name = name;
        Username = username;
        Email = email;
        PicUrl = picUrl;
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
        return Username;
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
        Username = username;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
