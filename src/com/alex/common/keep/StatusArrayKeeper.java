package com.alex.common.keep;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StatusArrayKeeper {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String PREFERENCES_NAME = "com_alex_funweibo_status_array";
    
    private static final String TAG_STATUS_ARRAY = "status_array";
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 保存微博列表到SharedPreferences
     * @param context Activity 上下文环境
     * @param status_array 微博列表
     */
    public static void keep(Context context, String status_array) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString(TAG_STATUS_ARRAY, status_array);
        editor.commit();
    }
    
    /**
     * 清空sharepreference
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 从SharedPreferences读取微博列表字符串
     * @param context
     * @return uid
     */
    public static String read(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        String status_array = pref.getString(TAG_STATUS_ARRAY, "");
        return status_array;
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/
    
}

