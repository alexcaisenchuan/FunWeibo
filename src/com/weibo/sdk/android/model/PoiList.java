/**
 * <p>Title: PoiList.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-11-9
 * @version 1.0
 */
package com.weibo.sdk.android.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.alex.common.utils.KLog;

/**
 * Poi列表
 * @author caisenchuan
 */
public class PoiList {
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "PoiList";

    ///////////////////服务器返回的json字段定义//////////////////////
    /**poi列表*/
    private static final String TAG_POIS = "pois";
    /**总数*/
    private static final String TAG_TOTAL_NUM = "total_number";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**某次查询中的poi总条目数*/
    private int mTotal = 0;
    /**当前已经请求下来的poi条目数*/
    private List<Poi> mList = new ArrayList<Poi>();
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * Poi列表
     */
    public PoiList() {
        //...
    }
    
    /**
     * 根据服务器返回的json字符串构造PoiList对象
     */
    public PoiList(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        mList = getPoiList(json);
        mTotal = json.getInt(TAG_TOTAL_NUM);
        KLog.d(TAG, "total : " + mTotal);
    }
    
    /**
     * 补充当前对象的列表
     * @param list 用于补充当前对象列表的list
     * @author caisenchuan
     */
    public synchronized boolean addAll(List<Poi> list) {
        boolean ret = false;
        if(list != null) {
            ret = mList.addAll(list);
        }
        return ret;
    }
    
    /**
     * 补充当前对象的列表，同时会更新total的值
     * @param jsonStr 用于补充当前对象列表的json字符串，服务端返回
     * @author caisenchuan
     * @throws JSONException 
     */
    public synchronized List<Poi> appendList(String jsonStr) throws JSONException {
        List<Poi> ret = new ArrayList<Poi>();
        JSONObject json = new JSONObject(jsonStr);
        ret = getPoiList(json);
        mList.addAll(ret);
        mTotal = json.getInt(TAG_TOTAL_NUM);
        KLog.d(TAG, "total : " + mTotal);
        return ret;
    }
    
    /**
     * 获取poi列表
     * @return
     * @author caisenchuan
     */
    public synchronized List<Poi> getList() {
        List<Poi> list = new ArrayList<Poi>();
        list.addAll(mList);
        return list;
    }
    
    /**
     * 清空列表
     */
    public synchronized void clear() {
        mList.clear();
    }
    
    /**
     * 得到poi列表的总长度
     * @return
     * @author caisenchuan
     */
    public int getTotal() {
        return mTotal;
    }
    
    /**
     * 设置poi列表的总长度
     * @param total
     */
    public void setTotal(int total) {
        mTotal = total;
    }

    /**
     * 读取某个偏移量对应的poi
     * @param pos
     * @return
     */
    public synchronized Poi get(int pos) {
        return mList.get(pos);
    }
    
    /**
     * 获取当前poi列表长度
     * @return
     */
    public synchronized int size() {
        return mList.size();
    }
    
    /**
     * 判断是否还能继续加载poi项目
     * @return
     * @author caisenchuan
     */
    public synchronized boolean hasMore() {
        KLog.d(TAG, "hasMore , size : %s , total : %s", mList.size(), mTotal);
        if(mList.size() < mTotal) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 根据服务器返回的json字符串返回poi列表
     * @param jsonStr
     * @return
     * @throws JSONException
     * @author caisenchuan
     */
    public static List<Poi> getPoiList(String jsonStr) throws JSONException {
        List<Poi> ret = new ArrayList<Poi>();
        if(!TextUtils.isEmpty(jsonStr)) {
            JSONObject json = new JSONObject(jsonStr);
            ret = getPoiList(json);
        }
        return ret;
    }
    
    /**
     * 根据服务器返回的json对象返回poi列表
     * @param json
     * @return
     * @throws JSONException
     * @author caisenchuan
     */
    public static List<Poi> getPoiList(JSONObject json) throws JSONException {
        List<Poi> ret = new ArrayList<Poi>();
        if(json != null) {
            JSONArray arr = json.getJSONArray(TAG_POIS);
            if(arr != null) {
                for(int i = 0; i < arr.length(); i++) {
                    try {
                        JSONObject poi_json = arr.getJSONObject(i);
                        Poi poi = new Poi(poi_json);
                        ret.add(poi);
                    } catch (JSONException e) {
                        KLog.w(TAG, "Exception while handle poi json", e);
                        continue;
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * 通过json字符串读取出其中的total字段
     * @param jsonStr
     * @return
     * @throws JSONException
     */
    public static int getTotalByJsonStr(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        int total = json.getInt(TAG_TOTAL_NUM);
        return total;
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
