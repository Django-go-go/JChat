package com.jkingone.jchat.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * Created by Jkingone on 2018/4/7.
 */

public class Friend extends BmobObject{
    private String user;
    private String friend;

    public Friend() {
    }

    public Friend(String user, String friend) {
        this.user = user;
        this.friend = friend;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }
}
