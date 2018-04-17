package com.jkingone.jchat.ui.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jkingone.jchat.R;
import com.jkingone.jchat.ui.activity.SplashActivity;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    @BindView(R.id.btn_clearCache)
    TextView mTextView_clearCache;

    @BindView(R.id.btn_clearChat)
    TextView mTextView_clearChat;

    @BindView(R.id.btn_password)
    TextView mTextView_password;

    @BindView(R.id.btn_logout)
    Button mButton_logout;

    @BindView(R.id.btn_connect)
    TextView mTextView_connect;

    @BindView(R.id.img_set_avatar)
    ImageView mImageView_avatar;

    @BindView(R.id.tv_set_name)
    TextView mTextView_name;

    private AlertDialog mAlertDialog;

    private View mView;

    private EditText mEditText_old;
    private EditText mEditText_new;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        mTextView_name.setText(BmobUser.getCurrentUser().getUsername());
        if (isConnected()) {
            mTextView_connect.setText("当前状态:在线");
        } else {
            mTextView_connect.setText("当前状态:离线");
        }
        mView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_updatepassword, null, false);
        mEditText_old = (EditText) mView.findViewById(R.id.edit_oldpassword);
        mEditText_new = (EditText) mView.findViewById(R.id.edit_newpassword);
        return view;
    }

    private boolean isConnected() {
        ConnectionStatus connectionStatus = BmobIM.getInstance().getCurrentStatus();
        if (connectionStatus.getCode() != ConnectionStatus.CONNECTED.getCode()) {
            return false;
        }
        return true;
    }

    private void clearChat() {
        ClearChatTask task = new ClearChatTask(new WeakReference<>(this));
        task.execute();
    }

    private void updatePassword(String oldpassword, String newpassword) {
        BmobUser.updateCurrentUserPassword(oldpassword, newpassword, new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if(e == null) {
                    Toast.makeText(SettingFragment.this.getActivity(), "修改成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingFragment.this.getActivity(), "修改失败", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void initDialog() {

        mAlertDialog = new AlertDialog.Builder(getContext())
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(mEditText_old.getText().toString())
                                || TextUtils.isEmpty(mEditText_new.getText().toString())) {
                            Toast.makeText(SettingFragment.this.getActivity(), "密码不能为空!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        updatePassword(mEditText_old.getText().toString(), mEditText_new.getText().toString());
                    }
                })
                .setView(mView)
                .setTitle("修改密码")
                .create();
    }

    @OnClick({R.id.btn_logout, R.id.btn_connect, R.id.btn_password, R.id.btn_clearChat, R.id.btn_clearCache})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn_clearCache: {
                break;
            }
            case R.id.btn_connect: {
                if (isConnected()) {
                    BmobIM.getInstance().disConnect();
                    mTextView_connect.setText("当前状态:离线");
                } else {
                    BmobIMClient.connect(BmobUser.getCurrentUser().getObjectId(), new ConnectListener() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                mTextView_connect.setText("当前状态:在线");
                            }
                        }
                    });
                }
                break;
            }
            case R.id.btn_password: {
                if (mAlertDialog == null) {
                    initDialog();
                }
                mAlertDialog.show();
                break;
            }
            case R.id.btn_clearChat: {
                clearChat();
                break;
            }
            case R.id.btn_logout: {
                BmobUser.logOut();
                startActivity(new Intent(getActivity(), SplashActivity.class));
                getActivity().finish();
                break;
            }
            default:
                break;
        }
    }

    private static class ClearChatTask extends AsyncTask<Void, Integer, Boolean> {
        WeakReference<SettingFragment> mReference;

        ClearChatTask(WeakReference<SettingFragment> reference) {
            mReference = reference;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            List<BmobIMConversation> list = BmobIM.getInstance().loadAllConversation();
            for (BmobIMConversation conversation : list) {
                conversation.resetMessages();
                BmobIM.getInstance().updateConversation(conversation);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(mReference.get().getActivity(), "已清除聊天记录", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mReference.get().getActivity(), "清除聊天记录失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
