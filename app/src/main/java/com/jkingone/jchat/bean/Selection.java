package com.jkingone.jchat.bean;

/**
 * Created by Jkingone on 2018/4/10.
 */

public class Selection {
    private boolean isLabel;
    private User user;
    private String character;

    public boolean isLabel() {
        return isLabel;
    }

    public void setLabel(boolean label) {
        isLabel = label;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    @Override
    public String toString() {
        return "Selection{" +
                "isLabel=" + isLabel +
                ", user=" + user +
                ", character='" + character + '\'' +
                '}';
    }
}
