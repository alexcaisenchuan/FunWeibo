package com.alex.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 一些杂项功能
 * @author caisenchuan
 */
public class Misc {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = Misc.class.getSimpleName();
    
    /**地球半径，米*/
    private static final double EARTH_RADIUS = 6378137;
    
    /*--------------------------
     * 方法
     *-------------------------*/
    /**
     * 计算度数
     * */
    private static double rad(double d) {
       return d * Math.PI / 180.0;
    }
    
    /**
     * 通过经纬度计算两点间的距离
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
       double radLat1 = rad(lat1);
       double radLat2 = rad(lat2);
       double a = radLat1 - radLat2;
       double b = rad(lng1) - rad(lng2);

       double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
       Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
       s = s * EARTH_RADIUS;
       s = Math.round(s * 10) / 10;
       return s;
    }
    
    /**
     * 判断某个apk是否安装
     * @param context 上下文，应用级别即可
     * @param pktname 要查询的apk包名
     * @return
     */
    public static boolean isAppInstalled(Context context, String pktname) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        
        try {
            pm.getPackageInfo(pktname, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch(PackageManager.NameNotFoundException e) {
            installed = false;
            KLog.d(TAG, "Exception", e);
        }
        
        return installed;
    }
    
    /**
     * 从Resource解析出Bitmap
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap getBitmapFromResources(Context context, int resId) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, resId);
    }
}
