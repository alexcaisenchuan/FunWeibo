/**
 * <p>Title: Place.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-11-9
 * @version 1.0
 */
package com.weibo.sdk.android.model;


import com.alex.common.utils.KLog;
import com.weibo.sdk.android.org.json.JSONException;
import com.weibo.sdk.android.org.json.JSONObject;

/**
 * annotations中的place
 * @author caisenchuan
 */
public class Place implements java.io.Serializable{
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /**
     * 
     */
    private static final long serialVersionUID = 1967776447566887635L;

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "Place";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    public String poiid = "";
    public String title = "";
    public double latitude = 0.0;
    public double longtitude = 0.0;
    public String type = "";
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 使用Json对象构造Place
     */
    public Place(JSONObject json) throws JSONException{
        this.poiid = json.optString("poiid", "");
        this.title = json.optString("title", "");
        
        try {
            //这部分参数允许缺少，容错
            this.latitude = json.optDouble("lat", 0.0);
            this.longtitude = json.optDouble("lon", 0.0);
            this.type = json.optString("type", "");
        } catch (Exception e) {
            KLog.w(TAG, "Exception", e);
        }
        
        KLog.d(TAG, toString());
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String ret = String.format("poiid : %s , title : %s , lat : %s , lon : %s , type : %s",
                                    poiid, title, latitude, longtitude, type);
        return ret;
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
