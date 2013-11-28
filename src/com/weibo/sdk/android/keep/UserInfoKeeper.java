package com.weibo.sdk.android.keep;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 该类用于保存用户信息到sharepreference，并提供读取功能
 * @author caisenchuan
 */
public class UserInfoKeeper {
    private static final String PREFERENCES_NAME = "com_weibo_sdk_android_user_info";
    
    /**
     * 保存用户信息到SharedPreferences
     * @param context Activity 上下文环境
     * @param uid 微博用户id
     */
    public static void keep(Context context, String weibo_userid) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString("weibo_userid", weibo_userid);
        editor.commit();
    }
    
    /**
     * 清空sharepreference
     * @param context
     */
    public static void clear(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 从SharedPreferences读取用户信息
     * @param context
     * @return uid
     */
    public static String read(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        String weibo_userid = pref.getString("weibo_userid", "");
        return weibo_userid;
    }
}
