package com.company.miadot.model;

import java.util.List;

public class Conversation {
    public String id;
    public List<String> participants;
    public boolean isGroup;
    public String groupName;
    public String groupPhoto;
    public Message lastMessage;
    public boolean pinned;
    public boolean archived;
    public int unreadCount;
    public String user1Id;
    public String user2Id;

    public Conversation() {}

    public String getUser1Id() { return user1Id; }
    public void setUser1Id(String user1Id) { this.user1Id = user1Id; }
    public String getUser2Id() { return user2Id; }
    public void setUser2Id(String user2Id) { this.user2Id = user2Id; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getGroupPhoto() { return groupPhoto; }
    public void setGroupPhoto(String groupPhoto) { this.groupPhoto = groupPhoto; }
    public Message getLastMessage() { return lastMessage; }
    public void setLastMessage(Message lastMessage) { this.lastMessage = lastMessage; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
