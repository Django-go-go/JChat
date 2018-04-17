package com.jkingone.jchat.ui.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jkingone.jchat.Constant;
import com.jkingone.jchat.R;
import com.jkingone.jchat.bean.Friend;
import com.jkingone.jchat.bean.NewFriend;
import com.jkingone.jchat.bean.User;
import com.jkingone.jchat.data.MyContentProvider;
import com.jkingone.jchat.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMExtraMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class NewFriendActivity extends AppCompatActivity {

    @BindView(R.id.recycle_newfriend)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar_new_friend)
    Toolbar mToolbar;

    private ItemTouchHelper mItemTouchHelper;
    private LinearLayoutManager mLinearLayoutManager;
    private NewFriendAdapter mNewFriendAdapter;
    private List<NewFriend> mFriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        ScreenUtils.setTranslucent(this);
        ButterKnife.bind(this);

        initToolbar();

        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.DOWN|ItemTouchHelper.UP,
                ItemTouchHelper.START|ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                NewFriend friend = mFriends.get(viewHolder.getLayoutPosition());
                mNewFriendAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                mFriends.remove(viewHolder.getLayoutPosition());
                mNewFriendAdapter.notifyDataSetChanged();
                if (friend.getStatus() == -1) {
                    MyContentProvider.deleteFriend(NewFriendActivity.this, friend.getUid());
                }
            }
        });


        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (mNewFriendAdapter == null) {
            mNewFriendAdapter = new NewFriendAdapter();
        }
        mRecyclerView.setAdapter(mNewFriendAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getFriends();
    }

    private void initToolbar() {
        mToolbar.setTitle("新的朋友");
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(R.drawable.ac_back_icon);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getFriends() {
        mFriends = MyContentProvider.getFriends(this, -1);
    }

    private Friend agreeFriend(final NewFriend friend) {
        Friend friendTable = new Friend();
        friendTable.setUser(BmobUser.getCurrentUser(User.class).getObjectId());
        friendTable.setFriend(friend.getUid());
        return friendTable;
    }

    /**
     * 发送同意添加好友的消息
     */
    private BmobIMConversation sendAgreeAddFriendMessage(final NewFriend add) {
        BmobIMUserInfo info = new BmobIMUserInfo(add.getUid(), add.getUsername(), add.getAvatar());
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        return messageManager;
    }

    class NewFriendAdapter extends RecyclerView.Adapter<NewFriendAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(NewFriendActivity.this).inflate(R.layout.item_newfriend_recycle, parent, false));
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            final NewFriend friend = mFriends.get(position);
            holder.mTextView_name.setText(friend.getUsername());
            holder.mTextView_content.setText(friend.getContent());
            if (friend.getStatus() == -1) {
                holder.mButton_newfriend.setEnabled(true);
            } else {
                holder.mButton_newfriend.setEnabled(false);
            }
            holder.mButton_newfriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    friend.setStatus(1);
                    holder.mButton_newfriend.setText("已同意");
                    notifyDataSetChanged();

                    BmobIMUserInfo info = new BmobIMUserInfo(friend.getUid(), friend.getUsername(), friend.getAvatar());
                    //创建一个暂态会话入口，发送好友请求
                    BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
                    //根据会话入口获取消息管理，发送好友请求
                    BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);

                    BmobIMExtraMessage msg = new BmobIMExtraMessage();
                    msg.setContent("我通过了你的好友验证请求，我们可以开始聊天了!");
                    msg.setMsgType(Constant.AGREE_FRIEND);

                    Map<String, Object> map = new HashMap<>(1);
                    User currentUser = BmobUser.getCurrentUser(User.class);
                    map.put("name", currentUser.getUsername());
                    map.put("avatar", currentUser.getAvatar());
                    msg.setExtraMap(map);

                    messageManager.sendMessage(msg, new MessageSendListener() {
                        @Override
                        public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                            if (e == null) {
                                boolean ok = MyContentProvider.updateFriend(NewFriendActivity.this, friend.getUid(), 1);
                                if (ok) {
                                    agreeFriend(friend).save(new SaveListener<String>() {
                                        @Override
                                        public void done(String s, BmobException e) {
                                            if (e != null) {
                                                Log.e(Constant.TAG, "agreeFriend: fail", e);
                                                MyContentProvider.updateFriend(NewFriendActivity.this, friend.getUid(), -1);
                                                friend.setStatus(-1);
                                                holder.mButton_newfriend.setText("同意");
                                                notifyDataSetChanged();
                                                Toast.makeText(NewFriendActivity.this, "同意失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    friend.setStatus(-1);
                                    holder.mButton_newfriend.setText("同意");
                                    Toast.makeText(NewFriendActivity.this, "同意失败", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(Constant.TAG, "sendMessage: fail", e);
                                friend.setStatus(-1);
                                holder.mButton_newfriend.setText("同意");
                                notifyDataSetChanged();
                                Toast.makeText(NewFriendActivity.this, "同意失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });
        }

        @Override
        public int getItemCount() {
            return mFriends.size();
        }

        class VH extends RecyclerView.ViewHolder {

            @BindView(R.id.img_avatar_newfriend)
            ImageView mImageView_avatar;

            @BindView(R.id.tv_name_newfriend)
            TextView mTextView_name;

            @BindView(R.id.tv_content)
            TextView mTextView_content;

            @BindView(R.id.btn_newfriend)
            Button mButton_newfriend;

            public VH(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
