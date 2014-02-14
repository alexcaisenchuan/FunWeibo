package com.alex.common;

/**
 * 一些配置信息
 * @author caisenchuan
 */
public class AppConfig {
    /*--------------------------
     * 常量
     *-------------------------*/
    /**全局调试开关*/
    public static final boolean DEBUG = false;
    
    ///////////////////////////////百度相关///////////////////////////////////
    /**百度 API KEY*/
    public static final String BAIDU_API_KEY = "nAdzTUW1sKlw6EW6naalhQD2";
    
    ///////////////////////////////微博相关///////////////////////////////////
    /**应用的key 请到官方申请正式的appkey替换APP_KEY*/
    public static final String WEIBO_APP_KEY="3708502202";
    /**替换为开发者REDIRECT_URL*/
    public static final String WEIBO_REDIRECT_URL = "http://caisenchuan.com/funweibo/callback.php";
    /**新支持scope 支持传入多个scope权限，用逗号分隔*/
    public static final String WEIBO_SCOPE = "email,direct_messages_read," +
    		                                 "direct_messages_write,friendships_groups_read," +
    		                                 "friendships_groups_write,statuses_to_me_read," +
    		                                 "follow_app_official_microblog";

    ////////////////////////////////路径配置////////////////////////////////////
    /**图片缓存文件报名*/
    public static final String SYSTEMCACHE = "thinkandroid";
    
    /**应用的基础文件在SD卡上的路径*/
    public static final String DIR_APP = "/funweibo/";
    /**图片存储路径*/
    public static final String DIR_PHOTO = DIR_APP + "photo/";
    /**下载文件存储路径*/
    public static final String DIR_DOWNLOAD = DIR_APP + "download/";
    
    //////////////////////////各种最大最小值////////////////////////
    /**存储的最大微博条数*/
    public static final int MAX_STORE_STATUS_NUM = 30;
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/

    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
