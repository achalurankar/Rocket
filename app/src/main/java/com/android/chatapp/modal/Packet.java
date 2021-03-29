package com.android.chatapp.modal;

public class Packet {
    Message data;
    String to;

    public Packet(Message data, String to) {
        this.data = data;
        this.to = to;
    }

    public Packet() {
    }
}
