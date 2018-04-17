package com.jkingone.jchat.utils;

import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;

/**
 * Created by Jkingone on 2018/4/11.
 */

public final class LogUtils {

    public static String show(BmobIMConversation conversation) {
        StringBuilder stringBuilder = new StringBuilder();
        String conversationIcon = conversation.getConversationIcon();
        String conversationId = conversation.getConversationId();
        String conversationTitle = conversation.getConversationTitle();
        String conversationType = conversation.getConversationType() + "";
        String draft = conversation.getDraft();
        String id = conversation.getId() + "";
        String isTop = conversation.getIsTop() + "";
        String unreadCount = conversation.getUnreadCount() + "";
        String updateTime = conversation.getUpdateTime() + "";
        stringBuilder
                .append("ConversationIcon:").append(conversationIcon == null ? "null" : conversationIcon).append("\n")
                .append("ConversationId:").append(conversationId == null ? "null" : conversationId).append("\n")
                .append("ConversationTitle:").append(conversationTitle == null ? "null" : conversationTitle).append("\n")
                .append("ConversationType:").append(conversationType).append("\n")
                .append("draft:").append(draft == null ? "null" : draft).append("\n")
                .append("id:").append(id).append("\n")
                .append("isTop:").append(isTop).append("\n")
                .append("UnreadCount:").append(unreadCount).append("\n")
                .append("updateTime:").append(updateTime).append("\n");
        for (BmobIMMessage message : conversation.getMessages()) {
            stringBuilder.append("msg:").append(message.toString()).append("\n");
        }
        return stringBuilder.toString();
    }
}
