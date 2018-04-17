package com.jkingone.jchat.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.jkingone.jchat.R;
import com.jkingone.jchat.data.MyContentProvider;
import com.jkingone.jchat.utils.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;

public class UserInfoActivity extends AppCompatActivity {

    private static final String TAG = "UserInfoActivity";

    @BindView(R.id.switch_top)
    Switch mSwitch_top;

    @BindView(R.id.switch_silent)
    Switch mSwitch_silent;

    @BindView(R.id.switch_reject)
    Switch mSwitch_reject;

    @BindView(R.id.relative_clear)
    RelativeLayout mRelativeLayout;

    @BindView(R.id.tv_userinfo_name)
    TextView mTextView_name;

    @BindView(R.id.img_userinfo_avatar)
    ImageView mImageView_avatar;

    @BindView(R.id.toolbar_userinfo)
    Toolbar mToolbar;

    private String id;

    private String title;

    private BmobIMConversation mConversation;

    private LoadTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        ButterKnife.bind(this);

        ScreenUtils.setTranslucent(this);

        initToolbar();

        Intent intent = getIntent();
        id = intent.getStringExtra("conversationId");
        title = intent.getStringExtra("conversationTitle");

        mTextView_name.setText(title);

        if (MyContentProvider.isNotLikeFriend(this, id)) {
            mSwitch_reject.setChecked(true);
        }

        mSwitch_silent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        mSwitch_reject.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MyContentProvider.updateFriend(UserInfoActivity.this, id, 0);
                } else {
                    MyContentProvider.updateFriend(UserInfoActivity.this, id, 1);
                }
            }
        });

        mSwitch_top.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mConversation != null) {
                    if (isChecked) {
                        mConversation.setIsTop(true);
                        BmobIM.getInstance().updateConversation(mConversation);
                    } else {
                        mConversation.setIsTop(false);
                        BmobIM.getInstance().updateConversation(mConversation);
                    }
                }
            }
        });
    }

    private void initToolbar() {
        mToolbar.setTitle("聊天设置");
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

    @Override
    protected void onStart() {
        super.onStart();
        if (mTask == null) {
            mTask = new LoadTask(new WeakReference<>(this));
        }
        mTask.execute(id);
    }

    @OnClick(R.id.relative_clear)
    public void clearChat() {
        if (mConversation != null) {
            mConversation.resetMessages();
        }
    }

    private static class LoadTask extends AsyncTask<String, Integer, BmobIMConversation> {

        WeakReference<UserInfoActivity> mReference;

        LoadTask(WeakReference<UserInfoActivity> reference) {
            mReference = reference;
        }

        @Override
        protected BmobIMConversation doInBackground(String... strings) {
            if (strings != null && strings.length > 0) {
                List<BmobIMConversation> list = BmobIM.getInstance().loadAllConversation();
                for (BmobIMConversation conversation : list) {
                    if (conversation.getConversationId().equals(strings[0])) {
                        return conversation;
                    }
                }
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(BmobIMConversation conversation) {
            mReference.get().mConversation = conversation;
            if (conversation != null && conversation.getIsTop() != null) {
                mReference.get().mSwitch_top.setChecked(conversation.getIsTop());
            } else {
                mReference.get().mSwitch_top.setChecked(false);
            }
        }
    }
}
