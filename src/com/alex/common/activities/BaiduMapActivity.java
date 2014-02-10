package com.alex.common.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alex.common.AppConfig;
import com.alex.common.OnHttpRequestReturnListener;
import com.alex.common.model.GeoPos;
import com.alex.common.utils.KLog;
import com.alex.common.utils.ShareUtils;
import com.alex.funweibo.R;
import com.alex.funweibo.model.Position;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
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
import android.view.MenuItem.OnMenuItemClickListener;
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
    
    /**设置地图中心*/
    public static final String INTENT_SET_CENTER = "set_center";
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
            // TODO Auto-generated method stub
            
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
    /**弹出框*/
    private PopupOverlay mPopWindow = null;
    /**弹出框的内容*/
    private Button mPopContent = null;
    /**地图的handler*/
    private MapHandler mHandler = new MapHandler();
    
    /**标记点纬度*/
    private double mMarkerLat = 0.0;
    /**标记点经度*/
    private double mMarkerLon = 0.0;
    /**标记点标题*/
    private String mMarkerTitle = "";
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 打开地图，默认添加一个标记点，并且把地图中心设为此标记点
     */
    public static void openMapWithMarker(Context context, double lat, double lon, String title) {
        Intent it = new Intent(context, BaiduMapActivity.class);
        
        //设置中心
        Bundle b = new Bundle();
        b.putDouble(EXTRA_LATITUDE, lat);
        b.putDouble(EXTRA_LONGTITUDE, lon);
        it.putExtra(INTENT_SET_CENTER, b);
        
        //添加标注点
        b = new Bundle();
        b.putDouble(EXTRA_LATITUDE, lat);
        b.putDouble(EXTRA_LONGTITUDE, lon);
        b.putString(EXTRA_TITLE, title);
        it.putExtra(INTENT_ADD_MARKER, b);
        
        context.startActivity(it);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        //添加菜单项  
        MenuItem add = menu.add(0, 0, 0, R.string.button_share);
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
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
        
        super.onCreate(savedInstanceState);

        //注意：请在试用setContentView前初始化BMapManager对象，否则会报错
        initMapManager();

        setContentView(R.layout.activity_baidu_map);
        
        mActionBar.setTitle(R.string.title_map);
        
        //初始化界面元素
        mPopContent = new Button(this);
        mPopContent.setBackgroundResource(R.drawable.background_pop);
        
        //注册地图
        initMap();
        
        //处理Intent
        handleIntent();
        
        //设置用户所处位置
        Position p = mApp.getCurrentLocation();
        if(p.isValid()) {
            setMyLocation(p.getLat(), p.getLon());
        }
    }
    
    @Override
    protected void onDestroy(){
        if(mMapView != null) {
            mMapView.destroy();
        }
        if(mBMapMan != null){
            mBMapMan.destroy();
            mBMapMan = null;
        }
        super.onDestroy();
    }
    
    @Override
    protected void onPause(){
        mMapView.onPause();
        if(mBMapMan != null){
           mBMapMan.stop();
        }
        super.onPause();
    }
    
    @Override
    protected void onResume(){
        mMapView.onResume();
        if(mBMapMan != null){
            mBMapMan.start();
        }
        super.onResume();
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 初始化地图管理器，注意：请在试用setContentView前初始化BMapManager对象，否则会报错
     */
    private void initMapManager() {
        mBMapMan = new BMapManager(getApplication());
        mBMapMan.init(AppConfig.BAIDU_API_KEY, null);  
    }
    
    /**
     * 初始化地图
     */
    private void initMap() {
        //设置MapView
        mMapView = (MapView)findViewById(R.id.bmapsView);
        mMapView.setBuiltInZoomControls(true);                      //设置启用内置的缩放控件
        mMapView.showScaleControl(true);                            //显示比例尺
        
        //设置弹出窗
        mPopWindow = new PopupOverlay(mMapView, new MyPopupClickListener());
    }

    /**
     * 处理Intent设置
     */
    private void handleIntent() {
        Intent it = getIntent();
        
        //若设置了地图中心点
        Bundle extra = it.getBundleExtra(INTENT_SET_CENTER);
        if(extra != null) {
            double lat = extra.getDouble(EXTRA_LATITUDE, 0.0);
            double lon = extra.getDouble(EXTRA_LONGTITUDE, 0.0);
            setCenter(lat, lon);
        }
        
        //若设置了添加Marker
        extra = it.getBundleExtra(INTENT_ADD_MARKER);
        if(extra != null) {
            double lat = extra.getDouble(EXTRA_LATITUDE, 0.0);
            double lon = extra.getDouble(EXTRA_LONGTITUDE, 0.0);
            String title = extra.getString(EXTRA_TITLE, "");
            
            mMarkerLat = lat;
            mMarkerLon = lon;
            mMarkerTitle = title;
            
            addMarker(lat, lon, title);
        }
    }
    
    /**
     * 设置地图中心
     */
    private void setCenter(double lat, double lon) {
        MapController mMapController = mMapView.getController();        //得到mMapView的控制权,可以用它控制和驱动平移和缩放
        GeoPos point = new GeoPos(lat, lon);                            //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
        mMapController.setCenter(point);                                //设置地图中心点
        mMapController.setZoom(DEFAULT_ZOOM_LEVEL);                     //设置地图zoom级别
    }
    
    /**
     * 把一个标注点添加到地图上
     */
    private void addMarker(double lat, double lon, String title) {
        //标记点图像
        Drawable mark = getResources().getDrawable(R.drawable.icon_gcoding);
        //创建Overlay
        MyOverlay overlay = new MyOverlay(mark, mMapView);
        
        //将Overlay添加到地图上
        mMapView.getOverlays().clear();
        mMapView.getOverlays().add(overlay);
         
        //添加标注点
        GeoPos p = new GeoPos(lat, lon);
        OverlayItem item = new OverlayItem(p, title, title);
        overlay.addItem(item);
        sendMessage(MSG_OPEN_POP_WINDOW, 0, 0, item);
        
        //刷新地图
        mMapView.refresh();
        
        //删除overlay
        //itemOverlay.removeItem(itemOverlay.getItem(0));
        //mMapView.refresh();
        
        //清除overlay
        // itemOverlay.removeAll();
        // mMapView.refresh();
    }
    
    /**
     * 设置用户当前位置
     */
    private void setMyLocation(double lat, double lon) {
        MyLocationOverlay myLocationOverlay = new MyLocationOverlay(mMapView);
        LocationData locData = new LocationData();
        //手动将位置源置为天安门，在实际应用中，请使用百度定位SDK获取位置信息，要在SDK中显示一个位置，需要使用百度经纬度坐标（bd09ll）
        locData.latitude = lat;
        locData.longitude = lon;
        locData.direction = 2.0f;
        myLocationOverlay.setData(locData);
        mMapView.getOverlays().add(myLocationOverlay);
        mMapView.refresh();
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
        //http://api.map.baidu.com/marker?location=40.047669,116.313082&title=我的位置
        //&content=百度奎科大厦&output=html
        //&src=Fun微博&coord_type=gcj02
        String url = String.format("http://api.map.baidu.com/marker?location=%s,%s&title=%s&content=%s&output=html&src=%s&coord_type=gcj02",
                                      mMarkerLat, mMarkerLon, mMarkerTitle, mMarkerTitle, getString(R.string.app_name));
        //获取短链接
        ShortUrlAPI api = new ShortUrlAPI(mToken);
        String url_long[] = {url};
        api.shorten(url_long, new OnShortenUrlListener(this, url));
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
