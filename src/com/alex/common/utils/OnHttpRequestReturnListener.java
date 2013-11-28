/**
 * <p>Title: OnHttpRequestReturnListener.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-19
 * @version 1.0
 */
package com.alex.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.alex.funweibo.R;
import com.alex.common.BaseActivity;
import com.weibo.sdk.android.WeiboError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.org.json.JSONException;

/**
 * HttpClient网络请求返回时的处理类，对出错情况做了简单报错处理
 * @author caisenchuan
 *
 */
public abstract class OnHttpRequestReturnListener implements RequestListener{
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "OnHttpRequestReturnListener";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    protected BaseActivity mBaseActivity = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * HttpClient网络请求返回时的处理类，对出错情况做了简单报错处理
     * @param base 用于显示Toast的Activity对象
     */
    public OnHttpRequestReturnListener(BaseActivity base) {
        this.mBaseActivity = base;
    }
    
    //onComplete()由其子类实现

    /* (non-Javadoc)
     * @see com.weibo.sdk.android.net.RequestListener#onComplete4binary(java.io.ByteArrayOutputStream)
     */
    @Override
    public void onComplete4binary(ByteArrayOutputStream arg0) {
        mBaseActivity.showToastOnUIThread(mBaseActivity.getString(R.string.hint_binary_stream) + arg0.toString());
    }

    /* (non-Javadoc)
     * @see com.weibo.sdk.android.net.RequestListener#onError(com.weibo.sdk.android.WeiboException)
     */
    @Override
    public void onError(WeiboException e) {
        KLog.w(TAG, "onError", e);
        String msg = e.getMessage();
        WeiboError error = null;
        try {
            error = new WeiboError(msg);
        } catch (JSONException e1) {
            KLog.w(TAG, "JSONException while build WeiboError", e1);
        }
        
        String str = "";
        if(error != null) {
            switch(error.error_code) {
                case WeiboError.ERROR_WEIBO_NOT_EXIST:
                    str = mBaseActivity.getString(R.string.weibo_error_not_exist);
                    break;
                    
                case WeiboError.ERROR_REQUEST_FREQ_BEYOND_LIMIT:
                    str = mBaseActivity.getString(R.string.weibo_error_request_freq_beyond_limit);
                    break;
                
                default:
                    str = mBaseActivity.getString(R.string.hint_error) + e.toString();
                    break;
            }
        } else {
            str = mBaseActivity.getString(R.string.hint_error) + e.toString();
        }
        
        mBaseActivity.showToastOnUIThread(str);
    }

    /* (non-Javadoc)
     * @see com.weibo.sdk.android.net.RequestListener#onIOException(java.io.IOException)
     */
    @Override
    public void onIOException(IOException e) {
        KLog.w(TAG, "onIOException", e);
        mBaseActivity.showToastOnUIThread(mBaseActivity.getString(R.string.hint_network_error) + e.toString());            
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
