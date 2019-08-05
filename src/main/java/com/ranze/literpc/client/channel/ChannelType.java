package com.ranze.literpc.client.channel;

public enum ChannelType {
    SHORT("short"),
    POOLED("pooled");

    private String type;

    ChannelType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ChannelType{" +
                "type='" + type + '\'' +
                '}';
    }
}
