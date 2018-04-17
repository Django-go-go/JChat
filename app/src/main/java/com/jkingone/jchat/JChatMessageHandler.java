package com.jkingone.jchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.jkingone.jchat.bean.Friend;
import com.jkingone.jchat.bean.NewFriend;
import com.jkingone.jchat.bean.User;
import com.jkingone.jchat.data.MyContentProvider;
import com.jkingone.jchat.ui.activity.AddFriendActivity;
import com.jkingone.jchat.ui.activity.MainActivity;
import com.jkingone.jchat.ui.activity.NewFriendActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Jkingone on 2018/4/6.
 */

public class JChatMessageHandler extends BmobIMMessageHandler {

    private static final String TAG = "JChatMessageHandler";

    private Context context;

    public JChatMessageHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onMessageReceive(final MessageEvent event) {
        executeMessage(event);
    }

    @Override
    public void onOfflineReceive(final OfflineMessageEvent event) {
        //每次调用connect方法时会查询一次离线消息，如果有，此方法会被调用
        Map<String, List<MessageEvent>> map = event.getEventMap();
        Log.i(TAG, "有" + map.size() + "个用户发来离线消息");
        for (Map.Entry<String, List<MessageEvent>> entry : map.entrySet()) {
            List<MessageEvent> list = entry.getValue();
            int size = list.size();
            Log.i(TAG, "用户" + entry.getKey() + "发来" + size + "条消息");
            for (int i = 0; i < size; i++) {
                executeMessage(list.get(i));
            }
        }
    }

    private void executeMessage(final MessageEvent event) {
        final BmobIMMessage msg = event.getMessage();
        final BmobIMConversation conversation = event.getConversation();
        String username = event.getFromUserInfo().getUserId();
        String title = conversation.getConversationTitle();

        switch (msg.getMsgType()) {
            case Constant.ADD_FRIEND:
                processAddFriend(msg);
                break;
            case Constant.AGREE_FRIEND:
                processAgreeFriend(msg);
                break;
            default: {
                if (username.equals(title)) {
                    BmobQuery<User> query = new BmobQuery<>();
                    query.addWhereEqualTo("objectId", username);
                    query.getObject(username, new QueryListener<User>() {
                        @Override
                        public void done(User user, BmobException e) {
                            if (e == null) {
                                String name = user.getUsername();
                                String avatar = user.getAvatar();
                                conversation.setConversationIcon(avatar);
                                conversation.setConversationTitle(name);
                                if (!msg.isTransient()) {
                                    BmobIM.getInstance().updateConversation(conversation);
                                }
                                processSDKMessage(event);
                            }
                        }
                    });
                } else {
                    processSDKMessage(event);
                }
            }
        }
    }

    private void processAddFriend(BmobIMMessage msg) {

        if (!MyContentProvider.isNotLikeFriend(context, msg.getFromId())) {
            NewFriend newFriend = new NewFriend();
            newFriend.setStatus(-1);
            newFriend.setUid(msg.getFromId());
            newFriend.setContent(msg.getContent());

            try {
                String extra = msg.getExtra();
                if (!TextUtils.isEmpty(extra)) {
                    JSONObject json = new JSONObject(extra);
                    String name = json.getString("name");
                    newFriend.setUsername(name);
                    String avatar = json.getString("avatar");
                    newFriend.setAvatar(avatar);

                } else {
                    Log.i(TAG, "processAddFriend: null");
                }
            } catch (Exception e) {
                Log.e(TAG, "processAddFriend: ", e);
            }

            MyContentProvider.addFriend(context, newFriend);

            Intent pendingIntent = new Intent(context, NewFriendActivity.class);
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background);
            BmobNotificationManager.getInstance(context).showNotification(largeIcon,
                    newFriend.getUsername(), msg.getContent(), newFriend.getUsername() + "请求添加你为朋友", pendingIntent);
        }
    }

    private void processAgreeFriend(final BmobIMMessage msg) {

        String name = null;
        String avatar = null;

        try {
            String extra = msg.getExtra();
            if (!TextUtils.isEmpty(extra)) {
                JSONObject json = new JSONObject(extra);
                name = json.getString("name");
                avatar = json.getString("avatar");
            } else {
                Log.i(TAG, "processAddFriend: null");
            }
        } catch (Exception t) {
            Log.e(TAG, "processAddFriend: ", t);
        }

        Friend friend = new Friend();
        friend.setUser(BmobUser.getCurrentUser(User.class).getObjectId());
        friend.setFriend(msg.getFromId());

        final NewFriend newFriend = new NewFriend();
        newFriend.setStatus(1);
        newFriend.setUid(msg.getFromId());
        newFriend.setContent(msg.getContent());
        newFriend.setUsername(name);
        newFriend.setAvatar(avatar);

        friend.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    MyContentProvider.addFriend(context, newFriend);

                    Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background);
                    BmobNotificationManager.getInstance(context).showNotification(largeIcon,
                            newFriend.getUsername(), msg.getContent(), newFriend.getUsername() + "已同意添加你为朋友", null);
                } else {
                    Intent pendingIntent = new Intent(context, AddFriendActivity.class);
                    pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background);
                    BmobNotificationManager.getInstance(context).showNotification(largeIcon,
                            newFriend.getUsername(), newFriend.getUsername() + "添加失败", newFriend.getUsername() + "添加失败", pendingIntent);
                }
            }
        });


    }

    private void processSDKMessage(MessageEvent event) {

        if (BmobNotificationManager.getInstance(context).isShowNotification()) {
            Intent pendingIntent = new Intent(context, MainActivity.class);
            pendingIntent.putExtra("conversationId", event.getConversation().getConversationId());
            pendingIntent.putExtra("conversationTitle", event.getConversation().getConversationTitle());
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            BmobNotificationManager.getInstance(context).showNotification(event, pendingIntent);

        } else {
            EventBus.getDefault().postSticky(event);
        }
    }

}
