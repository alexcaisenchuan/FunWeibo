package com.alex.common.keep;

import com.alex.common.utils.PrefUtils;

import android.content.Context;

/**
 * 各个设置项的保存类
 * @author caisenchuan
 */
public class SettingKeeper {
    /*--------------------------
     * 常量
     *-------------------------*/
    /**pref的名字*/
    private static final String PREFERENCES_NAME    = "pref_settings";
    
    /**选择的分类*/
    private static final String TAG_CATEGORY_SELECT = "category_select";
    /**移动网络下使用低清图片质量*/
    private static final String TAG_PIC_LOW_QUALITY_UNDER_MOBILE  = "pic_quality_under_mobile";
    
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
     * @param context
     * @param setting
     */
    public static void keepCategorySelect(Context context, String setting) {
        PrefUtils.keep(context, PREFERENCES_NAME, TAG_CATEGORY_SELECT, setting);
    }

    /**
     * 读取分类选择的设置
     * @param context
     * @return 设置值
     */
    public static String readCategorySelect(Context context) {
        return PrefUtils.readString(context, PREFERENCES_NAME, TAG_CATEGORY_SELECT);
    }
    
    /**
     * 保存分类选择的设置
     * @param context
     * @param setting
     */
    public static void keepPicLowQualityUnderMobile(Context context, boolean setting) {
        PrefUtils.keep(context, PREFERENCES_NAME, TAG_PIC_LOW_QUALITY_UNDER_MOBILE, setting);
    }

    /**
     * 读取分类选择的设置
     * @param context
     * @return 设置值
     */
    public static boolean readPicLowQualityUnderMobile(Context context) {
        return PrefUtils.readBoolean(context, PREFERENCES_NAME, TAG_PIC_LOW_QUALITY_UNDER_MOBILE);
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
