/**
 * <p>Title: Poi.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-11-3
 * @version 1.0
 */
package com.weibo.sdk.android.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.alex.common.utils.KLog;

/**
 * 微博的一个Poi(Place Of Interest)的信息
 * @author caisenchuan
 */
public class Poi implements Serializable{
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    /**serialVersionUID*/
    private static final long serialVersionUID = 1011534000477714884L;
    
    private static final String TAG = "Poi";

    /*--------------------------
     * 成员变量
     *-------------------------*/
    ////////////////////属性列表(请随时与微博的字段保持同步)/////////////////
    public String poiid = "";
    public String title = "";
    public String address = "";
    public double latitude = 0.0;
    public double longtitude = 0.0;
    public int    category = 0;
    public String city = "";
    public String province = "";
    public String country = "";
    public String url = "";
    public String phone = "";
    public String postcode = "";
    public String weibo_id = "0";
    public String categorys = "";
    public String category_name = "";
    public String map = "";
    public String poi_pic = "";
    public String icon = "";
    public String poi_street_address = "";
    public long   checkin_num = 0L;
    public long   checkin_user_num = 0L;
    public long   tip_num = 0L;
    public long   photo_num = 0L;
    public long   todo_num = 0L;
    public long   distance = 0L;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 使用Json字符串构造Poi
     */
    public Poi(String str) throws JSONException {
        this(new JSONObject(str));
    }
    
    /**
     * 使用Json对象构造Poi
     */
    public Poi(JSONObject json) {
        this.poiid = json.optString("poiid", "");
        this.title = json.optString("title", "");
        this.latitude = json.optDouble("lat", 0.0);
        this.longtitude = json.optDouble("lon", 0.0);
        this.category = json.optInt("category", 0);
        
        try {
            //这部分参数允许缺少，容错
            this.address = json.optString("address", "");
            this.city = json.optString("city", "");
            this.province = json.optString("province", "");
            this.country = json.optString("country", "");
            this.url = json.optString("url", "");
            this.phone = json.optString("phone", "");
            this.postcode = json.optString("postcode", "");
            this.weibo_id = json.optString("weibo_id", "");
            this.categorys = json.optString("categorys", "");
            this.category_name = json.optString("category_name", "");
            this.map = json.optString("map", "");
            this.poi_pic = json.optString("poi_pic", "");
            this.icon = json.optString("icon", "");
            this.poi_street_address = json.optString("poi_street_address", "");
            this.checkin_num = json.optLong("checkin_num", 0L);
            this.checkin_user_num = json.optLong("checkin_user_num", 0L);
            this.tip_num = json.optLong("tip_num", 0L);
            this.photo_num = json.optLong("photo_num", 0L);
            this.todo_num = json.optLong("todo_num", 0L);
            this.distance = json.optLong("distance", 0L);
        } catch (Exception e) {
            KLog.w(TAG, "Exception", e);
        }
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
