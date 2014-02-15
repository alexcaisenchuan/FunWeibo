package com.alex.funweibo.activities;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.alex.common.OnHttpRequestReturnListener;
import com.alex.common.activities.BaiduMapActivity;
import com.alex.common.activities.BaseActivity;
import com.alex.common.utils.KLog;
import com.alex.common.utils.SmartToast;
import com.alex.yaha.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.PlaceAPI;
import com.weibo.sdk.android.model.Poi;

/**
 * 地点详情页面
 * @author caisenchuan
 */
public class ActivityDetailPoi extends BaseActivity {
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = ActivityDetailPoi.class.getSimpleName();
    
    /**指定地点的poiid*/
    private static final String INTENT_POI_ID = "poi_id";
    /**指定地点的poi对象*/
    private static final String INTENT_POI_OBJ = "poi_obj";
    
    //msg what
    /**设置POI内容*/
    public static final int MSG_SET_POI_INFO = MSG_EXTEND_BASE + 1;

    /*--------------------------
     * 自定义类型
     *-------------------------*/
    private class OnPoiShow extends OnHttpRequestReturnListener {

        public OnPoiShow(BaseActivity base) {
            super(base);
        }

        @Override
        public void onComplete(String arg0) {
            try {
                Poi poi = new Poi(arg0);
                sendMessageToBaseHandler(MSG_SET_POI_INFO, 0, 0, poi);
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException", e);
                showToastOnUIThread(R.string.hint_poi_read_faild);
            }
        }
        
    }

    /*--------------------------
     * 成员变量
     *-------------------------*/
    //Views
    private ImageView mImagePlaceImg = null;
    private TextView mTextPlaceName = null;
    private TextView mTextPlaceAddress = null;
    private TextView mTextPlacePhone = null;
    private TextView mTextPlaceCategory = null;
    private TextView mTextPlaceUrl = null;
    
    //数据
    /**当前Poi*/
    private Poi mPoi = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 通过poiid打开一个地址的详情页
     */
    public static void openDetailPoi(Context context, String poiid) {
        Intent it = new Intent(context, ActivityDetailPoi.class);
        it.putExtra(INTENT_POI_ID, poiid);
        context.startActivity(it);
    }
    
    /**
     * 通过poiid打开一个地址的详情页
     */
    public static void openDetailPoi(Context context, Poi poi) {
        Intent it = new Intent(context, ActivityDetailPoi.class);
        it.putExtra(INTENT_POI_OBJ, poi);
        context.startActivity(it);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        //添加菜单项  
        MenuItem add = menu.add(0, 0, 0, getString(R.string.menu_map));
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(mPoi != null) {
                    BaiduMapActivity.openMapWithMarker(ActivityDetailPoi.this, mPoi.latitude, mPoi.longtitude, mPoi.title, mPoi.address);
                } else {
                    SmartToast.showLongToast(ActivityDetailPoi.this, R.string.hint_location_invalid, false);
                }
                return false;
            }
        });
        return true; 
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_poi);
        
        mActionBar.setTitle(R.string.title_detail);
        
        //绑定界面元素
        mImagePlaceImg = (ImageView)findViewById(R.id.img_place_img);
        mTextPlaceName = (TextView)findViewById(R.id.text_place_name);
        mTextPlaceAddress = (TextView)findViewById(R.id.text_place_address);
        mTextPlacePhone = (TextView)findViewById(R.id.text_place_phone);
        mTextPlaceCategory = (TextView)findViewById(R.id.text_place_category);
        mTextPlaceUrl = (TextView)findViewById(R.id.text_place_url);
        
        //处理Intent
        handleIntent();
    }

    @Override
    protected void handleBaseMessage(Message msg) {
        switch(msg.what) {
            case MSG_SET_POI_INFO:
                if(msg.obj instanceof Poi) {
                    Poi poi = (Poi)msg.obj;
                    setTextView(mTextPlaceName, poi.title);
                    setTextView(mTextPlaceAddress, poi.address);
                    setTextView(mTextPlacePhone, poi.phone);
                    setTextView(mTextPlaceCategory, poi.category_name);
                    setTextView(mTextPlaceUrl, poi.url);
                    if(!TextUtils.isEmpty(poi.poi_pic)) {
                        //mApp.getImageFetcher().loadFormCache(poi.poi_pic, mImagePlaceImg);
                        ImageLoader.getInstance().displayImage(poi.poi_pic, mImagePlaceImg);
                    }
                    mPoi = poi;
                }
                break;
            
            default:
                super.handleBaseMessage(msg);
                break;
        }
    }
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 处理Intent
     */
    private void handleIntent() {
        Intent it = getIntent();
        Object obj = it.getSerializableExtra(INTENT_POI_OBJ);
        if(obj instanceof Poi) {
            //如果传入POI对象，则直接使用之
            Poi poi = (Poi)obj;
            sendMessageToBaseHandler(MSG_SET_POI_INFO, 0, 0, poi);
        } else {
            //判断是否传入POIID，如果有，则使用其查询信息
            String poiid = it.getStringExtra(INTENT_POI_ID);
            if(!TextUtils.isEmpty(poiid)) {
                Oauth2AccessToken token = mApp.getAccessToken();
                PlaceAPI place = new PlaceAPI(token);
                place.poisShow(poiid, false, new OnPoiShow(this));
            } else {
                SmartToast.showLongToast(this, R.string.hint_location_invalid, false);
            }
        }
    }
    
    /**
     * 设置TextView的文字
     * @param view
     * @param text
     */
    private void setTextView(TextView view, String text) {
        if(TextUtils.isEmpty(text) || text.equals("null")) {
            view.setText(R.string.text_place_none);
        } else {
            view.setText(text);
        }
    }
}
