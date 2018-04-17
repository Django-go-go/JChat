package com.jkingone.jchat.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.promeg.pinyinhelper.Pinyin;
import com.jkingone.jchat.R;
import com.jkingone.jchat.adapter.FriendAdapter;
import com.jkingone.jchat.bean.Friend;
import com.jkingone.jchat.bean.NewFriend;
import com.jkingone.jchat.bean.Selection;
import com.jkingone.jchat.bean.User;
import com.jkingone.jchat.customview.SideBar;
import com.jkingone.jchat.data.MyContentProvider;
import com.jkingone.jchat.ui.activity.ChatActivity;
import com.jkingone.jchat.ui.activity.MainActivity;
import com.jkingone.jchat.ui.activity.NewFriendActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.jkingone.jchat.Constant.TAG;

/**
 * Created by Jkingone on 2018/4/7.
 */

public class FriendFragment extends BaseFragment {
    private static final String TAG = "FriendFragment";

    @BindView(R.id.sidebar)
    SideBar mSideBar;

    @BindView(R.id.recycle_friend)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_dialog)
    TextView mTextView_dialog;

    private FriendAdapter mFriendAdapter;

    private List<User> mUserList = new ArrayList<>();
    private List<Selection> selections = new ArrayList<>();

    private TextView mTextView;

    @Override
    protected void onLazyLoadOnce() {
        getLocalFriends();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        ButterKnife.bind(this, view);
        mSideBar.setTextViewDialog(mTextView_dialog);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mFriendAdapter = new FriendAdapter(FriendFragment.this.getContext(), selections);
        mRecyclerView.setAdapter(mFriendAdapter);
        mFriendAdapter.setListener(new FriendAdapter.OnClickListener() {
            @Override
            public void onClick(int pos) {
                if (pos == -1) {
                    startActivity(new Intent(FriendFragment.this.getActivity(), NewFriendActivity.class));
                } else {
                    Intent intent = new Intent(FriendFragment.this.getActivity(), ChatActivity.class);
                    intent.putExtra("conversationId", selections.get(pos).getUser().getObjectId());
                    intent.putExtra("conversationTitle", selections.get(pos).getUser().getUsername());
                    Log.i(TAG, "onClick: " + selections.get(pos).getUser().getObjectId());
                    Log.i(TAG, "onClick: " + selections.get(pos).getUser().getUsername());
                    startActivity(intent);
                }

            }
        });
        mTextView = new TextView(getContext());
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextSize(20);
        mTextView.setPadding(10, 10, 10, 10);
        mFriendAdapter.addFooter(mTextView);

        return view;
    }

    private void getSelections(List<User> users) {
        if (selections == null) {
            selections = new ArrayList<>();
        } else {
            selections.clear();
        }
        TreeMap<String, List<User>> treeMap = new TreeMap<>();
        List<User> other = new ArrayList<>();
        for (User user : users) {
            String str = Pinyin.toPinyin(user.getUsername().charAt(0)).toUpperCase();
            char c = str.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                List<User> list = treeMap.get(String.valueOf(c));
                if (list == null) {
                    list = new ArrayList<>();
                    treeMap.put(String.valueOf(c), list);
                }
                list.add(user);
            } else {
                other.add(user);
            }
        }

        for (Map.Entry<String, List<User>> entry : treeMap.entrySet()) {
            Selection selection = new Selection();
            selection.setLabel(true);
            selection.setCharacter(entry.getKey());
            selections.add(selection);
            for (User user : entry.getValue()) {
                Selection s = new Selection();
                s.setLabel(false);
                s.setUser(user);
                selections.add(s);
            }
        }
        Selection selection = new Selection();
        selection.setLabel(true);
        selection.setCharacter("#");
        selections.add(selection);
        for (User user : other) {
            Selection s = new Selection();
            s.setLabel(false);
            s.setUser(user);
            selections.add(s);
        }
    }

    public void getLocalFriends() {
        List<NewFriend> friends = MyContentProvider.getFriends(getContext(), 1);
        if (friends.size() > 0) {
            for (NewFriend friend : friends) {
                User user = new User();
                user.setObjectId(friend.getUid());
                user.setAvatar(friend.getAvatar());
                user.setUsername(friend.getUsername());
                mUserList.add(user);
            }
            getSelections(mUserList);
            mTextView.setText("有" + mUserList.size() + "位联系人");
            if (mFriendAdapter == null) {
                mFriendAdapter = new FriendAdapter(FriendFragment.this.getContext(), selections);
                mRecyclerView.setAdapter(mFriendAdapter);
            } else {
                mFriendAdapter.notifyDataSetChanged();
            }
        } else {
            queryFriends();
        }
    }

    public void queryFriends(){
        BmobQuery<Friend> query = new BmobQuery<>();
        BmobUser user = BmobUser.getCurrentUser();
        query.addWhereEqualTo("user", user.getObjectId());
        query.include("friend");
        query.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                if (e == null) {
                    if (list == null) {
                        Log.i(TAG, "friend null");
                    } else {
                        List<String> ids = new ArrayList<>();
                        for (Friend friend : list) {
                            ids.add(friend.getFriend());
                        }
                        BmobQuery<User> bmobQuery = new BmobQuery<>();
                        bmobQuery.addWhereContainedIn("objectId", ids);
                        bmobQuery.findObjects(new FindListener<User>() {
                            @Override
                            public void done(List<User> list, BmobException e) {
                                if (e == null) {
                                    mUserList = list;
                                    getSelections(mUserList);
                                    addLocal(mUserList);
                                    mTextView.setText("有" + mUserList.size() + "位联系人");
                                    if (mFriendAdapter == null) {
                                        mFriendAdapter = new FriendAdapter(FriendFragment.this.getContext(), selections);
                                        mRecyclerView.setAdapter(mFriendAdapter);
                                    } else {
                                        mFriendAdapter.notifyDataSetChanged();
                                    }
                                    Log.i(TAG, "queryFriends  " + mUserList);
                                } else {
                                    Log.e(TAG, "queryFriends: ", e);
                                }
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "query friend fail", e);
                }
            }
        });
    }

    private void addLocal(List<User> users) {
        for (User user : users) {
            NewFriend newFriend = new NewFriend();
            newFriend.setUsername(user.getUsername());
            newFriend.setAvatar(user.getAvatar());
            newFriend.setUid(user.getObjectId());
            newFriend.setStatus(1);
            MyContentProvider.addFriend(getContext(), newFriend);
        }
    }
}
