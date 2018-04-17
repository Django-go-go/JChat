package com.jkingone.jchat.ui.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jkingone.jchat.R;
import com.jkingone.jchat.customview.PagerSlidingTabStrip;
import com.jkingone.jchat.ui.fragment.ConversationFragment;
import com.jkingone.jchat.ui.fragment.FriendFragment;
import com.jkingone.jchat.ui.fragment.SettingFragment;
import com.jkingone.jchat.utils.ScreenUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.newim.listener.ConnectStatusChangeListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.pagersliding)
    PagerSlidingTabStrip mTabStrip;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private ViewPagerAdapter mViewPagerAdapter;
    private Fragment[] fragment = {new ConversationFragment(), new FriendFragment(), new SettingFragment()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScreenUtils.setTranslucent(this);
        ButterKnife.bind(this);
        initToolbar();

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabStrip.setViewPager(mViewPager);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.addfriend:
                        startActivity(new Intent(MainActivity.this, AddFriendActivity.class));
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        int height = ScreenUtils.getStatusHeight(this) + ScreenUtils.getActionBarSize(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        mToolbar.setLayoutParams(lp);
        mToolbar.requestLayout();
    }


    @Override
    protected void onStart() {
        super.onStart();
        ConnectionStatus connectionStatus = BmobIM.getInstance().getCurrentStatus();
        if (connectionStatus.getCode() != ConnectionStatus.CONNECTED.getCode()) {
            connect();
        }
        BmobIM.getInstance().setOnConnectStatusChangeListener(new ConnectStatusChangeListener() {
            @Override
            public void onChange(ConnectionStatus status) {
                Log.i(TAG, BmobIM.getInstance().getCurrentStatus().getMsg());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void connect() {
        BmobUser user = BmobUser.getCurrentUser();
        if (!TextUtils.isEmpty(user.getObjectId())) {
            BmobIM.connect(user.getObjectId(), new ConnectListener() {
                @Override
                public void done(String uid, BmobException e) {
                    if (e == null) {
                        Log.i(TAG, "connect ok");
                    } else {
                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragment.length;
        }

        @Override
        public Fragment getItem(int position) {
            return fragment[position];
        }
    }
}
