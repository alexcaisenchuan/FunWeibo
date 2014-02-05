package com.alex.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SharedPreferences相关工具类
 * @author caisenchuan
 */
public class PrefUtils {
    /*--------------------------
     * 常量
     *-------------------------*/

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
     * 保存字符串到pref里
     * @param context 上下文
     * @param pref_name share pref名字
     * @param tag 字段名
     * @param value 字段值
     */
    public static void keep(Context context, String pref_name, String tag, String value) {
        SharedPreferences pref = context.getSharedPreferences(pref_name, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString(tag, value);
        editor.commit();
    }
    
    /**
     * 保存boolean到pref里
     * @param context 上下文
     * @param pref_name share pref名字
     * @param tag 字段名
     * @param value 字段值
     */
    public static void keep(Context context, String pref_name, String tag, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(pref_name, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putBoolean(tag, value);
        editor.commit();
    }

    /**
     * 从pref里读取字符串
     * @param context 上下文
     * @param pref_name share pref名字
     * @param tag 字段名
     */
    public static String readString(Context context, String pref_name, String tag) {
        SharedPreferences pref = context.getSharedPreferences(pref_name, Context.MODE_APPEND);
        String ret = pref.getString(tag, "");
        return ret;
    }
    
    /**
     * 从pref里读取boolean
     * @param context 上下文
     * @param pref_name share pref名字
     * @param tag 字段名
     */
    public static boolean readBoolean(Context context, String pref_name, String tag) {
        SharedPreferences pref = context.getSharedPreferences(pref_name, Context.MODE_APPEND);
        boolean ret = pref.getBoolean(tag, false);
        return ret;
    }

    /**
     * 清空所有字段
     * @param context
     * @param pref_name share pref名字
     */
    public static void clear(Context context, String pref_name) {
        SharedPreferences pref = context.getSharedPreferences(pref_name, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
