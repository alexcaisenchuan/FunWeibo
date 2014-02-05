package com.alex.common.keep;

import com.alex.common.utils.PrefUtils;
import com.weibo.sdk.android.Oauth2AccessToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 该类用于保存微博授权信息到sharepreference，并提供读取功能
 * @author caisenchuan
 */
public class WeiboAuthInfoKeeper {
    /*--------------------------
     * 常量
     *-------------------------*/
    /**pref的名字*/
    private static final String PREFERENCES_NAME = "pref_weibo_auth_info";
	
    /**access_token*/
    private static final String TAG_TOKEN = "token";
    /**过期时间*/
    private static final String TAG_EXPIRES_TIME = "expiresTime";
    /**用户id*/
    private static final String TAG_UID = "uid";
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
	 * 保存accesstoken到SharedPreferences
	 * @param context Activity 上下文环境
	 * @param token Oauth2AccessToken
	 */
	public static void keepAccessToken(Context context, Oauth2AccessToken token) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putString(TAG_TOKEN, token.getToken());
		editor.putLong(TAG_EXPIRES_TIME, token.getExpiresTime());
		editor.commit();
	}

	/**
	 * 从SharedPreferences读取accessstoken
	 * @param context
	 * @return Oauth2AccessToken
	 */
	public static Oauth2AccessToken readAccessToken(Context context){
		Oauth2AccessToken token = new Oauth2AccessToken();
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		token.setToken(pref.getString(TAG_TOKEN, ""));
		token.setExpiresTime(pref.getLong(TAG_EXPIRES_TIME, 0));
		return token;
	}
	
	/**
     * 保存用户uid
     * @param context
     * @param uid
     */
    public static void keepUid(Context context, String weibo_userid) {
        PrefUtils.keep(context, PREFERENCES_NAME, TAG_UID, weibo_userid);
    }

    /**
     * 读取用户uid
     * @param context
     * @return uid
     */
    public static String readUid(Context context){
        return PrefUtils.readString(context, PREFERENCES_NAME, TAG_UID);
    }
}
