package com.jkingone.jchat.imagePicker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jkingone.jchat.R;
import com.jkingone.jchat.utils.ScreenUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";

    private ViewPager mViewPager;
    private List<String> imagePaths;
    private String photoPath;
    private String prevPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Intent intent = getIntent();
        String path = intent.getStringExtra("photo");
        if (path != null) {
            photoPath = path;
            Log.i(TAG, "onCreate: " + photoPath);
        }

        int pos = intent.getIntExtra("image", -1);
        if (pos != -1) {
            imagePaths = ImagePicker.getInstance().getImagePaths();
            Log.i(TAG, "onCreate: " + pos);
        }

        prevPath = intent.getStringExtra("preview");

        Log.i(TAG, "onCreate: " + prevPath);

        initViewPager(pos);

        findViewById(R.id.btn_backpress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

    }

    private void initViewPager(int pos) {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ImageView imageView = new ImageView(PreviewActivity.this);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(PreviewActivity.this),
                        ScreenUtils.getScreenHeight(PreviewActivity.this)));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (photoPath != null) {
                    Picasso.with(PreviewActivity.this)
                            .load(new File(photoPath))
                            .centerCrop()
                            .resize(ScreenUtils.getScreenWidth(PreviewActivity.this), ScreenUtils.getScreenHeight(PreviewActivity.this))
                            .into(imageView);
                } else if (prevPath != null) {
                    Picasso.with(PreviewActivity.this)
                            .load(prevPath)
                            .centerCrop()
                            .resize(ScreenUtils.getScreenWidth(PreviewActivity.this), ScreenUtils.getScreenHeight(PreviewActivity.this))
                            .into(imageView);
                } else {
                    Picasso.with(PreviewActivity.this)
                            .load(new File(imagePaths.get(position)))
                            .centerCrop()
                            .resize(ScreenUtils.getScreenWidth(PreviewActivity.this), ScreenUtils.getScreenHeight(PreviewActivity.this))
                            .into(imageView);
                }

                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                if (photoPath != null || prevPath != null) {
                    return 1;
                } else {
                    return imagePaths.size();
                }
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });

        if (pos != -1) {
            mViewPager.setCurrentItem(pos);
        }
    }
}
