package com.alex.common.model;

import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * 地理位置的重新封装
 * @author caisenchuan
 */
public class GeoPos extends GeoPoint{
    /*--------------------------
     * 常量
     *-------------------------*/

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
     * 直接使用double型的经纬度进行构造
     * @param lat
     * @param lon
     */
    public GeoPos(double lat, double lon) {
        super((int)(lat * 1E6), (int)(lon * 1E6));
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 使用int型的经纬度进行构造，单位是微度 (度 * 1E6)
     * @param arg0
     * @param arg1
     */
    private GeoPos(int lat, int lon) {
        super(lat, lon);
    }
}
