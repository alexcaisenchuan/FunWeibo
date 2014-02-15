/**
 * <p>Title: Position.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-19
 * @version 1.0
 */
package com.alex.funweibo.model;

import com.baidu.location.BDLocation;


/**
 * 位置对象
 * @author caisenchuan
 *
 */
public class Position {
    /*--------------------------
     * 常量
     *-------------------------*/
    //private static final String TAG = "Position";
    
    /**默认的无效值*/
    private static final double INVALID_VALUE = -256.0;
    
    /*--------------------------
     * 属性
     *-------------------------*/
    /**纬度*/
    private double latitude = INVALID_VALUE;
    /**经度*/
    private double longtitude = INVALID_VALUE;
    /**地址*/
    private String address = "";
    /**方向*/
    private float direction = 0.0f;
    /**速度*/
    private float speed = 0.0f;
    /**定位精度，单位：米*/
    private float radius = 0.0f;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 构造一个Position对象
     */
    public Position() {
        //...
    }
    
    /**
     * 构造一个Position对象
     */
    public Position(double latitude, double longtitude) {
        this.latitude = latitude;
        this.longtitude = longtitude;
    }
    
    /**
     * 获取纬度
     * @return
     */
    public double getLat() {
        return this.latitude;
    }
    
    /**
     * 获取经度
     * @return
     */
    public double getLon() {
        return this.longtitude;
    }
    
    /**
     * 获取地址
     * @return
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * @return the speed
     */
    public float getSpeed() {
        return speed;
    }
    
    /**
     * @return the direction
     */
    public float getDirection() {
        return direction;
    }
    
    /**
     * @return the radius
     */
    public float getRadius() {
        return radius;
    }
    
    /**
     * 设置纬度
     */
    public void setLat(double lat) {
        this.latitude = lat;
    }
    
    /**
     * 设置经度
     */
    public void setLon(double lon) {
        this.longtitude = lon;
    }
    
    /**
     * 设置地址
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(float direction) {
        this.direction = direction;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    /**
     * @param radius the radius to set
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    /**
     * 当前位置是否是有效的位置
     * @return
     */
    public boolean isValid() {
        return isValid(latitude, longtitude);
    }
    
    /**用于坐标转换*/
    private static double x_pi = Math.PI * 3000.0 / 180.0;
    
    /**
     * GCJ-02 坐标转换成 BD-09 坐标
     */
    public static Position gcj_to_bd(Position pos) {
        return gcj_to_bd(pos.latitude, pos.longtitude);
    }
    
    /**
     * GCJ-02 坐标转换成 BD-09 坐标
     */
    public static Position gcj_to_bd(double gg_lat, double gg_lon) {
        double x = gg_lon, y = gg_lat; 
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new Position(bd_lat, bd_lon);
    }
    
    /**
     * BD-09 坐标转换成 GCJ-02 坐标
     */
    public static Position bd_to_gcj(Position pos) {
        return bd_to_gcj(pos.latitude, pos.longtitude);
    }
    
    /**
     * BD-09 坐标转换成 GCJ-02 坐标
     */
    public static Position bd_to_gcj(double bd_lat, double bd_lon)  {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new Position(gg_lat, gg_lon);
    }
    
    /**
     * 某个位置是否是有效的位置
     * @return
     */
    public static boolean isValid(double lat, double lon) {
        boolean ret = false;
        
        if((lat == 0.0 && lon == 0.0) || lat == 4.9E-324 || lon == 4.9E-324) {
            //对于百度定位来说，两个都返回0也是无效的地址
            //4.9E-324也是无效的地址
            ret = false;
        } else {
            //纬度：-90 ~ 90
            //经度：-180 ~ 180
            if(lat >= -90.0 && lat <= 90.0 &&
               lon >= -180.0 && lon <= 180.0) {
                ret = true;
            }
        }
        
        return ret;
    }
    
    /**
     * 某个百度位置是否是有效的位置
     * @return
     */
    public static boolean isValid(BDLocation loc) {
        boolean ret = false;
        
        if(loc != null) {
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            
            ret = isValid(lat, lon);
        }
        
        return ret;
    }
    
    /**
     * 判断两个位置是否相等，使用坐标系作为依据
     */
    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        
        if(obj instanceof Position) {
            Position pos = (Position)obj;
            if(pos.latitude == this.latitude &&
               pos.longtitude == this.longtitude) {
                ret = true;
            } else {
                ret = false;
            }
        } else {
            ret =  false;
        }
        
        return ret;
    }
    
    /**
     * 生成此对象对应的hashcode，之所以要用这个是因为在HashMap比较时会用到此函数；
     * 根据Java文档的说明，当两个对象equals返回true时，他们的hashCode也必须相同；
     * 这里我们使用坐标对应的字符串hashcode算法作为我们的算法；
     */
    @Override
    public int hashCode() {
        //使用坐标字符串的hash code作为此对象的hash code
        String str = String.format("%s,%s", this.latitude, this.longtitude);
        int hash = str.hashCode();
        return hash;
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    
    /*--------------------------
     * private方法
     *-------------------------*/
}
