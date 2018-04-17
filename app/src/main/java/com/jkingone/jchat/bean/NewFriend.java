package com.jkingone.jchat.bean;

/**
 * Created by Jkingone on 2018/4/8.
 */

public class NewFriend {
    private String uid;
    private String username;
    private String avatar;
    private int status;//1 同意请求 -1 还未同意 0 黑名单
    private String content;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "NewFriend{" +
                "uid='" + uid + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                ", status=" + status +
                ", content='" + content + '\'' +
                '}';
    }
}
