/**
 * <p>Title: ActivityLogin.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.funweibo.activities;

import java.text.SimpleDateFormat;

import com.alex.common.AppConfig;
import com.alex.yaha.R;
import com.alex.common.activities.BaseActivity;
import com.alex.common.keep.WeiboAuthInfoKeeper;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.KLog;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.sso.SsoHandler;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

/**
 * 登录界面
 * @author caisenchuan
 *
 */
public class ActivityLogin extends BaseActivity implements OnClickListener{
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    class AuthDialogListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            
            String code = values.getString("code");
            if(code != null){
                SmartToast.showShortToast(ActivityLogin.this, 
                                          R.string.hint_weibo_auth_code_success,
                                          false);
                return;
            }
            
            String token = values.getString("access_token");
            String expires_in = values.getString("expires_in");
            String weibo_userid = values.getString("uid");
            
            Oauth2AccessToken accessToken = new Oauth2AccessToken(token, expires_in);
            mApp.setAccessToken(accessToken);
            mApp.setWeiboUserid(weibo_userid);
            mApp.getCurrentUserInfo();
            
            if (accessToken.isSessionValid()) {
                //保存信息
                WeiboAuthInfoKeeper.keepAccessToken(ActivityLogin.this, accessToken);
                WeiboAuthInfoKeeper.keepUid(ActivityLogin.this, weibo_userid);
                
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                        .format(new java.util.Date(accessToken.getExpiresTime()));
                KLog.d(TAG, "认证成功: \r\n access_token: " + token + "\r\n"
                            + "expires_in: " + expires_in + "\r\n有效期：" + date
                            + "uid : " + weibo_userid);
                
                SmartToast.showShortToast(ActivityLogin.this, 
                                          getString(R.string.hint_weibo_auth_success) + date,
                                          true);
                
                //进入主界面
                gotoMain();
            }
        }

        @Override
        public void onError(WeiboDialogError e) {
            SmartToast.showShortToast(getApplicationContext(),
                                      getString(R.string.hint_weibo_auth_error) + e.getMessage(),
                                      true);
        }

        @Override
        public void onCancel() {
            SmartToast.showShortToast(getApplicationContext(),
                                      getString(R.string.hint_weibo_auth_cancel),
                                      true);
        }

        @Override
        public void onWeiboException(WeiboException e) {
            SmartToast.showShortToast(getApplicationContext(),
                                      getString(R.string.hint_weibo_auth_exception) + e.getMessage(),
                                      true);
        }

    }

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "ActivityLogin";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**微博对象*/
    private Weibo mWeibo = null;
    /**SsoHandler 仅当sdk支持sso时有效*/
    private SsoHandler mSsoHandler = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        switch(id) {
            case R.id.weibo_login: {
                mSsoHandler = new SsoHandler(this, mWeibo);
                mSsoHandler.authorize(new AuthDialogListener(),null);
                break;
            }
            
            default: {
                KLog.w(TAG, "Unknow id onClick : %d", id);
                break;
            }
        }
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //若参数无效，则显示授权界面
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
    
        //创建微博对象
        mWeibo = Weibo.getInstance(AppConfig.WEIBO_APP_KEY, AppConfig.WEIBO_REDIRECT_URL, AppConfig.WEIBO_SCOPE);
        
        //读取保存参数
        if(mApp.isWeiboAuthValid()) {
            //若参数有效，则进入主界面
            gotoMain();
        } else {
            setContentView(R.layout.activity_login);
            
            //设置字体
            TextView tv = (TextView)findViewById(R.id.text_login_title);  
            //从assert中获取有资源，获得app的assert，采用getAserts()，通过给出在assert/下面的相对路径。在实际使用中，字体库可能存在于SD卡上，可以采用createFromFile()来替代createFromAsset。   
            Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Structr_Regular.ttf");  
            tv.setTypeface(face);
            
            //设置界面元素
            findViewById(R.id.weibo_login).setOnClickListener(this);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        KLog.d(TAG, "onDestory");
        
        super.onDestroy();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        KLog.d(TAG, "onActivityResult");
        // sso 授权回调
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/

    /**
     * 进入主界面
     * @author caisenchuan
     */
    private void gotoMain() {
        startActivity(new Intent(ActivityLogin.this,ActivityPopularPOIs.class));
        finish();
    }
}
