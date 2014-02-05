package com.alex.common.keep;

import com.alex.common.utils.PrefUtils;

import android.content.Context;

/**
 * 微博信息保存
 * @author caisenchuan
 */
public class WeiboDataKeeper {
    /*--------------------------
     * 常量
     *-------------------------*/
    /**pref的名字*/
    private static final String PREFERENCES_NAME = "pref_weibo_data";
    
    /**最近一次的微博列表结果*/
    private static final String TAG_STATUS_ARRAY = "status_array";
    /**当前用户信息s*/
    private static final String TAG_CURR_USER = "curr_user";
    
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
     * 清空数据
     */
    public static void clear(Context context) {
        PrefUtils.clear(context, PREFERENCES_NAME);
    }
    
    /**
     * 保存微博列表
     * @param context
     * @param str
     */
    public static void keepStatus(Context context, String str) {
        PrefUtils.keep(context, PREFERENCES_NAME, TAG_STATUS_ARRAY, str);
    }

    /**
     * 读取微博列表字符串
     * @param context
     */
    public static String readStatus(Context context) {
        return PrefUtils.readString(context, PREFERENCES_NAME, TAG_STATUS_ARRAY);
    }
    
    /**
     * 保存当前用户信息
     * @param context
     * @param str
     */
    public static void keepCurrUser(Context context, String str) {
        PrefUtils.keep(context, PREFERENCES_NAME, TAG_CURR_USER, str);
    }

    /**
     * 读取当前用户信息
     * @param context
     */
    public static String readCurrUser(Context context) {
        return PrefUtils.readString(context, PREFERENCES_NAME, TAG_CURR_USER);
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/
    
}

