package com.jkingone.jchat.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jkingone.jchat.R;
import com.jkingone.jchat.utils.DensityUtils;

public class PagerSlidingTabStrip extends ViewGroup {
    private static final String TAG = "PagerSlidingTabStrip";

    private Context mContext;

    private Integer[] icons = {
            R.drawable.tab_chat,
            R.drawable.tab_contacts,
            R.drawable.tab_me
    };

    private Integer[] icons_hover = {
            R.drawable.tab_chat_hover,
            R.drawable.tab_contacts_hover,
            R.drawable.tab_me_hover
    };

    private String[] words = {
            "会话", "通讯录", "我"
    };

    private final PageListener pageListener = new PageListener();
    public OnPageChangeListener delegatePageListener;

    private ViewPager pager;

    private int tabCount = 0;

    private int currentPosition = 0;
    private int lastPosition = 0;
    private float currentPositionOffset = 0f;

    private int tabPadding;

    private int height;
    private int width = 0;

    private Paint dividerPaint;

    public PagerSlidingTabStrip(Context context) {
        this(context, null);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setColor(Color.GRAY);

        setWillNotDraw(false);

        height = DensityUtils.dp2px(context, 64);
        tabPadding = DensityUtils.dp2px(context, 8);
        tabCount = icons.length;
    }

    public void setViewPager(ViewPager pager) {
        this.pager = pager;

        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        if (pager.getAdapter().getCount() != tabCount) {
            throw new IllegalStateException("count is not equal.");
        }

        pager.addOnPageChangeListener(pageListener);

        pager.setCurrentItem(currentPosition);

        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    public void notifyDataSetChanged() {
        removeAllViews();

        for (int i = 0; i < tabCount; i++) {

            TextView textView = new TextView(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.weight = 0;
            textView.setLayoutParams(lp);
            textView.setText(words[i]);
            textView.setTextSize(12);
            textView.setTextColor(Color.GRAY);
            textView.setGravity(Gravity.CENTER);

            ImageView imageView = new ImageView(mContext);
            LinearLayout.LayoutParams imglp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 0);
            imglp.weight = 1;
            imageView.setLayoutParams(imglp);
            imageView.setBackgroundResource(icons[i]);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);


            LinearLayout tab = new LinearLayout(mContext);
            tab.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            tab.setOrientation(LinearLayout.VERTICAL);
            tab.addView(imageView);
            tab.addView(textView);
            tab.setPadding(tabPadding, tabPadding, tabPadding, tabPadding);
            tab.setGravity(Gravity.CENTER);

            final int pos = i;
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    pager.setCurrentItem(pos, false);
                }
            });

            addView(tab);
        }
        invalidate();
    }

    public void addTab(Integer[] icon, String[] word) {
        icons = new Integer[icon.length];
        words = new String[word.length];
        System.arraycopy(icon, 0, icons, 0, icon.length);
        System.arraycopy(word, 0, words, 0, word.length);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            height = h;
        }
        setMeasuredDimension(getMeasuredWidth(), height);
        int spec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            //这个很重要，没有就不显示
            getChildAt(i).measure(widthMeasureSpec, spec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        width = getMeasuredWidth() / tabCount;
        Log.i(TAG, "onLayout: width " + width);
        notifyLayout();
    }

    private void notifyLayout() {
        if (getChildCount() == tabCount && tabCount != 0) {
            for (int i = 0; i < tabCount; i++) {
                View child = getChildAt(i);
                child.layout(width * i, getPaddingTop(), width * (i + 1), getPaddingTop() + height);
            }
            Log.i(TAG, "notifyLayout: ");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.i(TAG, "onDraw: ");

        canvas.drawLine(getPaddingStart(), getPaddingTop(), getMeasuredWidth(), getPaddingTop(), dividerPaint);

        if (tabCount == 0) {
            return;
        }
        

        if (currentPosition <= (getChildCount() - 1)) {

            for (int i = 0; i < tabCount; i++) {
                ViewGroup tab = (ViewGroup) this.getChildAt(i);
                ImageView imageView = (ImageView) tab.getChildAt(0);
                TextView textView = (TextView) tab.getChildAt(1);

                if (i != currentPosition) {
                    imageView.setBackgroundResource(icons[i]);
                    textView.setTextColor(Color.GRAY);
                } else {
                    imageView.setBackgroundResource(icons_hover[i]);
                    textView.setTextColor(Color.parseColor("#FF4081"));
                }

            }

        }
    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (positionOffset >= 0) {
                currentPositionOffset = positionOffset;
            }

            currentPosition = position;

            invalidate();

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {

            currentPosition = position;
            invalidate();

            if (delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }
        }

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    private static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
