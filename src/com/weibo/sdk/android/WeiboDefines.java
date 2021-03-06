package com.weibo.sdk.android;

public class WeiboDefines {
    public static final String CLIENT_ID = "client_id";
    
    public static final String RESPONSE_TYPE = "response_type";
    
    public static final String USER_REDIRECT_URL = "redirect_uri";
    
    public static final String DISPLAY = "display";
    
    public static final String USER_SCOPE = "scope";
    
    public static final String PACKAGE_NAME = "packagename";
    
    public static final String KEY_HASH = "key_hash";

    ////////////////////////返回值定义//////////////////////
    /**返回空数组*/
    public static final String RET_EMPTY_ARRAY = "[]";
    /**微博列表*/
    public static final String RET_TAG_STATUSES = "statuses";
    /**hasvisible，不知道作用，boolean型*/
    public static final String RET_TAG_HAS_VISIBLE = "hasvisible";
    /**上一个位置，分页获取时使用，long型*/
    public static final String RET_TAG_PREV_CURSOR = "previous_cursor";
    /**下一个位置，分页获取时使用，long型*/
    public static final String RET_TAG_NEXT_CURSOR = "next_cursor";
    /**总数，long型*/
    public static final String RET_TAG_TOTAL_NUMBER = "total_number";
    
    ////////////////////////各个最大值//////////////////////
    /**Poi搜索的最大半径，单位：米*/
    public static final int MAX_POI_SEARCH_RANGE = 10000;
    /**Poi每次读取的数目上限*/
    public static final int MAX_POI_COUNT_TO_GET_ONE_TIME = 50;
    /**微博字数上限*/
    public static final int MAX_STATUS_CONTENT_LENGTH = 140;
}
