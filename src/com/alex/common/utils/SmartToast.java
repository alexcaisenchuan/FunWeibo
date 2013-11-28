/**
 * <p>Title: SmartToast.java</p>
 * <p>Description: 简易Toast封装类</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.common.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author caisenchuan
 *
 */
public class SmartToast {
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/

    /*--------------------------
     * 成员变量
     *-------------------------*/
    private static Toast mSingletonToast = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    public static void initSingletonToast(Context context) {
        mSingletonToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }
    
    /**
     * 显示一个短Toast
     * @param context
     * @param str 要显示的字符串
     * @param singleton true为单例Toast，否则为普通Toast
     */
    public static void showShortToast(Context context, String str, boolean singleton) {
        show(context, str, false, singleton);
    }
    
    /**
     * 显示一个短Toast
     * @param context
     * @param id 字符串资源id
     * @param singleton true为单例Toast，否则为普通Toast
     */
    public static void showShortToast(Context context, int id, boolean singleton) {
        show(context, context.getString(id), false, singleton);
    }
    
    /**
     * 显示一个长Toast
     * @param context
     * @param str 要显示的字符串
     * @param singleton true为单例Toast，否则为普通Toast
     */
    public static void showLongToast(Context context, String str, boolean singleton) {
        show(context, str, true, singleton);
    }
    
    /**
     * 显示一个短Toast
     * @param context
     * @param id 字符串资源id
     * @param singleton true为单例Toast，否则为普通Toast
     */
    public static void showLongToast(Context context, int id, boolean singleton) {
        show(context, context.getString(id), true, singleton);
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 根据不同设置显示不同类型的Toast
     * @param context 上下文
     * @param text 要显示的文字
     * @param lengthLong true为长时间Toast，否则为短时间Toast
     * @param singleton true为单例Toast，否则为普通Toast
     */
    private static void show(Context context, String text, boolean lengthLong, boolean singleton) {
        int duration = Toast.LENGTH_SHORT;
        if(lengthLong) {
            duration = Toast.LENGTH_LONG;
        }
        
        if(singleton && mSingletonToast != null) {
            mSingletonToast.setText(text);
            mSingletonToast.setDuration(duration);
            mSingletonToast.show();
        } else {
            Toast.makeText(context, text, duration).show();
        }
    }
}
