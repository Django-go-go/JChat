package com.jkingone.jchat.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.jkingone.jchat.R;

/**
 * Created by Jkingone on 2018/4/7.
 */

public class SideBar extends View {

    public SideBar(Context context) {
        super(context);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 触摸字母索引发生变化的回调接口
     */
    private onLetterChangeListener onLetterChangeListener = null;

    private String[] alphabet = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"
    };

    private int currentIndex = -1;

    private Paint paint = new Paint();

    private TextView textViewDialog = null;

    /**
     * 为SideBar设置显示字母的TextView
     */
    public void setTextViewDialog(TextView textViewDialog) {
        this.textViewDialog = textViewDialog;
    }

    /**
     * 绘制列表控件的方法
     * 将要绘制的字母以从上到下的顺序绘制在一个指定区域
     * 如果是进行选中的字母就进行高亮显示
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取SideBar的高度
        int viewHeight = getMeasuredHeight();
        //获取SideBar的宽度
        int viewWidth = getMeasuredWidth();
        //获得每个字母索引的高度
        int singleHeight = viewHeight / alphabet.length;

        //绘制每一个字母的索引
        for (int i = 0; i < alphabet.length; i++) {
            paint.setColor(Color.GRAY);
            paint.setTextSize(25);
            paint.setAntiAlias(true);

            //如果当前的手指触摸索引和字母索引相同，那么字体颜色进行区分
            if (currentIndex == i) {
                paint.setColor(Color.parseColor("#FF4081"));
                paint.setFakeBoldText(true);
            }

            /**
             * 绘制字体，需要制定绘制的x、y轴坐标
             *
             * x轴坐标 = 控件宽度的一半 - 字体宽度的一半
             * y轴坐标 = singleHeight * i + singleHeight
             */

            float x = viewWidth / 2 - paint.measureText(alphabet[i]) / 2;
            float y = singleHeight * i + singleHeight;
            canvas.drawText(alphabet[i], x, y, paint);

            paint.reset();
        }
    }

    public void setOnLetterChangeListener(
            onLetterChangeListener onLetterChangeListener) {

        this.onLetterChangeListener = onLetterChangeListener;
    }

    private onLetterChangeListener getOnLetterChangeListener() {
        return onLetterChangeListener;
    }

    /**
     * 当手指触摸的字母索引发生变化时，调用该回调接口
     */
    public interface onLetterChangeListener {
        void onLetterChange(String letterTouched);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 触摸事件的代码
        final int action = event.getAction();
        //手指触摸点在屏幕的Y坐标
        final float touchYPos = event.getY();
        int preIndex = currentIndex;
        final onLetterChangeListener listener = getOnLetterChangeListener();

        // 比例 = 手指触摸点在屏幕的y轴坐标 / SideBar的高度
        // 触摸点的索引 = 比例 * 字母索引数组的长度
        final int currentTouchIndex = (int) (touchYPos / getHeight() * alphabet.length);

        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                currentIndex = -1;
                if (textViewDialog != null) {
                    textViewDialog.setVisibility(View.INVISIBLE);
                }
                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // 不是同一个字母索引
                if (currentTouchIndex != preIndex) {
                    // 如果触摸点没有超出控件范围
                    if (currentTouchIndex >= 0 && currentTouchIndex < alphabet.length) {
                        if (listener != null) {
                            listener.onLetterChange(alphabet[currentTouchIndex]);
                        }

                        if (textViewDialog != null) {
                            textViewDialog.setText(alphabet[currentTouchIndex]);
                            textViewDialog.setVisibility(View.VISIBLE);
                        }

                        currentIndex = currentTouchIndex;
                        invalidate();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }
}
