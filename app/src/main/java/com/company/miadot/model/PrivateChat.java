package com.company.miadot.model;

public class PrivateChat {
    private String id;
    private String user1Id;
    private String user2Id;
    private String lastMessage;
    private long lastMessageTime;
    private String lastMessageSenderId;
    private boolean user1HasUnread;
    private boolean user2HasUnread;
    private String animalId; // ID do animal que gerou a conversa (opcional)

    public PrivateChat() {}

    public PrivateChat(String user1Id, String user2Id, String animalId) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.animalId = animalId;
        this.lastMessageTime = System.currentTimeMillis();
        this.user1HasUnread = false;
        this.user2HasUnread = false;
    }

    // Getters e setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser1Id() { return user1Id; }
    public void setUser1Id(String user1Id) { this.user1Id = user1Id; }

    public String getUser2Id() { return user2Id; }
    public void setUser2Id(String user2Id) { this.user2Id = user2Id; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    public boolean isUser1HasUnread() { return user1HasUnread; }
    public void setUser1HasUnread(boolean user1HasUnread) { this.user1HasUnread = user1HasUnread; }

    public boolean isUser2HasUnread() { return user2HasUnread; }
    public void setUser2HasUnread(boolean user2HasUnread) { this.user2HasUnread = user2HasUnread; }

    public String getAnimalId() { return animalId; }
    public void setAnimalId(String animalId) { this.animalId = animalId; }

    public String getOtherUserId(String currentUserId) {
        return currentUserId.equals(user1Id) ? user2Id : user1Id;
    }

    public boolean hasUnreadFor(String userId) {
        return userId.equals(user1Id) ? user1HasUnread : user2HasUnread;
    }
}
