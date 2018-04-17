package com.jkingone.jchat.utils;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/8/16.
 */

public class TimeUtils {
    /**
     * 格式化时间
     */
    public static String formatTime(Long time){
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if(min.length() < 2){
            min = "0" + time / (1000 * 60) + "";
        }else {
            min = time / (1000 * 60) + "";
        }
        if(sec.length() == 4){
            sec = "0" + (time % (1000 * 60)) + "";
        }else if (sec.length() == 3){
            sec = "00" + (time %(1000 * 60)) + "";
        }else if (sec.length() == 2){
            sec = "000" + (time % (1000 * 60)) + "";
        }else if (sec.length() == 1){
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }
	
	public static String formatDateString(Context context, long time) {
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat
                .getTimeFormat(context);
        Date date = new Date(time);
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }
}
