package com.company.miadot.model;

import java.util.List;
import java.util.Map;

public class Message {
    public String id;
    public String text;
    public String type; // text, image, video, audio, sticker, post
    public String mediaUrl;
    public String senderId;
    public long timestamp;
    public List<String> readBy;
    public Map<String, String> reactions; // userId -> emoji
    public String replyTo;
    public boolean forwarded;

    public Message() {}
}

