/**
 * <p>Title: WLog.java</p>
 * <p>Description: 打印调试封装类</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.common.utils;

import android.util.Log;

/**
 * 打印调试日志
 * @author caisenchuan
 */
public class KLog {
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    
    /*--------------------------
     * 常量
     *-------------------------*/

    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/
    ////////////////普通字符串//////////////////
    public static void v(String tag, String str) {
        Log.v(tag, str);
    }
    
    public static void d(String tag, String str) {
        Log.d(tag, str);
    }
    
    public static void w(String tag, String str) {
        Log.w(tag, str);
    }
    
    public static void e(String tag, String str) {
        Log.e(tag, str);
    }
    
    ////////////////支持格式化参数//////////////////
    public static void v(String tag, String format, Object... args) {
        Log.v(tag, String.format(format, args));
    }
    
    public static void d(String tag, String format, Object... args) {
        Log.d(tag, String.format(format, args));
    }
    
    public static void w(String tag, String format, Object... args) {
        Log.w(tag, String.format(format, args));
    }
    
    public static void e(String tag, String format, Object... args) {
        Log.e(tag, String.format(format, args));
    }
    
    ////////////////支持堆栈输出//////////////////
    public static void d(String tag, String str, Throwable tr) {
        Log.d(tag, str, tr);
    }
    
    public static void w(String tag, String str, Throwable tr) {
        Log.w(tag, str, tr);
    }
    
    public static void e(String tag, String str, Throwable tr) {
        Log.e(tag, str, tr);
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
