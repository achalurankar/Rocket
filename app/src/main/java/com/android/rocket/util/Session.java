package com.android.rocket.util;

import android.database.sqlite.SQLiteDatabase;

import com.android.rocket.model.Friend;
import com.android.rocket.model.GroupInfo;
import com.android.rocket.model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class Session {

    public static User LoggedInUser = null;
    public static Friend SelectedFriend;
    public static GroupInfo SelectedGroup;
    public static SQLiteDatabase DbRef;

    public static boolean saveUserInfo(String responseData){
        boolean result = true;
        try {
            JSONObject responseObj = new JSONObject(responseData);
            Session.LoggedInUser = new User();
            Session.LoggedInUser.setUsername(responseObj.getString("username"));
            Session.LoggedInUser.setUserId(responseObj.getInt("userId"));
            Session.LoggedInUser.setEmailId(responseObj.getString("emailId"));
            Session.LoggedInUser.setPicture(responseObj.getString("picture"));
        } catch (JSONException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }
}
