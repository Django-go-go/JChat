package com.jkingone.jchat.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by Jkingone on 2018/4/7.
 */

public class User extends BmobUser {

    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "avatar='" + avatar + '\'' +
                '}';
    }
}
