package com.jkingone.jchat.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jkingone.jchat.Constant;
import com.jkingone.jchat.R;
import com.jkingone.jchat.utils.ScreenUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.jkingone.jchat.Constant.LOGIN_OK;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.edit_username)
    EditText mEditText_username;
    @BindView(R.id.edit_password)
    EditText mEditText_password;
    @BindView(R.id.btn_register)
    Button mButton_register;
    @BindView(R.id.btn_login)
    Button mButton_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ScreenUtils.setTranslucent(this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_register)
    public void register() {
        final BmobUser bmobUser = new BmobUser();
        String username = mEditText_username.getText().toString();
        if (username.length() != 0) {
            bmobUser.setUsername(username);
        } else {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = mEditText_password.getText().toString();
        if (password.length() != 0) {
            bmobUser.setPassword(password);
        } else {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        bmobUser.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    Log.i(Constant.TAG, "sign up ok");
                    bmobUser.login(new SaveListener<BmobUser>() {
                        @Override
                        public void done(BmobUser bmobUser, BmobException e) {
                            if (e == null) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Log.e(Constant.TAG, "login ", e);
                            }
                        }
                    });
                } else {
                    Log.e(Constant.TAG, "signUp", e);
                }
            }
        });

    }

    @OnClick(R.id.btn_login)
    public void login() {
        final BmobUser bmobUser = new BmobUser();
        String username = mEditText_username.getText().toString();
        if (username.length() != 0) {
            bmobUser.setUsername(username);
        } else {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = mEditText_password.getText().toString();
        if (password.length() != 0) {
            bmobUser.setPassword(password);
        } else {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        bmobUser.login(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Log.e(Constant.TAG, "login ", e);
                }
            }
        });

    }
}
