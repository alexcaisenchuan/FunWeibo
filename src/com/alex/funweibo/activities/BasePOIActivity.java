package com.alex.funweibo.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SpinnerAdapter;

import com.alex.common.BaseActivity;
import com.alex.common.utils.KLog;
import com.alex.common.utils.OnHttpRequestReturnListener;
import com.alex.common.utils.SmartToast;
import com.alex.funweibo.R;
import com.alex.funweibo.model.Position;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.weibo.sdk.android.WeiboDefines;
import com.weibo.sdk.android.api.PlaceAPI;
import com.weibo.sdk.android.model.Poi;
import com.weibo.sdk.android.model.PoiCategory;
import com.weibo.sdk.android.model.PoiList;
import com.weibo.sdk.android.model.WeiboResponse;

/**
 * poi界面的基类，提供了一些基础功能，包括：<br />
 * 1.mPoiList：poi列表；<br />
 * 2.滑动到底部自动刷新，获取更多poi；<br />
 * 3.加载时的提示框以及加载失败的提示；<br />
 * 4.顶部ActionBar提供类型选择功能；<br />
 * 5.poi列表的操作：选择分类、读取下一组；<br />
 * 6.可设置一次读取poi的个数，以及poi的搜索范围；<br />
 * 7.提供给不同子类的界面展示接口，以及信息读取接口；<br />
 * 
 * @author caisenchuan
 */
public abstract class BasePOIActivity extends BaseActivity {

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = BasePOIActivity.class.getSimpleName();
    
    /**调试打印开关*/
    private static final boolean VERBOSE = true;
    
    ///////////////intent/////////////////
    /**设置默认显示的分类*/
    public static final String INTENT_EXTRA_DEFAULT_CATEGORY = "default_category";
    
    ///////////////mBaseHandler msg what//////////////
    /**继承本类的自定义msg what要从此基值开始设置*/
    protected static final int MSG_POI_ACTIVITY_BASE  = MSG_EXTEND_BASE + 100;
    
    /**打开加载提示框*/
    private static final int MSG_SHOW_LOADING_HINT    = MSG_EXTEND_BASE + 1;
    /**关闭加载提示框*/
    private static final int MSG_DISMISS_LOADING_HINT = MSG_EXTEND_BASE + 2;
    /**得到poi信息时的回调*/
    private static final int MSG_ON_GET_POIS          = MSG_EXTEND_BASE + 3;
    
    /////////////////其他/////////////////
    private static final int SCROLL_STATE_IDLE = 0;
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 读取微博信息的回调函数
     * @author caisenchuan
     */
    private class GetPoisListener extends OnHttpRequestReturnListener {

        /**
         * 读取微博信息的回调函数
         * @param base 用于显示Toast的Activity对象
         */
        public GetPoisListener(BaseActivity base) {
            super(base);
        }

        /* (non-Javadoc)
         * @see com.weibo.sdk.android.net.RequestListener#onComplete(java.lang.String)
         */
        @Override
        public void onComplete(String str) {
            try {
                if(VERBOSE) {
                    KLog.d(TAG, "GetPois , ret : %s", str);
                }

                List<Poi> list = new ArrayList<Poi>();
                if(!TextUtils.isEmpty(str) && 
                   !str.startsWith(WeiboDefines.RET_EMPTY_ARRAY)) {
                    int total = WeiboResponse.getTotalNum(str);
                    list = PoiList.getPoiList(str);
                    sendMessageToBaseHandler(MSG_ON_GET_POIS, total, 0, list);
                }
                onGetPoiList(list);
            } catch (Exception e) {
                KLog.w(TAG, "JSONException while build status", e);
                showToastOnUIThread(getString(R.string.hint_poi_read_faild) + e.toString());
                onLoadFinish();
            } finally {
                mGettingPoiList = false;
            }
        }
        
        /* (non-Javadoc)
         * @see com.alex.wemap.utils.OnHttpRequestReturnListener#onComplete4binary(java.io.ByteArrayOutputStream)
         */
        @Override
        public void onComplete4binary(ByteArrayOutputStream arg0) {
            try {
                super.onComplete4binary(arg0);
            } finally {
                onLoadFinish();
            }
        }
        
        /* (non-Javadoc)
         * @see com.alex.wemap.utils.OnHttpRequestReturnListener#onError(com.weibo.sdk.android.WeiboException)
         */
        @Override
        public void onError(com.weibo.sdk.android.WeiboException e) {
            try {
                super.onError(e);
            } finally {
                onLoadFinish();
            }
        }
        
        /* (non-Javadoc)
         * @see com.alex.wemap.utils.OnHttpRequestReturnListener#onIOException(java.io.IOException)
         */
        @Override
        public void onIOException(IOException e) {
            try {
                super.onIOException(e);
            } finally {
                onLoadFinish();
            }
        }
    }
    
    /**
     * 百度定位的监听器
     */
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            
            //首次定位完成后，读取一次poi列表
            if(!mHasGetPoiList) {
                getNextNearbyPois();
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }
    
    /**
     * 下载菜单选择监听器
     */
    private class SpinnerSelectListener implements OnNavigationListener {

        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            if(itemPosition >= 0 && itemPosition < PoiCategory.mCategorys.size()) {
                PoiCategory c = PoiCategory.mCategorys.get(itemPosition);
                if(c != null) {
                    String category = c.id;
                    KLog.d(TAG, "onNavigationItemSelected , pos : %d , id : %d , category : %s",
                                 itemPosition, itemId, category);
                    if(category != null && !category.equals(mCurrentCategory)) {
                        //类型改变才重新读取
                        //TODO 注意考虑第一次设置默认选项的情况，简单的方法是不把默认放到第一位即可，囧
                        selectCategory(category);
                    }
                }
            }
            return false;
        }
        
    }
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /*-----------protected------------*/
    ////////////////////////////Views/////////////////////////
    /**底部加载提示*/
    protected View mLoadView = null;
    ////////////////////////////数据/////////////////////////
    /**Poi列表*/
    protected PoiList mPoiList = null;
    /**默认选择的分类*/
    String mDefaultCategory = "";
    ///////////////////////////标志位及计数//////////////////
    /**列表中项目的总个数*/
    protected int mCount = 0;
    /**列表中最后一个项目的序号*/
    protected int mLastItem = 0;
    /**poi查询字串，不查询时记得置空*/
    protected String mPoiQuery = "";
    /**当前分类*/
    protected String mCurrentCategory = "";
    /**上一次选择的分类*/
    protected String mLastCategory = "";
    /**查询poi列表的当前页码*/
    protected int mCurrPoiPage = 0;
    /**是否正在读取poi列表*/
    protected boolean mGettingPoiList = false;
    ///////////////////////////其他//////////////////
    /**位置API*/
    protected PlaceAPI mPlaceApi = null;
    
    /*-------------private-----------------*/
    ////////////////////////////设定值////////////////////////
    /**每次读取Poi的个数，最大50*/
    private int mPoiCountToGetOneTime = 5;
    /**Poi搜索半径，单位：米，最大10000*/
    private int mPoiSearchRange = 2000;
    ////////////////////////////其他//////////////////////////
    /**是否已经读取过poi列表的标志位*/
    private boolean mHasGetPoiList = false;
    /** 定位监听器 */
    private BDLocationListener mLocListener = new MyLocationListener();
    
    /*--------------------------
     * public方法
     *-------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_base_poi, menu);
        MenuItem item = menu.findItem(R.id.item_search);
        SearchView search = (SearchView)item.getActionView();
        search.setIconifiedByDefault(true);
        search.setOnQueryTextListener(new OnQueryTextListener() {
            
            @Override
            public boolean onQueryTextSubmit(String query) {
                KLog.d(TAG, "onQueryTextSubmit : %s", query);
                searchPoi(query);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                KLog.d(TAG, "onQueryTextChange : %s", newText);
                return true;
            }
        });
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        KLog.d(TAG, "currentapiVersion : %s", currentapiVersion);
        if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // 4.0以上采用ActionExpandListener
            item.setOnActionExpandListener(new OnActionExpandListener() {

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    //搜索栏关闭
                    KLog.d(TAG, "onMenuItemActionCollapse " + item.getItemId());
                    closeSearch();
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    //搜索栏展开
                    KLog.d(TAG, "onMenuItemActionExpand " + item.getItemId());
                    return true;
                }
            });
        } else {
            // 4.0以下采用onCloseListenerS
            search.setOnCloseListener(new OnCloseListener() {

                @Override
                public boolean onClose() {
                    KLog.d(TAG, "mSearchView on close ");
                    closeSearch();
                    return false;
                }
            });
        }
        
        return true;
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //处理Intent信息
        Intent it = getIntent();
        //若启动时指定分类，则使用指定的分类
        String defaultCategory = it.getStringExtra(INTENT_EXTRA_DEFAULT_CATEGORY);
        if(!TextUtils.isEmpty(defaultCategory)) {
            mDefaultCategory = defaultCategory;
        }
        //若没有设置默认分类，则使用固定值
        if(TextUtils.isEmpty(mDefaultCategory)) {
            mDefaultCategory = PoiCategory.DEFAULT_POI_CATEGORY;
        }
        
        //底部加载提示
        mLoadView = getLayoutInflater().inflate(R.layout.footer_load, null);
        
        //设置actionbar
        mActionBar.setDisplayShowTitleEnabled(false);
        
        //设置下拉菜单数据
        ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(this, R.layout.list_spinner_poi_category);
        for(PoiCategory c : PoiCategory.mCategorys) {
            arrAdapter.add(c.name);
        }
        SpinnerAdapter adapter = arrAdapter;
        // 将ActionBar的操作模型设置为NAVIGATION_MODE_LIST
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // 为ActionBar设置下拉菜单和监听器
        mActionBar.setListNavigationCallbacks(adapter, new SpinnerSelectListener());
        //选择默认选中的项目
        int pos = PoiCategory.getPositionById(mDefaultCategory);
        if(pos >= 0) {
            mActionBar.setSelectedNavigationItem(pos);
        }
        
        //微博相关
        if(mToken != null) {
            mPlaceApi = new PlaceAPI(mToken);
        } else {
            if(mToken == null) {
                SmartToast.showShortToast(this, R.string.hint_auth_invalid, false);
            }
        }
    }
    
    @Override
    protected void onStart() {
        KLog.d(TAG, "onStart");

        //注册定位监听器
        mApp.getLocationClient().registerLocationListener(mLocListener);
        
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        KLog.d(TAG, "onStop");
        
        //取消注册定位监听器
        mApp.getLocationClient().unRegisterLocationListener(mLocListener);
        
        super.onStop();
    }
    
    /**
     * 提供给子类的onScrollStateChanged调用
     * @param view
     * @param scrollState
     */
    protected void scrollStateChanged(int scrollState) {
        //KLog.d(TAG, "scrollState = " + scrollState);
        //下拉到空闲是，且最后一个item的数等于数据的总数时，进行更新
        if(mLastItem == mCount  && scrollState == SCROLL_STATE_IDLE) {
            if(mPoiList == null || mPoiList.hasMore()) {
                getNextNearbyPois();
            } else {
                SmartToast.showLongToast(this, R.string.hint_no_more, true);
            }
        }
    }

    /**
     * 提供给子类的onScroll调用
     * @param view
     * @param firstVisibleItem
     * @param visibleItemCount
     * @param totalItemCount
     */
    protected void scroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //KLog.d(TAG, "firstVisibleItem = %d , visibleItemCount = %d , totalItemCount = %d",
        //             firstVisibleItem, visibleItemCount, totalItemCount);
        
        mLastItem = firstVisibleItem + visibleItemCount - 1;  //减1是因为上面加了个addFooterView
    }
    
    /**
     * 关闭搜索
     */
    protected void closeSearch() {
        if(!TextUtils.isEmpty(mPoiQuery)) {
            //若之前进行过搜索，则在搜索框关闭时重新查询一下签到信息
            mPoiQuery = "";
            selectCategory(mLastCategory);
        } else {
            //如果之前没有进行过搜索则直接关闭搜索框
            mPoiQuery = "";
        }
    }
    
    /**
     * 搜索Poi
     * @param query
     */
    protected void searchPoi(String query) {
        mPoiQuery = query;
        selectCategory(PoiCategory.ALL_CATEGORY);
    }
    
    /**
     * 选择分类
     */
    protected void selectCategory(String category) {
        if(category != null) {
            mLastCategory = mCurrentCategory;   //保存分类
            mCurrentCategory = category;        //设置分类
            mCurrPoiPage = 0;                   //复位当前页码
            if(mPoiList != null) {
                mPoiList.clear();               //清空列表
            }
            
            //子类可能要做一些响应的处理
            onCategorySelected(category);
            
            //显示加载提示
            setLoadView(true);
            
            //加载poi信息
            getNextNearbyPois();
        }
    }
    
    /**
     * 读取附近的poi信息，每调用一次，都会尝试读取下一组poi
     */
    protected void getNextNearbyPois() {
        if(mGettingPoiList) {
            KLog.w(TAG, "Getting poi list already!");
        } else {
            Position pos = mApp.getCurrentLocation();
            if(mPlaceApi != null && pos.isValid()) {
                //若位置有效，则查询周边信息，否则等位置有效后再查询
                String lat = String.valueOf(pos.latitude);
                String lon = String.valueOf(pos.longtitude);
                int page = mCurrPoiPage + 1;        //读取下一组
                KLog.d(TAG, "getNextNearbyPois, lat : %s , lon : %s , page : %s", lat, lon, page);
                mPlaceApi.nearbyPois(lat,
                                     lon, 
                                     mPoiSearchRange,
                                     mPoiQuery,
                                     mCurrentCategory,
                                     mPoiCountToGetOneTime,
                                     page,
                                     false,
                                     new GetPoisListener(this));
                
                sendMessageToBaseHandler(MSG_SHOW_LOADING_HINT);
                mGettingPoiList = true;
                mHasGetPoiList = true;
            }
        }
    }
    
    @Override
    protected void handleBaseMessage(Message msg) {
        switch(msg.what) {
            case MSG_SHOW_LOADING_HINT: {
                setLoadView(true);
                break;
            }
            
            case MSG_DISMISS_LOADING_HINT: {
                setLoadView(false);
                break;
            }
            
            case MSG_ON_GET_POIS: {
                Object obj = msg.obj;
                if(obj instanceof List<?>) {
                    List<Poi> list = (List<Poi>)obj;
                    //微博返回有效数组才解析
                    if(mPoiList == null) {
                        mPoiList = new PoiList();
                    }
                    mPoiList.addAll(list);

                    int total = msg.arg1;
                    if(total > 0) {
                        mPoiList.setTotal(total);
                    }
                    
                    mCurrPoiPage++;
                }
                break;
            }
            
            default: {
                super.handleBaseMessage(msg);
                break;
            }
        }
    };

    /**
     * 加载完成时关闭加载提示以及设置变量（无论加载成功或失败都这么做）
     */
    protected void onLoadFinish() {
        sendMessageToBaseHandler(MSG_DISMISS_LOADING_HINT);
        mGettingPoiList = false;
    }
    
    /**
     * 设置Poi的搜索半径
     * @param range
     */
    protected void setPoiSearchRange(int range) {
        if(range < 100) {
            mPoiSearchRange = 100;
        } else if(range > WeiboDefines.MAX_POI_SEARCH_RANGE) {
            mPoiSearchRange = WeiboDefines.MAX_POI_SEARCH_RANGE;
        } else {
            mPoiSearchRange = range;
        }
    }
    
    /**
     * 设置一次读取poi的数目
     * @param count
     */
    protected void setPoiCountToGetOneTime(int count) {
        if(count < 1) {
            mPoiCountToGetOneTime = 1;
        } else if(count > WeiboDefines.MAX_POI_COUNT_TO_GET_ONE_TIME) {
            mPoiCountToGetOneTime = WeiboDefines.MAX_POI_COUNT_TO_GET_ONE_TIME;
        } else {
            mPoiCountToGetOneTime = count;
        }
    }
    /////////////////////需要子类实现的方法////////////////////////
    /**
     * 获取Poi列表成功后，会调用此回调函数，
     * 子类可以在此函数中对刚刚获取到的列表做进一步处理
     * @param list 获取到的poi列表
     */
    protected abstract void onGetPoiList(List<Poi> list);
    /**
     * 当某个分类被选择后，会回调此函数
     * @param category_id 被选择的分类的id
     */
    protected abstract void onCategorySelected(String category_id);
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 设置加载提示的显示
     * @param enable
     * @author caisenchuan
     */
    private void setLoadView(boolean enable) {
        if(mLoadView != null) {
            if(enable) {
                mLoadView.setVisibility(View.VISIBLE);
            } else {
                mLoadView.setVisibility(View.GONE);
            }
        }
    }
}
