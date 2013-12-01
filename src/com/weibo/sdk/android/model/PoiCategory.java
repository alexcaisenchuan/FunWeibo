package com.weibo.sdk.android.model;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

/**
 * Poi分类
 * @author caisenchuan
 */
public class PoiCategory {
    /*--------------------------
     * 常量
     *-------------------------*/
    /**所有分类*/
    public static final String ALL_CATEGORY = "0";
    /**默认Poi分类*/
    public static final String DEFAULT_POI_CATEGORY = "64";
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**分类id*/
    public String id;
    /**分类名称*/
    public String name;
    /**父级分类id*/
    public String pid;
    
    /**Poi分类列表*/
    public static List<PoiCategory> mCategorys = new ArrayList<PoiCategory>();
    static {
        mCategorys.add(new PoiCategory("19", "出行住宿", "0"));
        mCategorys.add(new PoiCategory("44", "楼宇机构", "0"));
        mCategorys.add(new PoiCategory("51", "校园生活", "0"));
        mCategorys.add(new PoiCategory("64", "餐饮美食", "0"));
        mCategorys.add(new PoiCategory("115", "购物服务", "0"));
        mCategorys.add(new PoiCategory("169", "生活娱乐", "0"));
        mCategorys.add(new PoiCategory("194", "公园户外", "0"));
        mCategorys.add(new PoiCategory("258", "公司", "0"));
        mCategorys.add(new PoiCategory("601", "地点", "0"));
    }
    /*--------------------------
     * public方法
     *-------------------------*/
    public PoiCategory(String id, String name, String pid) {
        this.id = id;
        this.name = name;
        this.pid = pid;
    }
    
    /**
     * 根据id获得对应的位置
     * @param id
     * @return >= 0 对应的位置; < 0 查找失败;
     */
    public static int getPositionById(String id) {
        int ret = -1;
        
        if(!TextUtils.isEmpty(id)) {
            int len = mCategorys.size();
            for(int i = 0; i < len; i++) {
                if(mCategorys.get(i).id.equals(id)) {
                    ret = i;
                    break;
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
