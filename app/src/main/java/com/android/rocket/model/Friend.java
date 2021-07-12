package com.android.rocket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Friend {
    private int userId;
    private String username;
    private String picture;
    private long pictureVersion;
    private RecentMessage recentMessage;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentMessage {
        private int senderId;
        private int receiverId;
        private String text;
        private int unseenCount;
        private String dateSent;
        private boolean seen;
    }
}
