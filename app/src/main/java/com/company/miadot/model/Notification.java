package com.company.miadot.model;

import java.util.Map;

public class Notification {
    public String id;
    public String type; // like, comment, reply, follow, mention, share, invite, reminder, message, adoption_interest
    public boolean read;
    public long timestamp;
    public String senderId;
    public String senderName;
    public String senderPhoto;
    public String postId;
    public String commentId;
    public String groupId;
    public String text;
    public Map<String, Object> extra;

    public Notification() {}
}

