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

import com.alex.funweibo.model.Position;
import com.alex.common.AppConfig;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.KLog;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.ta.util.cache.TAFileCache;
import com.ta.util.cache.TAFileCache.TACacheParams;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.keep.AccessTokenKeeper;
import com.weibo.sdk.android.keep.UserInfoKeeper;

import android.app.Application;
import android.text.TextUtils;

/**
 * @author caisenchuan
 *
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
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "AppControl";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**微博的access_token*/
    private Oauth2AccessToken mAccessToken = null;
    /**微博的用户id*/
    private String mWeiboUserid = "";
    /**网站的session id*/
    private String mSessionID = "";

    /**百度定位对象 */
    private LocationClient mLocationClient = null;
    /** 定位参数 */
    private LocationClientOption mLocOpt = new LocationClientOption();
    
    /**当前位置，会在后台不断更新*/
    private Position mCurrentLocation = new Position();
    
    /** ThinkAndroid 文件缓存 */
    private TAFileCache mFileCache = null;
    
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

        //授权相关
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
        
        //设置其他
        SmartToast.initSingletonToast(getApplicationContext());
    }

    public void setAccessToken(Oauth2AccessToken token) {
        this.mAccessToken = token;
    }
    
    public void setWeiboUserid(String uid) {
        this.mWeiboUserid = uid;
    }
    
    public void setSessionID(String session) {
        this.mSessionID = session;
    }
    
    public Oauth2AccessToken getAccessToken() {
        return this.mAccessToken;
    }
    
    public String getWeiboUserid() {
        return this.mWeiboUserid;
    }
    
    public String getSessionID() {
        return this.mSessionID;
    }
    
    /**
     * 判断微博授权是否有效
     * @return
     */
    public boolean isWeiboAuthValid() {
        boolean ret = false;
        
        //如果token、userid有效，且没有过期，则认为有效
        if(mAccessToken != null && !TextUtils.isEmpty(mWeiboUserid)) {
            if(mAccessToken.isSessionValid()) {
                long uid = 0;
                try {
                    uid = Long.valueOf(mWeiboUserid);
                } catch (Exception e) {
                    KLog.w(TAG, "weiboUserid is not valid long type", e);
                }
                
                if(uid > 0L) {
                    ret = true;
                } else {
                    ret = false;
                }
            }
        }
        
        return ret;
    }
    
    public LocationClient getLocationClient() {
        return this.mLocationClient;
    }
    
    public Position getCurrentLocation() {
        return this.mCurrentLocation;
    }
    
    /**
     * 获取文件缓存对象
     * @return
     * @author caisenchuan
     */
    public TAFileCache getFileCache() {
        if (mFileCache == null) {
            TACacheParams cacheParams = new TACacheParams(this, AppConfig.SYSTEMCACHE);
            TAFileCache fileCache = new TAFileCache(cacheParams);
            mFileCache = fileCache;
        }
        return mFileCache;
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
        //读取保存参数
        Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(this);
        String weiboUserid = UserInfoKeeper.read(this);
        
        setAccessToken(token);
        setWeiboUserid(weiboUserid);
        
        return;
    }
}
