package com.android.chatapp;

public class GroupInfo {
    String GroupId, GroupName, PicUrl;

    GroupInfo() {

    }

    public GroupInfo(String groupId, String groupName, String picUrl) {
        GroupId = groupId;
        GroupName = groupName;
        PicUrl = picUrl;
    }

    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }
}
