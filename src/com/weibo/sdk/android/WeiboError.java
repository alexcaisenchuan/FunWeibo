/**
 * <p>Title: WeiboErrorDefines.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-22
 * @version 1.0
 */
package com.weibo.sdk.android;

import com.weibo.sdk.android.org.json.JSONException;
import com.weibo.sdk.android.org.json.JSONObject;

/**
 * 微博错误码的定义
 * @author caisenchuan
 */
public class WeiboError {
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "WeiboError";
    
    /////////////////JSON Tags///////////////////
    /**调用地址*/
    private static final String TAG_REQUEST = "request";
    /**错误信息*/
    private static final String TAG_ERROR = "error";
    /**错误码*/
    private static final String TAG_ERROR_CODE = "error_code";
    
    /////////////////错误码定义//////////////////
    /**指定的微博不存在*/
    public static final int ERROR_WEIBO_NOT_EXIST           = 20101;
    /**用户请求频次超出限制*/
    public static final int ERROR_REQUEST_FREQ_BEYOND_LIMIT = 10023;
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**调用地址*/
    public String request = "";
    /**错误信息*/
    public String error = "";
    /**错误码*/
    public int error_code = 0;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 通过JSON字符串创建WeiboError对象
     * @throws JSONException JSON构建错误
     */
    public WeiboError(String str) throws JSONException {
        this(new JSONObject(str));
    }
    
    /**
     * 通过JSON对象创建WeiboError对象
     * @param json
     */
    public WeiboError(JSONObject json) {
        if(json != null) {
            this.request = json.optString(TAG_REQUEST, "");
            this.error = json.optString(TAG_ERROR, "");
            this.error_code = json.optInt(TAG_ERROR_CODE, 0);
        } else {
            throw new NullPointerException();
        }
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
