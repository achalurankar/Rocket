package com.android.rocket.util;

public interface Constants {
    String TYPING_STATUS = "typing_status";

    /**
     *  aws ec2 instance ip address
     * */
    String ip = "3.108.193.33";

    /**
     *  localhost ip address
     * */
    String localhost = "10.0.2.2";

    /**
     *  Api endpoint
     *  either aws ec2 instance ip
     *  or localhost
     * */
    String host = "http://" + localhost + ":8080/api/v1";

    String ROCKET_PREFERENCES = "rocket_preferences";
    String USER_INFO_JSON = "UserInfo";

    /**
     * user profile pictures table variables
     */
    String TABLE_USER_PROFILE_PICTURES = "user_profile_pictures";
    String FIELD_USER_ID = "user_id";
    String FIELD_VERSION = "version";
    String FIELD_FILE_PATH = "path";

    /**
     * chat logs pictures table variables
     */
    String TABLE_CHAT_LOGS_PICTURES = "chat_logs_pictures";
    String FIELD_MESSAGE_ID = "message_id";

    int TYPING = 1;
    int OFFLINE = 2;
    int ONLINE = 3;
}
