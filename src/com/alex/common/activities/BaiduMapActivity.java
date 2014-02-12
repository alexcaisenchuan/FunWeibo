package com.alex.common.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alex.common.AppConfig;
import com.alex.common.OnHttpRequestReturnListener;
import com.alex.common.model.GeoPos;
import com.alex.common.utils.BaiduMapUtils;
import com.alex.common.utils.KLog;
import com.alex.common.utils.ShareUtils;
import com.alex.common.utils.SmartToast;
import com.alex.funweibo.R;
import com.alex.funweibo.model.Position;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.ShortUrlAPI;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 谷歌地图界面
 * @author caisenchuan
 */
public class BaiduMapActivity extends BaseActivity {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = BaiduMapActivity.class.getSimpleName();
    
    /**设置用户当前所在位置*/
    public static final String INTENT_SET_MY_POS = "set_my_pos";
    /**添加标记点*/
    public static final String INTENT_ADD_MARKER = "add_marker";
    
    /**设置纬度*/
    public static final String EXTRA_LATITUDE    = "latitude";
    /**设置经度*/
    public static final String EXTRA_LONGTITUDE  = "longtitude";
    /**设置标题*/
    public static final String EXTRA_TITLE       = "title";
    /**设置地址*/
    public static final String EXTRA_ADDRESS     = "address";
    
    //message.what
    /**打开Pop窗*/
    private static final int MSG_OPEN_POP_WINDOW = 1;
    
    /** 默认的缩放级别*/
    private static final float DEFAULT_ZOOM_LEVEL   = 15.0f;
    /**弹出窗与标注点之间的偏移量*/
    private static final int POP_OFFSET_PX          = 80;
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 要处理overlay点击事件时需要继承ItemizedOverlay
     * 不处理点击事件时可直接生成ItemizedOverlay.
     */
    private class MyOverlay extends ItemizedOverlay<OverlayItem> {
        //用MapView构造ItemizedOverlay
        public MyOverlay(Drawable mark, MapView mapView){
            super(mark, mapView);
        }
        
        protected boolean onTap(int index) {
            //在此处理item点击事件
            OverlayItem item = getItem(index);      //获取对应的OverlayItem
            if(item != null) {
                Message msg = mHandler.obtainMessage(MSG_OPEN_POP_WINDOW, item);
                mMapView.getController().animateTo(item.getPoint(), msg);
            }
            return true;
        }
        
        public boolean onTap(GeoPoint pt, MapView mapView){
            //在此处理MapView的点击事件，当返回 true时
            if(mPopWindow != null) {
                mPopWindow.hidePop();
                mMapView.removeView(mPopContent);
            }
            super.onTap(pt, mapView);
            return false;
        }
    }
    
    /**
     * 弹出窗点击响应器
     */
    private class MyPopupClickListener implements PopupClickListener {

        @Override
        public void onClickedPopup(int arg0) {
            BaiduMapUtils.openBaiduMap(BaiduMapActivity.this, mMarkerLat, mMarkerLon, mMarkerTitle, mMarkerAddress, getString(R.string.app_name));
        }
        
    }
    
    /**
     * 常用事件监听，用来处理通常的网络错误，授权验证错误等
     */
    private class MyGeneralListener implements MKGeneralListener {
        
        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                if(AppConfig.DEBUG) {
                    SmartToast.showLongToast(BaiduMapActivity.this, R.string.hint_check_network, false);
                }
            } else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                if(AppConfig.DEBUG) {
                    SmartToast.showLongToast(BaiduMapActivity.this, R.string.hint_check_find_case, false);
                }
            }
        }

        @Override
        public void onGetPermissionState(int iError) {
            //非零值表示key验证未通过
            if (iError != 0) {
                KLog.w(TAG, "onGetPermissionState faild : %s", iError);
                if(AppConfig.DEBUG) {
                    SmartToast.showLongToast(BaiduMapActivity.this, getString(R.string.hint_auth_faild) + iError, false);
                }
            } else {
                KLog.d(TAG, "onGetPermissionState success");
                if(AppConfig.DEBUG) {
                    SmartToast.showLongToast(BaiduMapActivity.this, getString(R.string.hint_auth_success), false);
                }
            }
        }
    }
    
    /**
     * 位置监听器
     */
    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation loc) {
            if (loc == null) {
                return;
            }
            
            KLog.d(TAG, "onReceiveLocation : %s", loc);
            Position p = new Position(loc.getLatitude(), loc.getLongitude());
            p.setDirection(loc.getDerect());
            p.setSpeed(loc.getSpeed());
            p.setRadius(loc.getRadius());
            setMyLocation(p);
        }

        @Override
        public void onReceivePoi(BDLocation loc) {
            if (loc == null) {
                return;
            }
            
            KLog.d(TAG, "onReceivePoi : %s", loc);
        }
        
    }
    
    /**
     * 此Activity的handler
     * @author caisenchuan
     */
    private class MapHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_OPEN_POP_WINDOW: {
                    if(msg.obj instanceof OverlayItem) {
                        OverlayItem item = (OverlayItem)msg.obj;
                        mPopContent.setText(item.getTitle());
                        mPopWindow.showPopup(mPopContent, item.getPoint(), POP_OFFSET_PX);
                    }
                    break;
                }
                
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /** 地图管理器 */
    private BMapManager mBMapMan = null;
    /**地图视图*/
    private MapView mMapView = null;
    /**全局地图控制器*/
    private MapController mMapController = null;
    /**弹出框*/
    private PopupOverlay mPopWindow = null;
    /**弹出框的内容*/
    private Button mPopContent = null;
    /**我的位置图层*/
    private MyLocationOverlay myLocationOverlay = null;
    /**地图的handler*/
    private MapHandler mHandler = new MapHandler();
    /**位置监听器*/
    private MyLocationListener mLocationListener = new MyLocationListener();
    
    /**标记点纬度*/
    private double mMarkerLat = 0.0;
    /**标记点经度*/
    private double mMarkerLon = 0.0;
    /**标记点标题*/
    private String mMarkerTitle = "";
    /**标记点地址*/
    private String mMarkerAddress = "";
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 打开地图，默认添加一个标记点，并且把地图中心设为此标记点
     */
    public static void openMapWithMarker(Context context, double lat, double lon, String title, String address) {
        Intent it = new Intent(context, BaiduMapActivity.class);
        
        //添加标注点
        Bundle b = new Bundle();
        b.putDouble(EXTRA_LATITUDE, lat);
        b.putDouble(EXTRA_LONGTITUDE, lon);
        b.putString(EXTRA_TITLE, title);
        b.putString(EXTRA_ADDRESS, address);
        it.putExtra(INTENT_ADD_MARKER, b);
        
        context.startActivity(it);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        
        //我的位置
        MenuItem my = menu.add(0, 0, 0, R.string.button_my);
        my.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        my.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                spanToMyLocation();
                return false;
            }
        });
        
        //分享
        MenuItem add = menu.add(0, 0, 0, R.string.button_share);
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                share();
                return false;
            }
        });
        
        return true;
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        KLog.d(TAG, "onCreate");
        
        //注意：请在试用setContentView前初始化BMapManager对象，否则会报错
        initMapManager();
        
        //初始化界面元素
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_map);
        initView();
        
        //注册地图
        initMap();
        
        //处理Intent
        handleIntent();
        
        //设置用户所处位置
        Position p = mApp.getCurrentLocation();
        setMyLocation(p);
    }
    
    @Override
    protected void onPause() {
        //注销位置监听器
        mApp.getLocationClient().unRegisterLocationListener(mLocationListener);
        //MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
        mMapView.onPause();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        //注册位置监听器
        mApp.getLocationClient().registerLocationListener(mLocationListener);
        //MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
        mMapView.onResume();
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        //MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.destroy();
        super.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
        
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMapView.onRestoreInstanceState(savedInstanceState);
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 初始化地图管理器，注意：请在试用setContentView前初始化BMapManager对象，否则会报错
     */
    private void initMapManager() {
        mBMapMan = new BMapManager(getApplication());
        if(!mBMapMan.init(AppConfig.BAIDU_API_KEY, new MyGeneralListener())) {
            KLog.w(TAG, "BMapManager init faild!");
            if(AppConfig.DEBUG) {
                SmartToast.showLongToast(this, R.string.hint_init_faild, false);
            }
        }
    }
    
    /**
     * 初始化界面
     */
    private void initView() {
        mActionBar.setTitle(R.string.title_map);
        
        mPopContent = new Button(this);
        mPopContent.setMaxWidth(500);
        mPopContent.setTextSize(18);
        mPopContent.setBackgroundResource(R.drawable.selector_bg_pop);
        mPopContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BaiduMapUtils.openBaiduMap(BaiduMapActivity.this, mMarkerLat, mMarkerLon, mMarkerTitle, mMarkerAddress, getString(R.string.app_name));
            }
        });
    }
    
    /**
     * 初始化地图
     */
    private void initMap() {
        //设置MapView
        mMapView = (MapView)findViewById(R.id.bmapsView);
        mMapView.setBuiltInZoomControls(true);                      //设置启用内置的缩放控件
        mMapView.showScaleControl(true);                            //显示比例尺
        
        //地图控制器
        mMapController = mMapView.getController();
        
        //我的位置
        myLocationOverlay = new MyLocationOverlay(mMapView);
        
        //设置弹出窗
        mPopWindow = new PopupOverlay(mMapView, new MyPopupClickListener());
    }

    /**
     * 处理Intent设置
     */
    private void handleIntent() {
        Intent it = getIntent();
        
        //若设置了添加Marker
        Bundle extra = it.getBundleExtra(INTENT_ADD_MARKER);
        if(extra != null) {
            double lat = extra.getDouble(EXTRA_LATITUDE, 0.0);
            double lon = extra.getDouble(EXTRA_LONGTITUDE, 0.0);
            String title = extra.getString(EXTRA_TITLE, "");
            String address = extra.getString(EXTRA_ADDRESS, "");
            
            mMarkerLat = lat;
            mMarkerLon = lon;
            mMarkerTitle = title;
            mMarkerAddress = address;
            
            addMarker(lat, lon, title, address);
            
            setCenter(lat, lon);
        }
    }
    
    /**
     * 设置地图中心
     */
    private void setCenter(double lat, double lon) {
        GeoPoint point = CoordinateConvert.fromGcjToBaidu(new GeoPos(lat, lon));      //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
        mMapController.setCenter(point);                                //设置地图中心点
        mMapController.setZoom(DEFAULT_ZOOM_LEVEL);                     //设置地图zoom级别
    }
    
    /**
     * 把一个标注点添加到地图上
     */
    private void addMarker(double lat, double lon, String title, String address) {
        //标记点图像
        Drawable mark = getResources().getDrawable(R.drawable.icon_gcoding);
        //创建Overlay
        MyOverlay overlay = new MyOverlay(mark, mMapView);
        
        //将Overlay添加到地图上
        mMapView.getOverlays().clear();
        mMapView.getOverlays().add(overlay);
         
        //添加标注点
        GeoPoint p = CoordinateConvert.fromGcjToBaidu(new GeoPos(lat, lon));
        OverlayItem item = new OverlayItem(p, title, address);
        overlay.addItem(item);
        sendMessage(MSG_OPEN_POP_WINDOW, 0, 0, item);
        
        //刷新地图
        mMapView.refresh();
    }
    
    /**
     * 设置用户当前位置
     * @param p 要设置的位置，gcj坐标系
     */
    private void setMyLocation(Position p) {
        if(p != null && p.isValid()) {
            //坐标转换
            GeoPoint gp = CoordinateConvert.fromGcjToBaidu(new GeoPos(p.getLat(), p.getLon()));
            //创建位置
            LocationData locData = new LocationData();
            locData.latitude = gp.getLatitudeE6() * 1.0 / 1E6;
            locData.longitude = gp.getLongitudeE6() * 1.0 / 1E6;
            locData.direction = p.getDirection();
            locData.accuracy = p.getRadius();
            locData.speed = p.getSpeed();
            //设置位置
            myLocationOverlay.setData(locData);
            if(!mMapView.getOverlays().contains(myLocationOverlay)) {
                mMapView.getOverlays().add(myLocationOverlay);
            } else {
                //KLog.d(TAG, "overlay added al-ready!");
            }
            mMapView.refresh();
        }
    }
    
    /**
     * 地图动画移动到我的位置
     */
    private void spanToMyLocation() {
        if(myLocationOverlay != null) {
            LocationData data = myLocationOverlay.getMyLocation();
            GeoPos point = new GeoPos(data.latitude, data.longitude);
            mMapView.getController().animateTo(point);
        }
    }
    
    /**
     * 给handler发送消息
     */
    private void sendMessage(int what, int arg1, int arg2, Object obj) {
        if(mHandler != null) {
            Message msg = new Message();
            msg.what = what;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.obj  = obj;
            mHandler.sendMessage(msg);
        }
    }
    
    /**
     * 分享地点
     */
    private void share() {
        String url = BaiduMapUtils.getMarkerHtmlString(mMarkerLat, mMarkerLon, mMarkerTitle, mMarkerAddress, getString(R.string.app_name));
        ShortUrlAPI api = new ShortUrlAPI(mToken);
        String url_long[] = {url};
        api.shorten(url_long, new OnShortenUrlListener(this, url));     //转换成短链接
    }
    
    /**
     * 得到短链接后的处理监听器
     */
    private class OnShortenUrlListener extends OnHttpRequestReturnListener {

        /**分享标题*/
        private String shareTitle = "";
        /**分享正文*/
        private String shareText = "";
        
        public OnShortenUrlListener(BaseActivity base, String ori_url) {
            super(base);
            this.shareTitle = String.format("%s%s", getString(R.string.app_name), getString(R.string.common_share));
            this.shareText = String.format("%s : %s , (%s%s)", 
                                            mMarkerTitle, ori_url, getString(R.string.text_share_from), getString(R.string.app_name));
        }

        @Override
        public void onComplete(String arg0) {
            //解析返回json
            String url = getRetShortenUrl(arg0);
            
            //若有效，则进行分享
            if(!TextUtils.isEmpty(url)) {
                //组装分享内容
                shareText = String.format("%s : %s , (%s%s)", 
                                           mMarkerTitle, url, getString(R.string.text_share_from), getString(R.string.app_name));
            } else {
                KLog.w(TAG, "getRetShortenUrl faild!");
            }
            
            //分享
            ShareUtils.share(BaiduMapActivity.this, shareTitle, shareText, null);
        }
        
        @Override
        public void onComplete4binary(ByteArrayOutputStream arg0) {
            super.onComplete4binary(arg0);
            //分享原网址
            ShareUtils.share(BaiduMapActivity.this, shareTitle, shareText, null);
        }
        
        @Override
        public void onError(WeiboException e) {
            super.onError(e);
            //分享原网址
            ShareUtils.share(BaiduMapActivity.this, shareTitle, shareText, null);
        }
        
        @Override
        public void onIOException(IOException e) {
            super.onIOException(e);
            //分享原网址
            ShareUtils.share(BaiduMapActivity.this, shareTitle, shareText, null);
        }
        
        /**
         * 从返回的字符串中得到短链接
         * @param str
         * @return
         */
        private String getRetShortenUrl(String str) {
            String ret = "";
            
            KLog.d(TAG, "getRetShortenUrl : %s", str);
            if(!TextUtils.isEmpty(str)) {
                try {
                    JSONObject json = new JSONObject(str);
                    JSONArray arr = json.getJSONArray("urls");
                    if(arr != null && arr.length() >= 1) {
                        JSONObject obj = arr.getJSONObject(0);
                        String s_url = obj.optString("url_short", "");
                        if(!TextUtils.isEmpty(s_url)) {
                            ret = s_url;
                        }
                    }
                } catch (JSONException e) {
                    KLog.w(TAG, "JSONException", e);
                }
            }
            return ret;
        }
    }
}
