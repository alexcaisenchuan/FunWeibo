package com.alex.common.keep;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 各个设置项的保存类
 * @author caisenchuan
 */
public class SettingKeeper {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String PREFERENCES_NAME = "com_alex_funweibo_setting";
    
    private static final String TAG_CATEGORY_SELECT = "category_select";
    
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
     * 保存分类选择的设置
     * @param context Activity 上下文环境
     * @param setting 微博列表
     */
    public static void keepCategorySelect(Context context, String setting) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString(TAG_CATEGORY_SELECT, setting);
        editor.commit();
    }

    /**
     * 从SharedPreferences读取微博列表字符串
     * @param context
     * @return 设置值
     */
    public static String readCategorySelect(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        String setting = pref.getString(TAG_CATEGORY_SELECT, "");
        return setting;
    }
    
    /**
     * 清空所有设置的SharePref
     * @param context
     */
    public static void clearAll(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
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
