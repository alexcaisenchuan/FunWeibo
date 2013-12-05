package com.alex.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络相关工具类
 * @author caisenchuan
 */
public class NetworkUtils {
    /*--------------------------
     * 常量
     *-------------------------*/
    /**
     * 网络类型定义
     */
    public enum NetworkType {
        /**未知类型*/
        NETWORK_UNKNOWN,
        /**无网络*/
        NETWORK_NONE,
        /**Wifi或者WiMax*/
        NETWORK_WIFI,
        /**移动网络*/
        NETWORK_MOBILE,
        /**有线网*/
        NETWORK_ETHERNET,
        /**其他类型*/
        NETWORK_OTHERS
    }
    
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
     * 读取当前网络类型
     */
    public static NetworkType getCurrentNetworkType(Context context) {  
        NetworkType ret = NetworkType.NETWORK_UNKNOWN;
        
        if (context != null) {  
            ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
            if(manager != null) {
                NetworkInfo info = manager.getActiveNetworkInfo();  
                if (info != null) {
                    boolean available = info.isAvailable();
                    if(available) {
                        int type = info.getType();
                        switch(type) {
                            case ConnectivityManager.TYPE_WIFI:
                            case ConnectivityManager.TYPE_WIMAX:
                                ret = NetworkType.NETWORK_WIFI;
                                break;
                            
                            case ConnectivityManager.TYPE_MOBILE:
                            case ConnectivityManager.TYPE_MOBILE_DUN:
                            case ConnectivityManager.TYPE_MOBILE_HIPRI:
                            case ConnectivityManager.TYPE_MOBILE_MMS:
                            case ConnectivityManager.TYPE_MOBILE_SUPL:
                                ret = NetworkType.NETWORK_MOBILE;
                                break;
                                
                            case ConnectivityManager.TYPE_ETHERNET:
                                ret = NetworkType.NETWORK_ETHERNET;
                                break;
                                
                            default:
                                ret = NetworkType.NETWORK_OTHERS;
                                break;
                        }
                    } else {
                        ret = NetworkType.NETWORK_NONE;
                    }
                }
            }
        }
        
        return ret;
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
