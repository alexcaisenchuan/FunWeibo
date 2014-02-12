package com.alex.common.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * 百度地图相关工具
 * @author caisenchuan
 */
public class BaiduMapUtils {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = BaiduMapUtils.class.getSimpleName();
    
    /**百度地图的应用包名*/
    public static final String BAIDU_MAP_PKT_NAME = "com.baidu.BaiduMap";
    
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
     * 得到显示marker的请求字符串
     */
    public static String getMarkerQueryString(double lat, double lon, String title, String address, String src) {
        String url = String.format("marker?location=%s,%s&title=%s&content=%s&src=%s&coord_type=gcj02", lat, lon, title, address, src);
        return url;
    }
    
    /**
     * 得到在网页上显示marker的请求字符串
     */
    public static String getMarkerHtmlString(double lat, double lon, String title, String address, String src) {
        String query = getMarkerQueryString(lat, lon, title, address, src);
        String ret = String.format("http://api.map.baidu.com/%s&output=html", query);
        KLog.d(TAG, ret);
        return ret;
    }
    
    /**
     * 得到在百度地图显示marker的请求字符串
     */
    public static String getMarkerIntentString(double lat, double lon, String title, String address, String src) {
        String query = getMarkerQueryString(lat, lon, title, address, src);
        String ret = String.format("bdapp://map/%s", query);
        KLog.d(TAG, ret);
        return ret;
    }
    
    /**
     * 启动百度地图应用并且显示marker
     */
    public static void startBaiduMapApp(Activity activity, double lat, double lon, String title, String address, String src) {
        Uri uri = Uri.parse(getMarkerIntentString(lat, lon, title, address, src));
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        it.setPackage(BAIDU_MAP_PKT_NAME);
        activity.startActivity(it);
    }
    
    /**
     * 启动百度地图网页并且显示marker
     */
    public static void gotoBaiduMapHtml(Activity activity, double lat, double lon, String title, String address, String src) {
        Uri uri = Uri.parse(getMarkerHtmlString(lat, lon, title, address, src));  
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(it);
    }
    
    /**
     * 启动百度地图并且显示marker，优先级：应用 -> 网页
     */
    public static void openBaiduMap(Activity activity, double lat, double lon, String title, String address, String src) {
        if(Misc.isAppInstalled(activity, BAIDU_MAP_PKT_NAME)) {
            startBaiduMapApp(activity, lat, lon, title, address, src);
        } else {
            gotoBaiduMapHtml(activity, lat, lon, title, address, src);
        }
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
