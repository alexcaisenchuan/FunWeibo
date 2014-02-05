/**
 * <p>Title: AppControl.java</p>
 * <p>Description: 应用总体控制类</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.funweibo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.alex.funweibo.model.Position;
import com.alex.common.AppConfig;
import com.alex.common.keep.WeiboAuthInfoKeeper;
import com.alex.common.keep.WeiboDataKeeper;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.KLog;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.ta.util.bitmap.TABitmapCacheWork;
import com.ta.util.bitmap.TABitmapCallBackHanlder;
import com.ta.util.bitmap.TADownloadBitmapHandler;
import com.ta.util.cache.TAFileCache;
import com.ta.util.cache.TAFileCache.TACacheParams;
import com.ta.util.extend.draw.DensityUtils;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.model.User;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.org.json.JSONException;

import android.app.Application;
import android.text.TextUtils;

/**
 * 整个应用的控制
 * @author caisenchuan
 */
public class AppControl extends Application{
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 全局位置监听器
     * @author caisenchuan
     */
    private class BaseLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //KLog.d(TAG, "onReceiveLocation >>>>>>>>>>>>>>>>>>>>>>>");
            
            if (location == null) {
                return;
            }
            
            //设置全局位置
            if(Position.isValid(location)) {
                mCurrentLocation.latitude = location.getLatitude();
                mCurrentLocation.longtitude = location.getLongitude();
                if(location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    mCurrentLocation.address = location.getAddrStr();
                }
            }
            
            //打印调试
            /*StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            }
            sb.append("\nstreet : ");
            sb.append(location.getStreet());
            sb.append("\ncity : ");
            sb.append(location.getCity());
            
            KLog.d(TAG, sb.toString());*/
            KLog.d(TAG, "onReceiveLocation , lat : %s , lon : %s", location.getLatitude(), location.getLongitude());
        }

        public void onReceivePoi(BDLocation poiLocation) {
            KLog.d(TAG, "onReceivePoi >>>>>>>>>>>>>>>>>>>>>>>");
            
            if (poiLocation == null) {
                return;
            }
            
            //打印调试
            StringBuffer sb = new StringBuffer(256);
            sb.append("Poi time : ");
            sb.append(poiLocation.getTime());
            sb.append("\nerror code : ");
            sb.append(poiLocation.getLocType());
            sb.append("\nlatitude : ");
            sb.append(poiLocation.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(poiLocation.getLongitude());
            sb.append("\nradius : ");
            sb.append(poiLocation.getRadius());
            if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(poiLocation.getAddrStr());
            }
            if (poiLocation.hasPoi()) {
                sb.append("\nPoi:");
                sb.append(poiLocation.getPoi());
            } else {
                sb.append("noPoi information");
            }
            
            KLog.d(TAG, sb.toString());
        }
    }
    
    /**
     * 用户信息读取监听函数
     */
    private class GetUserInfoListener implements RequestListener {

        @Override
        public void onComplete(String arg0) {
            try {
                if(!TextUtils.isEmpty(arg0)) {
                    mCurrentUser = new User(arg0);
                    WeiboDataKeeper.keepCurrUser(getApplicationContext(), arg0);
                    KLog.d(TAG, "current user : %s", mCurrentUser.toString());
                }
            } catch (com.weibo.sdk.android.model.WeiboException e) {
                KLog.w(TAG, "WeiboException", e);
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException", e);
            }
        }

        @Override
        public void onComplete4binary(ByteArrayOutputStream arg0) {
            //...
        }

        @Override
        public void onError(WeiboException arg0) {
            //...
        }

        @Override
        public void onIOException(IOException arg0) {
            //...
        }
        
    }
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "AppControl";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**微博的access_token*/
    private Oauth2AccessToken mAccessToken = null;
    /**当前用户*/
    private User mCurrentUser = null;
    /**微博的用户id*/
    private long mWeiboUserid = 0L;

    /**百度定位对象 */
    private LocationClient mLocationClient = null;
    /** 定位参数 */
    private LocationClientOption mLocOpt = new LocationClientOption();
    
    /**当前位置，会在后台不断更新*/
    private Position mCurrentLocation = new Position();
    
    /** ThinkAndroid 文件缓存 */
    private TAFileCache mFileCache = null;
    
    /**图片缓存加载器*/
    private TABitmapCacheWork mImageFetcher = null;

    /**新微博的临时编号*/
    private int mNewWeiboTempId = 0;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 构造函数
     */
    public AppControl() {
        //...
    }
    
    /* (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        KLog.d(TAG, "onCreate");

        //加载保存信息
        restoreAppInfo();
        
        //设置定位相关参数
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setAK(AppConfig.BAIDU_API_KEY);      //设置Access Key
        // 设置定位参数
        mLocOpt.setOpenGps(true);
        mLocOpt.setAddrType("all");//返回的定位结果包含地址信息
        mLocOpt.setCoorType("gcj02");//返回的定位结果是gcj02,默认值gcj02
        mLocOpt.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
        mLocOpt.disableCache(true);//禁止启用缓存定位
        mLocOpt.setPoiNumber(5);    //最多返回POI个数   
        mLocOpt.setPoiDistance(1000); //poi查询距离        
        mLocOpt.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息        
        mLocationClient.setLocOption(mLocOpt);
        mLocationClient.registerLocationListener(new BaseLocationListener());
        
        //启动定位
        mLocationClient.start();
        
        //缓存设置
        TACacheParams cacheParams = new TACacheParams(this, AppConfig.SYSTEMCACHE);
        TAFileCache fileCache = new TAFileCache(cacheParams);
        mFileCache = fileCache;
        //图片缓存相关
        TADownloadBitmapHandler f = new TADownloadBitmapHandler(this,
                                                                DensityUtils.dipTopx(this, 128),
                                                                DensityUtils.dipTopx(this, 128));
        TABitmapCallBackHanlder taBitmapCallBackHanlder = new TABitmapCallBackHanlder();
        taBitmapCallBackHanlder.setLoadingImage(this, R.drawable.empty_photo);
        mImageFetcher = new TABitmapCacheWork(this);
        mImageFetcher.setProcessDataHandler(f);
        mImageFetcher.setCallBackHandler(taBitmapCallBackHanlder);
        mImageFetcher.setFileCache(mFileCache);
        
        //设置其他
        SmartToast.initSingletonToast(getApplicationContext());
        
        //读取当前登录的用户信息
        getCurrentUserInfo();
    }

    /**
     * 设置access token
     * @param token
     */
    public void setAccessToken(Oauth2AccessToken token) {
        this.mAccessToken = token;
    }
    
    /**
     * 设置用户id
     * @param uid
     */
    public void setWeiboUserid(String uid) {
        try {
            this.mWeiboUserid = Long.valueOf(uid);
        } catch(Exception e) {
            KLog.w(TAG, "Exception", e);
        }
    }
    
    /**
     * 读取access token
     * @return
     */
    public Oauth2AccessToken getAccessToken() {
        return this.mAccessToken;
    }
    
    /**
     * 读取用户id
     * @return
     */
    public long getWeiboUserid() {
        return this.mWeiboUserid;
    }
    
    /**
     * 判断微博授权是否有效
     * @return
     */
    public boolean isWeiboAuthValid() {
        boolean ret = false;
        
        //如果token、userid有效，且没有过期，则认为有效
        if(mAccessToken != null) {
            if(mAccessToken.isSessionValid()) {
                if(mWeiboUserid > 0L) {
                    ret = true;
                } else {
                    ret = false;
                }
            }
        }
        
        return ret;
    }
    
    /**
     * 获取位置管理器
     * @return
     */
    public LocationClient getLocationClient() {
        return this.mLocationClient;
    }
    
    /**
     * 获取当前位置
     * @return
     */
    public Position getCurrentLocation() {
        return this.mCurrentLocation;
    }
    
    /**
     * 获取文件缓存对象
     * @return
     */
    public TAFileCache getFileCache() {
        return mFileCache;
    }
    
    /**
     * 获取图片下载器
     * @return
     */
    public TABitmapCacheWork getImageFetcher() {
        return mImageFetcher;
    }
    
    /**
     * 获取下一个新微博的临时编号
     * @return
     */
    public int getNextNewWeiboTempId() {
        mNewWeiboTempId++;
        return mNewWeiboTempId;
    }
    
    /**
     * 获取当前使用用户
     * @return
     */
    public User getCurrUser() {
        return mCurrentUser;
    }
    
    /**
     * 注销当前用户信息
     */
    public void logout() {
        mAccessToken = null;
        mCurrentUser = null;
        mWeiboUserid = 0L;
        
        WeiboAuthInfoKeeper.clear(this);
        WeiboDataKeeper.clear(this);
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 加载应用信息
     */
    private void restoreAppInfo() {
        //授权信息
        Oauth2AccessToken token = WeiboAuthInfoKeeper.readAccessToken(this);
        String weiboUserid = WeiboAuthInfoKeeper.readUid(this);
        setAccessToken(token);
        setWeiboUserid(weiboUserid);

        //用户信息
        try {
            String userStr = WeiboDataKeeper.readCurrUser(this);
            mCurrentUser = new User(userStr);
        } catch (com.weibo.sdk.android.model.WeiboException e) {
            KLog.w(TAG, "WeiboException", e);
        } catch (JSONException e) {
            KLog.w(TAG, "JSONException", e);
        }
        
        return;
    }
    
    /**
     * 读取当前用户信息
     */
    public void getCurrentUserInfo() {
        if(isWeiboAuthValid()) {
            UsersAPI users = new UsersAPI(mAccessToken);
            users.show(mWeiboUserid, new GetUserInfoListener());
        }
    }
}
