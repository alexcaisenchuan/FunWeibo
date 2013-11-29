package com.alex.common;

/**
 * 一些配置信息
 * @author caisenchuan
 */
public class AppConfig {
    /*--------------------------
     * 常量
     *-------------------------*/
    /**百度 API KEY*/
    public static final String BAIDU_API_KEY = "nAdzTUW1sKlw6EW6naalhQD2";
    
    //微博相关
    /**应用的key 请到官方申请正式的appkey替换APP_KEY*/
    public static final String WEIBO_APP_KEY="3708502202";
    /**替换为开发者REDIRECT_URL*/
    public static final String WEIBO_REDIRECT_URL = "http://caisenchuan.com/weibo/callback.php";
    /**新支持scope 支持传入多个scope权限，用逗号分隔*/
    public static final String WEIBO_SCOPE = "email,direct_messages_read," +
    		                                 "direct_messages_write,friendships_groups_read," +
    		                                 "friendships_groups_write,statuses_to_me_read," +
    		                                 "follow_app_official_microblog";

    /**图片缓存文件报名*/
    public static final String SYSTEMCACHE = "thinkandroid";
    
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
