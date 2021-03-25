package com.android.chatapp.modal;

public class Packet {
    Data data;
    String to;

    public Packet(Data data, String to) {
        this.data = data;
        this.to = to;
    }

    public Packet() {
    }
}
