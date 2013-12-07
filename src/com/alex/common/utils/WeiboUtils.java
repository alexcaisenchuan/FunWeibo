package com.alex.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;

import com.alex.common.BaseActivity;
import com.alex.common.utils.NetworkUtils.NetworkType;
import com.alex.funweibo.AppControl;
import com.alex.funweibo.R;
import com.ta.util.cache.TAFileCache;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.PlaceAPI;
import com.weibo.sdk.android.model.Status;
import com.weibo.sdk.android.model.Status.ExtraParams;

/**
 * 微博相关工具类
 * @author caisenchuan
 */
public class WeiboUtils {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = WeiboUtils.class.getSimpleName();
    ////////////////Broadcast参数///////////////////
    /**发布新微博*/
    public static final String BROADCAST_ACTION_NEW_WEIBO_SEND     = "com.alex.funweibo.action.new_weibo_send";
    /**发布新微博成功*/
    public static final String BROADCAST_ACTION_NEW_WEIBO_SUCCESS  = "com.alex.funweibo.action.new_weibo_success";
    /**发布新微博失败*/
    public static final String BROADCAST_ACTION_NEW_WEIBO_FAILD    = "com.alex.funweibo.action.new_weibo_faild";
    
    /**序列化方式传递一条微博的信息*/
    public static final String INTENT_EXTRA_WEIBO_STATUS_OBJ = "weibo_status_obj";
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 发送微博的回调函数
     * @author caisenchuan
     */
    private static class AddWeiboListener extends OnHttpRequestReturnListener {

        /**发送时使用的临时微博*/
        private Status mTempStatus;
        
        public AddWeiboListener(BaseActivity base, Status tempStatus) {
            super(base);
            this.mTempStatus = tempStatus;
            KLog.d(TAG, "tempId : %s", tempStatus.getExtraParams().getTempId());
        }

        @Override
        public void onComplete(String arg0) {
            try {
                //KLog.d(TAG, arg0);
                
                //读取微博
                Status status = new Status(arg0);
                ExtraParams p = status.getExtraParams();
                p.setTempId(mTempStatus.getExtraParams().getTempId());       //设置对应的临时微博id
                
                if(status != null) {
                    mBaseActivity.showToastOnUIThread(mBaseActivity.getString(R.string.hint_checkin_success));
                    
                    //发出广播
                    sendBroadCast(mBaseActivity, BROADCAST_ACTION_NEW_WEIBO_SUCCESS, status);
                    
                    //删除照片
                    ImageUtils.deletePhoto(mTempStatus.getBmiddle_pic());
                }
            } catch (Exception e) {
                KLog.w(TAG, "Exception while build status", e);
                mBaseActivity.showToastOnUIThread(mBaseActivity.getString(R.string.hint_add_weibo_faild) + e.toString());
                sendBroadCast(mBaseActivity, BROADCAST_ACTION_NEW_WEIBO_FAILD, mTempStatus);      //发出广播
            }
        }
        
        @Override
        public void onComplete4binary(ByteArrayOutputStream arg0) {
            try {
                super.onComplete4binary(arg0);
            } finally {
                sendBroadCast(mBaseActivity, BROADCAST_ACTION_NEW_WEIBO_FAILD, mTempStatus);      //发出广播
            }
        }
        
        @Override
        public void onError(WeiboException e) {
            try {
                super.onError(e);
            } finally {
                sendBroadCast(mBaseActivity, BROADCAST_ACTION_NEW_WEIBO_FAILD, mTempStatus);      //发出广播
            }
        }
        
        @Override
        public void onIOException(IOException e) {
            try {
                super.onIOException(e);
            } finally {
                sendBroadCast(mBaseActivity, BROADCAST_ACTION_NEW_WEIBO_FAILD, mTempStatus);      //发出广播
            }
        }
    }
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
    
    /**
     * 发布新微博
     * @param activity
     * @param token
     * @param tempStatus
     */
    public static void postNewWeibo(BaseActivity activity, Status tempStatus) {
        
        AppControl app = (AppControl)activity.getApplication();
        Oauth2AccessToken token = app.getAccessToken();
        
        PlaceAPI place = new PlaceAPI(token);
        String poiid = tempStatus.getPlace().poiid;
        String content = tempStatus.getText();
        String picPath = tempStatus.getBmiddle_pic();
        
        place.poisAddCheckin(poiid,
                             content,
                             picPath,
                             true,
                             new AddWeiboListener(activity, tempStatus));
    }
    
    /**
    * 发出广播
    * @param context
    * @param action
    * @param status
    */
    public static void sendBroadCast(Context context, String action, Status status) {
        KLog.d(TAG, "sendBroadCast , %s", action);
        Intent it = new Intent();
        it.setAction(action);
        it.putExtra(INTENT_EXTRA_WEIBO_STATUS_OBJ, status);
        context.sendBroadcast(it);
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
