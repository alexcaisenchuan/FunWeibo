package com.alex.common.utils;

import android.content.Context;

import com.alex.common.utils.NetworkUtils.NetworkType;
import com.ta.util.cache.TAFileCache;
import com.weibo.sdk.android.model.Status;

/**
 * 微博相关工具类
 * @author caisenchuan
 */
public class WeiboUtils {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = WeiboUtils.class.getSimpleName();
    
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
     * 根据当前网络状态获取对应的微博图片的url
     */
    public static String getStatusPicUrlByNetworkStatus(Context c, Status status, TAFileCache cache) {
        String url = "";
        
        if(c != null && status != null) {
            url = status.getBmiddle_pic();      //默认返回middle
            if(NetworkUtils.getCurrentNetworkType(c) == NetworkType.NETWORK_MOBILE) {
                byte[] buffer = null;
                if(cache != null) {
                    buffer = cache.getBufferFromMemCache(url);
                }
                if(buffer == null) {
                    url = status.getThumbnail_pic();
                    KLog.d(TAG, "get thumbnail pic");
                } else {
                    //若middle_pic已经在缓存中，则返回之
                    KLog.d(TAG, "middle pic in cache, use it!");
                }
            } else {
                //...
            }
        }
        
        return url;
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
