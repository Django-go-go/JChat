package com.jkingone.jchat.ui.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jkingone.jchat.Constant;
import com.jkingone.jchat.R;
import com.jkingone.jchat.bean.User;
import com.jkingone.jchat.utils.ScreenUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMExtraMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class AddFriendActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_addfriend)
    Toolbar mToolbar;

    @BindView(R.id.edit_content)
    EditText mEditText_content;

    @BindView(R.id.btn_addfriend)
    Button mButton_addfriend;

    @BindView(R.id.edit_addfriend)
    EditText mEditText_addfiend;

    @BindView(R.id.img_avatar_friend)
    ImageView mImageView_avatar;

    @BindView(R.id.tv_name_friend)
    TextView mTextView_name;

    @BindView(R.id.relative_addfriend)
    View relativeLayout;

    private User mUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        ButterKnife.bind(this);

        ScreenUtils.setTranslucent(this);

        mEditText_addfiend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    BmobQuery<User> bmobQuery = new BmobQuery<>();
                    bmobQuery.addWhereEqualTo("username", v.getText().toString());
                    bmobQuery.setLimit(1);
                    bmobQuery.findObjects(new FindListener<User>() {
                        @Override
                        public void done(List<User> list, BmobException e) {
                            if (e == null && list != null && list.size() > 0) {
                                mUser = list.get(0);
                                mTextView_name.setText(mUser.getUsername());
                                relativeLayout.setVisibility(View.VISIBLE);
                                mButton_addfriend.setEnabled(true);
                                mButton_addfriend.setText("发送");
                                mButton_addfriend.setBackgroundColor(Color.parseColor("#FF4081"));
                                mButton_addfriend.setTextColor(Color.WHITE);
                                mEditText_content.setText("");
                            } else {
                                Toast.makeText(AddFriendActivity.this, "没有此用户", Toast.LENGTH_SHORT).show();
                                relativeLayout.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                    handled = true;
                }
                return handled;
            }
        });

        mToolbar.setTitle("添加朋友");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ac_back_icon);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.setTitleTextColor(Color.WHITE);
    }
    
    @OnClick(R.id.btn_addfriend)
    public void addfriend() {
        if (mUser != null) {
            if (BmobIM.getInstance().getCurrentStatus().getCode() != ConnectionStatus.CONNECTED.getCode()) {
                Log.i(Constant.TAG, "not connect");
                return;
            }
            BmobIMUserInfo info = new BmobIMUserInfo(mUser.getObjectId(), mUser.getUsername(), mUser.getAvatar());
            BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
            BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
            BmobIMExtraMessage msg = new BmobIMExtraMessage();
            msg.setMsgType(Constant.ADD_FRIEND);
            msg.setContent(mEditText_content.getText().toString());
            Map<String, Object> map = new HashMap<>(1);
            User currentUser = BmobUser.getCurrentUser(User.class);
            map.put("name", currentUser.getUsername());
            map.put("avatar", currentUser.getAvatar());
            msg.setExtraMap(map);
            messageManager.sendMessage(msg, new MessageSendListener() {
                @Override
                public void done(BmobIMMessage msg, BmobException e) {
                    if (e == null) {
                        mButton_addfriend.setEnabled(false);
                        mButton_addfriend.setText("已发送");
                        mButton_addfriend.setBackgroundColor(Color.WHITE);
                        mButton_addfriend.setTextColor(Color.GRAY);
                    } else {
                        Log.i(Constant.TAG, "add friend fail", e);
                        mButton_addfriend.setEnabled(true);
                        mButton_addfriend.setText("发送");
                        mButton_addfriend.setBackgroundColor(Color.parseColor("#FF4081"));
                        mButton_addfriend.setTextColor(Color.WHITE);
                        Toast.makeText(AddFriendActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
    }
}
