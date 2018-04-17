package com.jkingone.jchat.ui.activity;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.jkingone.jchat.Constant;
import com.jkingone.jchat.R;
import com.jkingone.jchat.utils.ScreenUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.b.V;

import static com.jkingone.jchat.Constant.LOGIN_OK;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.tv_appname)
    TextView mTextView;

    boolean isFirst = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ScreenUtils.setTranslucent(this);
        ButterKnife.bind(this);
        hasPermission();
        mTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        YoYo.with(Techniques.DropOut).onRepeat(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                mTextView.setVisibility(View.VISIBLE);
            }
        }).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                if (!isFirst) {
                    isFirst = true;
                    if (BmobUser.getCurrentUser() != null) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }
        }).duration(1500).delay(500).playOn(mTextView);
    }

    private void hasPermission() {

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (PackageManager.PERMISSION_GRANTED
                != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && permissions[1].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                finish();
            }
        }
    }
}
