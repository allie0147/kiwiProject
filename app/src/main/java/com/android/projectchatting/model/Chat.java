package com.android.projectchatting.model;

public class Chat {
    private String id;
    private String sender;
    private String receiver;
    private String message;
    private long wDate;
    private String profileUrl;
    private String imageUrl;
    private static final int UNREAD = 1;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, long wDate, String profileUrl, String imageUrl) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.wDate = wDate;
        this.profileUrl = profileUrl;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getwDate() {
        return wDate;
    }

    public void setwDate(long wDate) {
        this.wDate = wDate;
    }

    public static int getUNREAD() {
        return UNREAD;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", message='" + message + '\'' +
                ", wDate=" + wDate +
                ", profileUrl='" + profileUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
