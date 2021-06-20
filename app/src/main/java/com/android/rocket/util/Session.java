package com.android.rocket.util;

import com.android.rocket.modal.GroupInfo;
import com.android.rocket.modal.User;

import org.json.JSONException;
import org.json.JSONObject;

public class Session {

    public static User LoggedInUser = new User();
    public static User mSelectedUser;
    public static GroupInfo mSelectedGroup;

    public static boolean saveUserInfo(String responseData){
        boolean result = true;
        try {
            JSONObject responseObj = new JSONObject(responseData);
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
