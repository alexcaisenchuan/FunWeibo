/**
 * <p>Title: ActivityPopularPOIs.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-11-3
 * @version 1.0
 */
package com.alex.funweibo.activities;

import java.util.ArrayList;
import java.util.List;

import com.alex.common.AppConfig;
import com.alex.common.BaseActivity;
import com.alex.funweibo.R;
import com.alex.common.keep.SettingKeeper;
import com.alex.common.keep.StatusArrayKeeper;
import com.alex.common.utils.ImageUtils;
import com.alex.common.utils.OnHttpRequestReturnListener;
import com.alex.common.utils.KLog;
import com.alex.common.utils.StringUtils;
import com.alex.common.utils.WeiboUtils;
import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.internal.PLA_AbsListView;
import com.huewu.pla.lib.internal.PLA_AbsListView.OnScrollListener;
import com.huewu.pla.lib.internal.PLA_AdapterView;
import com.huewu.pla.lib.internal.PLA_AdapterView.OnItemClickListener;
import com.weibo.sdk.android.WeiboDefines;
import com.weibo.sdk.android.api.WeiboAPI.SORT2;
import com.weibo.sdk.android.model.Place;
import com.weibo.sdk.android.model.Poi;
import com.weibo.sdk.android.model.Status;
import com.weibo.sdk.android.model.WeiboException;
import com.weibo.sdk.android.model.Status.TypeSpec;
import com.weibo.sdk.android.org.json.JSONArray;
import com.weibo.sdk.android.org.json.JSONException;
import com.weibo.sdk.android.org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 热门地点微博
 * @author caisenchuan
 */
public class ActivityPopularPOIs extends BasePOIActivity implements OnScrollListener, OnClickListener {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "ActivityPopularPOIs";
    
    //测试SaveInstance用
    private static final String TEST_KEY = "TestKey";
    
    ///////////////mBaseHandler msg what//////////////
    /**刷新列表内容*/
    public static final int MSG_REFRESH_LIST = MSG_POI_ACTIVITY_BASE + 1;
    /**将微博添加到列表头*/
    public static final int MSG_ADD_STATUS_TO_FIRST = MSG_POI_ACTIVITY_BASE + 2;
    /**将添加的微博替换成发布成功的微博*/
    public static final int MSG_REPLACE_SENDING_STATUS = MSG_POI_ACTIVITY_BASE + 3;
    /**发布微博失败*/
    public static final int MSG_SENDING_STATUS_FAILD = MSG_POI_ACTIVITY_BASE + 4;
    /**显示加载的提示，用msg.arg1表示，0代表隐藏，1代表显示*/
    public static final int MSG_SET_LOADING_HINT = MSG_POI_ACTIVITY_BASE + 5;
    
    //////////////mBaseHandler msg value//////////////
    //MSG_REFRESH_LIST发出时，指定是否是第一次的列表更新
    /**不是第一次的列表更新*/
    public static final int VALUE_REFRESH_LIST_ARG1_DEFAULT    = 0;
    /**是第一次的列表更新*/
    public static final int VALUE_REFRESH_LIST_ARG1_FIRST_TIME = 1;
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 评论列表Adapter
     * @author caisenchuan
     */
    public class ListAdapter extends BaseAdapter {
        /**
         * 一个条目的ViewHolder
         */
        private class ListItemViewHolder {
            /**标题*/
            public TextView mTitle;
            /**内容*/
            public TextView mContent;
            /**图片*/
            public ImageView mPic;
            /**赞的个数*/
            public TextView mLikeNum;
            /**发送提示*/
            public TextView mSendingHint;
        }
        
        /**
         * inflater
         */
        private LayoutInflater mInflater = null;
        
        /**
         * 构造函数
         */
        public ListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            if(mStatus != null) {
                return mStatus.size();
            } else {
                return 0;
            }
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return null;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemViewHolder holder = null;
            
            if(convertView == null) {
                holder = new ListItemViewHolder();
                
                convertView = mInflater.inflate(R.layout.list_item_weibo_pics, null);
                holder.mTitle = (TextView)convertView.findViewById(R.id.waterfall_title);
                holder.mContent = (TextView)convertView.findViewById(R.id.waterfall_content);
                holder.mLikeNum = (TextView)convertView.findViewById(R.id.waterfall_like_num);
                holder.mPic = (ImageView)convertView.findViewById(R.id.waterfall_pic);
                holder.mSendingHint = (TextView)convertView.findViewById(R.id.waterfall_sending_hint);
                convertView.setTag(holder);
            } else {
                holder = (ListItemViewHolder)convertView.getTag();
            }
            
            //设置条目内容
            final Status status = getPosItem(position);
            //图片
            String url = WeiboUtils.getStatusPicUrlByNetworkStatus(ActivityPopularPOIs.this, status, mApp.getFileCache());
            holder.mPic.setImageResource(R.drawable.empty_photo);
            if(!TextUtils.isEmpty(url)) {
                if(StringUtils.isLocalUri(url)) {
                    Bitmap bm = ImageUtils.createNewBitmapAndCompressByFile(url, 300, 300);
                    holder.mPic.setImageBitmap(bm);
                } else {
                    mApp.getImageFetcher().loadFormCache(url, holder.mPic);
                }
            }
            //其他信息
            if(status != null) {
                //标题
                Place place = status.getPlace();
                if(place != null) {
                    holder.mTitle.setText(place.title);
                }
                
                //正文
                holder.mContent.setText(StringUtils.getStripContent(status.getText()));
                
                //底部提示
                TypeSpec type = status.getExtraParams().getType();
                if(type == TypeSpec.NEW_WEIBO_SENDING) {
                    //发送中
                    holder.mSendingHint.setVisibility(View.VISIBLE);
                    holder.mSendingHint.setText(R.string.text_sending_hint);
                    holder.mSendingHint.setClickable(false);
                    
                    holder.mLikeNum.setVisibility(View.GONE);
                } else if(type == TypeSpec.NEW_WEIBO_FAILD) {
                    //发送失败
                    holder.mSendingHint.setVisibility(View.VISIBLE);
                    holder.mSendingHint.setText(R.string.text_send_faild_hint);
                    holder.mSendingHint.setClickable(true);
                    holder.mSendingHint.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            showToastOnUIThread("Resend : " + status.getPlace().poiid);
                            WeiboUtils.postNewWeibo(ActivityPopularPOIs.this,
                                                    status);
                            status.getExtraParams().setType(TypeSpec.NEW_WEIBO_SENDING);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                    
                    holder.mLikeNum.setVisibility(View.GONE);
                } else {
                    //普通微博
                    holder.mSendingHint.setVisibility(View.GONE);
                    
                    holder.mLikeNum.setText(String.valueOf(status.getAttitudesCount()));
                    holder.mLikeNum.setVisibility(View.VISIBLE);
                }
            }
            
            return convertView;
        }
    }
    
    /**
     * 读取微博信息的回调函数
     * @author caisenchuan
     */
    private class GetPoiStatusesListener extends OnHttpRequestReturnListener {

        /**
         * 读取微博信息的回调函数
         * @param base 用于显示Toast的Activity对象
         */
        public GetPoiStatusesListener(BaseActivity base) {
            super(base);
        }

        /* (non-Javadoc)
         * @see com.weibo.sdk.android.net.RequestListener#onComplete(java.lang.String)
         */
        @Override
        public void onComplete(String arg0) {
            try {
                //KLog.d(TAG, "ret : " + arg0);
                KLog.d(TAG, "get poi status return");
                
                List<Status> list = Status.constructStatuses(arg0);
                if(list != null && list.size() > 0) {
                    //若本次获取的列表的分类与上次不一样，则要把之前的给清空
                    cleanStatusJsonArrIfNeeded();
                    //保存查询到的微博json，用于下次启动应用恢复
                    saveStatusToJsonArr(arg0);
                }
                sendMessageToBaseHandler(MSG_REFRESH_LIST,
                                         VALUE_REFRESH_LIST_ARG1_DEFAULT,
                                         0,
                                         list);
            } catch (com.weibo.sdk.android.org.json.JSONException e) {
                KLog.w(TAG, "Exception", e);
                showToastOnUIThread(R.string.hint_read_weibo_error);
            } catch (WeiboException e) {
                KLog.w(TAG, "Exception", e);
                showToastOnUIThread(R.string.hint_read_weibo_error);
            } finally {
                onLoadFinish(true);
            }
        }
    }
    
    /**
     * 列表的某个条目被点击时的调用
     */
    private class MyOnItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(PLA_AdapterView<?> parent, View view,
                int position, long id) {
            KLog.d(TAG, "onItemClick : %d", position);
            Status status = getPosItem(position);
            if(status != null) {
                if(status.getExtraParams().getType() == TypeSpec.NORMAL) {
                    openDetailWeiboActivity(status);
                }
            }
        }
    }
    
    /**
     * 广播监听
     */
    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            KLog.d(TAG, "onReceive , %s", act);
            
            if(act.equals(WeiboUtils.BROADCAST_ACTION_NEW_WEIBO_SEND)) {
                //发布新微博，将其添加到列表顶端
                Object obj = intent.getSerializableExtra(WeiboUtils.INTENT_EXTRA_WEIBO_STATUS_OBJ);
                if(obj instanceof Status) {
                    sendMessageToBaseHandler(MSG_ADD_STATUS_TO_FIRST, 0, 0, obj);
                }
            } else if(act.equals(WeiboUtils.BROADCAST_ACTION_NEW_WEIBO_SUCCESS)) {
                //发布成功，替换对应的临时微博
                Object obj = intent.getSerializableExtra(WeiboUtils.INTENT_EXTRA_WEIBO_STATUS_OBJ);
                if(obj instanceof Status) {
                    sendMessageToBaseHandler(MSG_REPLACE_SENDING_STATUS, 0, 0, obj);
                }
            } else if(act.equals(WeiboUtils.BROADCAST_ACTION_NEW_WEIBO_FAILD)) {
                //发布失败
                Object obj = intent.getSerializableExtra(WeiboUtils.INTENT_EXTRA_WEIBO_STATUS_OBJ);
                if(obj instanceof Status) {
                    sendMessageToBaseHandler(MSG_SENDING_STATUS_FAILD, 0, 0, obj);
                }
            } else {
                //其他
            }
        }
        
    }
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    ////////////////////////////Views////////////////////////
    /**listview*/
    private MultiColumnListView mListContent = null;
    /**列表的Adapter*/
    private ListAdapter mAdapter = null;
    //侧边栏相关
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    /**顶端提示框*/
    private TextView mTextHintTop;
    
    ////////////////////////////数据/////////////////////////
    /**微博列表*/
    private List<Status> mStatus = new ArrayList<Status>();
    /**用于暂存微博列表的JSON Array*/
    private JSONArray mStatusJsonArr = new JSONArray();
    
    private String[] mPlanetTitles = {"A"};
    
    ///////////////////////////标志位及计数//////////////////
    /**
     * 首次选择分类的标志位
     * */
    private boolean mFirstSelectCategory = true;
    /**
     * 重新选择POI后，清空Status列表，
     * 但是暂存的Status JSON Array不能清空，
     * 要等获取到新的Status才暂存
     * */
    private boolean mStatusJsonArrCleanFlag = false;
    /**
     * 若为true，表示当前的status列表是恢复出来的而不是来源于网络返回
     * */
    private boolean mStatusRestoreFlag = false;
    
    /////////////////////////其他///////////////////////////
    /**广播监听器*/
    private MyBroadcastReceiver mReceiver = new MyBroadcastReceiver();
    
    /*--------------------------
     * public方法
     *-------------------------*/
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            default:
                break;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        //添加菜单项  
        MenuItem add = menu.add(0, 0, 0, getString(R.string.menu_checkin));
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //拍照并创建新微博
                Intent it = new Intent(ActivityPopularPOIs.this, ActivityNewWeibo.class);
                it.putExtra(ActivityNewWeibo.INTENT_EXTRA_TAKE_PHOTO, true);
                startActivity(it);
                return false;
            }
        });
        return true; 
    }
    
    @Override
    public void onScrollStateChanged(PLA_AbsListView view, int scrollState) {
        scrollStateChanged(scrollState);
    }

    @Override
    public void onScroll(PLA_AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        scroll(firstVisibleItem, visibleItemCount, totalItemCount);
    }
    
    @Override
    public void onBackPressed() {
        //super.onBackPressed();        //默认的onBackPressed()是退出应用，这里我们希望应用只是后台
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
    

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        // Handle action buttons
        switch(item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see com.alex.wemap.activities.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDefaultCategory = SettingKeeper.readCategorySelect(this);
        
        super.onCreate(savedInstanceState);

        KLog.d(TAG, "onCreate");
        
        //测试用
        if(savedInstanceState != null) {
            int test = savedInstanceState.getInt(TEST_KEY);
            KLog.d(TAG, "TestKey : %s", test);
        }
        
        setContentView(R.layout.activity_popular_poi);
        
        //设置drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        //顶端提示框
        mTextHintTop = (TextView)findViewById(R.id.text_hint_top);
        
        //设置listview
        mListContent = (MultiColumnListView)findViewById(R.id.list_weibo_content);
        mListContent.addFooterView(mLoadView);
        mListContent.addFooterView(mReloadView);
        //设置adapter
        mAdapter = new ListAdapter(this);
        mListContent.setAdapter(mAdapter);
        //监听滑动
        mListContent.setOnScrollListener(this);
        //监听点击
        mListContent.setOnItemClickListener(new MyOnItemClickListener());
        
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(WeiboUtils.BROADCAST_ACTION_NEW_WEIBO_SEND);
        filter.addAction(WeiboUtils.BROADCAST_ACTION_NEW_WEIBO_SUCCESS);
        filter.addAction(WeiboUtils.BROADCAST_ACTION_NEW_WEIBO_FAILD);
        registerReceiver(mReceiver, filter);
        
        //尝试从SharePref中恢复数据
        tryRestoreStatusFromSharePref();
    }
    
    @Override
    protected void onStart() {
        KLog.d(TAG, "onStart");
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        KLog.d(TAG, "onStop");
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        KLog.d(TAG, "onDestroy");
        
        //取消广播监听
        unregisterReceiver(mReceiver);
        
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //此方法在Activity退到后台之前被调用，因为Activity后台后有可能会被系统回收
        
        //测试用
        KLog.d(TAG, "onSaveInstanceState");
        outState.putInt(TEST_KEY, 1);
        super.onSaveInstanceState(outState);
        
        //将微博列表保存到SharePref中
        saveJsonArrToSharePref();
    }
    
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
    @Override
    protected void handleBaseMessage(Message msg) {
        switch(msg.what) {
            case MSG_REFRESH_LIST: {
                Object obj = msg.obj;
                if(obj instanceof List<?>) {
                    if(mStatusRestoreFlag) {
                        mStatus.clear();
                        mStatusRestoreFlag = false;
                    }
                    List<Status> list = (List<Status>)obj;
                    mStatus.addAll(list);
                    mCount = mStatus.size();
                    if(mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    if(msg.arg1 == VALUE_REFRESH_LIST_ARG1_FIRST_TIME) {
                        mStatusRestoreFlag = true;
                    }
                }
                break;
            }
            
            case MSG_ADD_STATUS_TO_FIRST: {
                Object obj = msg.obj;
                if(obj instanceof Status) {
                    Status s = (Status)obj;
                    mStatus.add(0, s);
                    mCount = mStatus.size();
                    if(mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
                break;
            }
            
            case MSG_REPLACE_SENDING_STATUS: {
                Object obj = msg.obj;
                if(obj instanceof Status) {
                    Status status = (Status)obj;
                    int temp = status.getExtraParams().getTempId();
                    for(Status s : mStatus) {
                        if(s.getExtraParams().getTempId() == temp) {
                            KLog.d(TAG, "find temp id : %s", temp);
                            s.getExtraParams().setType(TypeSpec.NORMAL);
                            s.update(status);
                            break;
                        }
                    }
                    if(mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
                break;
            }
            
            case MSG_SENDING_STATUS_FAILD: {
                Object obj = msg.obj;
                if(obj instanceof Status) {
                    Status status = (Status)obj;
                    int temp = status.getExtraParams().getTempId();
                    for(Status s : mStatus) {
                        if(s.getExtraParams().getTempId() == temp) {
                            KLog.d(TAG, "find temp id : %s", temp);
                            s.getExtraParams().setType(TypeSpec.NEW_WEIBO_FAILD);
                            break;
                        }
                    }
                    if(mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
                break;
            }
            
            case MSG_SET_LOADING_HINT: {
                int show = msg.arg1;
                if(mTextHintTop != null) {
                    if(show == 1) {
                        mTextHintTop.setText(R.string.hint_loading_status);
                        mTextHintTop.setVisibility(View.VISIBLE);
                    } else {
                        mTextHintTop.setVisibility(View.GONE);
                    }
                }
                break;
            }
            
            default: {
                super.handleBaseMessage(msg);
                break;
            }
        }
    }
    
    ////////////////////实现的父类方法////////////////////////
    @Override
    protected void onGetPoiList(List<Poi> list) {
        //暂停一段时间再请求，不要做得太频繁
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            KLog.w(TAG, "Exception", e);
        }
        
        getPoiListStatuses(list);
    }

    @Override
    protected void onCategorySelected(String category_id) {
        if(!mFirstSelectCategory) {
            mStatus.clear();
        } else {
            //首次加载不能清空微博列表，因为要显示之前的
        }
        mFirstSelectCategory = false;
        //显示加载提示
        sendMessageToBaseHandler(MSG_SET_LOADING_HINT, 1, 0, null);
        //设置标志位，用于等会重置保存的JSON字符串
        mStatusJsonArrCleanFlag = true;
        //保存设置值
        SettingKeeper.keepCategorySelect(this, category_id);
        
        mAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onLoadFinish(boolean success) {
        super.onLoadFinish(success);

        //关闭加载提示
        sendMessageToBaseHandler(MSG_SET_LOADING_HINT, 0, 0, null);
    }
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 读取一组poi的对应微博
     * @param list
     */
    private void getPoiListStatuses(List<Poi> list) {
        if(list != null) {
            for(Poi poi : list) {
                getPoiStatuses(poi);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    KLog.w(TAG, "Exception", e);
                }
            }
        }
    }
    
    /**
     * 读取某个poi对应的微博
     * @param poi
     */
    private void getPoiStatuses(Poi poi) {
        if(poi != null) {
            KLog.d(TAG, "getPoiStatuses : " + poi.poiid);
            mPlaceApi.poisPhotos(poi.poiid,
                                 10,
                                 1,
                                 SORT2.SORT_BY_TIME,
                                 false,
                                 new GetPoiStatusesListener(this));
        }
    }
    
    /**
     * 读取某个位置上的项目
     */
    private Status getPosItem(int position) {
        Status ret = null;
        
        if(mStatus != null) {
            if(position >= 0 && position < mStatus.size()) {
                ret = mStatus.get(position);
            }
        }
        
        return ret;
    }
    
    /**
     * 启动某条公共墙微博的Activity
     * @param sg 要查看的微博对象
     * @author caisenchuan
     */
    private void openDetailWeiboActivity(Status s) {
        if(s != null) {
            Intent intent = new Intent(ActivityPopularPOIs.this, ActivityDetailWeibo.class);
            intent.putExtra(ActivityDetailWeibo.INTENT_EXTRA_WEIBO_STATUS_OBJ, s);
            startActivity(intent);
        }
    }

    /**
     * 若所选择的的分类已经改变，则重置JSON Array
     */
    private void cleanStatusJsonArrIfNeeded() {
        synchronized (mStatusJsonArr) {
            if(mStatusJsonArrCleanFlag) {
                mStatusJsonArr = new JSONArray();
                mStatusJsonArrCleanFlag = false;
            }
        }
    }
    
    /**
     * 将返回的微博列表暂存到JSON Array里
     * @param jsonStr
     */
    private void saveStatusToJsonArr(String jsonStr) {
        if (TextUtils.isEmpty(jsonStr) || jsonStr.equals(WeiboDefines.RET_EMPTY_ARRAY)) {
            // 返回[]则代表没有数据
            return;
        }

        synchronized (mStatusJsonArr) {
            JSONObject json;
            try {
                if(mStatusJsonArr.length() < AppConfig.MAX_STORE_STATUS_NUM) {      //不用存所有微博，只存一部分就可以了，要不然打开应用的时候会很慢
                    json = new JSONObject(jsonStr);
                    JSONArray arr = json.getJSONArray(WeiboDefines.RET_TAG_STATUSES);
                    for(int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        if(obj != null) {
                            mStatusJsonArr.put(obj);
                        }
                    }
                }
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException", e);
            }
        }

        return;
    }
    
    /**
     * 将微博列表保存到SharePref中
     */
    private void saveJsonArrToSharePref() {
        synchronized (mStatusJsonArr) {
            JSONObject json = new JSONObject();
            try {
                if(mStatusJsonArr.length() > 0) {
                    json.put(WeiboDefines.RET_TAG_STATUSES, mStatusJsonArr);
                    json.put(WeiboDefines.RET_TAG_TOTAL_NUMBER, mStatusJsonArr.length());
                    String str = json.toString();
                    //KLog.d(TAG, "saveJsonArrToSharePref, json : %s", str);
                    KLog.d(TAG, "saveJsonArrToSharePref");
                    StatusArrayKeeper.keep(this, str);
                }
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException", e);
            }
        }
    }
    
    /**
     * 尝试从SharePref中恢复上一次看的微博列表
     */
    private void tryRestoreStatusFromSharePref() {
        String str = StatusArrayKeeper.read(this);
        try {
            KLog.d(TAG, "tryRestoreStatusFromSharePref, str : %s", str);
            List<Status> list = Status.constructStatuses(str);
            sendMessageToBaseHandler(MSG_REFRESH_LIST,
                                     VALUE_REFRESH_LIST_ARG1_FIRST_TIME,
                                     0,
                                     list);
        } catch (JSONException e) {
            KLog.w(TAG, "JSONException", e);
        } catch (WeiboException e) {
            KLog.w(TAG, "WeiboException", e);
        }
    }
}
