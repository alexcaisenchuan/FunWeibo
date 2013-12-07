/**
 * <p>Title: ActivityNewWeibo.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.funweibo.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import com.alex.common.BaseActivity;
import com.alex.funweibo.R;
import com.alex.funweibo.model.Position;
import com.alex.common.utils.OnHttpRequestReturnListener;
import com.alex.common.utils.ImageUtils;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.KLog;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboDefines;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.PlaceAPI;
import com.weibo.sdk.android.model.Status;
import com.weibo.sdk.android.model.Status.ExtraParams;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 发送一条新微博
 * @author caisenchuan
 *
 */
public class ActivityNewWeibo extends BaseActivity implements OnClickListener{
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 发送微博的回调函数
     * @author caisenchuan
     *
     */
    private class AddWeiboListener extends OnHttpRequestReturnListener {

        /**发送时使用的临时微博*/
        private Status mTempStatus;
        
        public AddWeiboListener(BaseActivity base, Status tempStatus) {
            super(base);
            this.mTempStatus = tempStatus;
            KLog.d(TAG, "tempId : %s", tempStatus.getExtraParams().getTempId());
        }

        @Override
        public void onComplete(String arg0) {
            try {
                //KLog.d(TAG, arg0);
                
                //读取微博
                Status status = new Status(arg0);
                ExtraParams p = status.getExtraParams();
                p.setTempId(mTempStatus.getExtraParams().getTempId());       //设置对应的临时微博id
                
                if(status != null) {
                    showToastOnUIThread(getString(R.string.hint_checkin_success));
                    
                    //发出广播
                    sendBroadCast(BROADCAST_ACTION_NEW_WEIBO_SUCCESS, status);
                    
                    //删除照片
                    deletePhoto();
                }
            } catch (Exception e) {
                KLog.w(TAG, "Exception while build status", e);
                showToastOnUIThread(getString(R.string.hint_add_weibo_faild) + e.toString());
                sendBroadCast(BROADCAST_ACTION_NEW_WEIBO_FAILD, mTempStatus);      //发出广播
            }
        }
        
        @Override
        public void onComplete4binary(ByteArrayOutputStream arg0) {
            try {
                super.onComplete4binary(arg0);
            } finally {
                sendBroadCast(BROADCAST_ACTION_NEW_WEIBO_FAILD, mTempStatus);      //发出广播
            }
        }
        
        @Override
        public void onError(WeiboException e) {
            try {
                super.onError(e);
            } finally {
                sendBroadCast(BROADCAST_ACTION_NEW_WEIBO_FAILD, mTempStatus);      //发出广播
            }
        }
        
        @Override
        public void onIOException(IOException e) {
            try {
                super.onIOException(e);
            } finally {
                sendBroadCast(BROADCAST_ACTION_NEW_WEIBO_FAILD, mTempStatus);      //发出广播
            }
        }
    }
    
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "ActivityNewWeibo";
    
    ///////////////startActivityForResult参数/////////////////
    /**拍照*/
    public static final int REQUEST_CODE_TAKE_PHOTO = 1;
    /**选择签到地点*/
    public static final int REQUEST_CODE_SELECT_POI = 2;
    
    ////////////////Intent 参数///////////////////
    /**启动时是否要启动拍照*/
    public static final String INTENT_EXTRA_TAKE_PHOTO  = "take_photo";
    /**poi id*/
    public static final String INTENT_EXTRA_POI_ID      = "poi_id";
    /**poi名字*/
    public static final String INTENT_EXTRA_POI_TITLE   = "poi_title";
    /**序列化方式传递一条微博的信息*/
    public static final String INTENT_EXTRA_WEIBO_STATUS_OBJ = "weibo_status_obj";
    
    ////////////////Broadcast参数///////////////////
    /**发布新微博*/
    public static final String BROADCAST_ACTION_NEW_WEIBO_SEND     = "com.alex.funweibo.action.new_weibo_send";
    /**发布新微博成功*/
    public static final String BROADCAST_ACTION_NEW_WEIBO_SUCCESS  = "com.alex.funweibo.action.new_weibo_success";
    /**发布新微博失败*/
    public static final String BROADCAST_ACTION_NEW_WEIBO_FAILD    = "com.alex.funweibo.action.new_weibo_faild";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**最近一张照片的存储路径*/
    private String mLastPicPath = "";
    /**poi id*/
    private String mPoiid = "";
    /**poi 名字*/
    private String mPoiTitle = "";
    
    /////////////////Views///////////////////////
    private EditText mEditNewWeiboContent = null;
    private TextView mTextLocation = null;
    private ImageView mImageWeiboPic = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.text_location:
                //定位按钮
                Intent it = new Intent(ActivityNewWeibo.this, ActivityPoiSelect.class);
                startActivityForResult(it, REQUEST_CODE_SELECT_POI);
                break;
                
            case R.id.img_weibo_pic:
                //微博图片
                if(!photoValid()) {
                    //没有图片时启动拍照
                    takePhoto();
                }
                break;
            
            default:
                break;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        //添加菜单项  
        MenuItem add=menu.add(0,0,0,"发送");
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                checkAndPost();
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
        setContentView(R.layout.activity_new_weibo);
        
        //若调用者指定，则启动拍照
        Intent it = getIntent();
        boolean shouldTakePhoto = it.getBooleanExtra(INTENT_EXTRA_TAKE_PHOTO, false);
        if(shouldTakePhoto) {
            takePhoto();
        }
        
        //设置界面元素
        mEditNewWeiboContent = (EditText)findViewById(R.id.edit_new_weibo_content);
        mTextLocation = (TextView)findViewById(R.id.text_location);
        mImageWeiboPic = (ImageView)findViewById(R.id.img_weibo_pic);
        
        mTextLocation.setOnClickListener(this);
        mImageWeiboPic.setOnClickListener(this);
        
        setPoiInfoWithIntent(getIntent());      //若传进来的intent有poi信息，则使用之
        //setPostionDisplay();                  //显示当前地址
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch(requestCode) {
            case REQUEST_CODE_TAKE_PHOTO: {
                if(Activity.RESULT_OK == resultCode) {
                    File file = new File(mLastPicPath);
                    if(file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        mImageWeiboPic.setImageURI(uri);
                    }
                    
                    SmartToast.showLongToast(this, 
                                             String.format("%s%s", getString(R.string.hint_photo_saved), mLastPicPath),
                                             true);
                }
                break;
            }
            
            case REQUEST_CODE_SELECT_POI: {
                if(Activity.RESULT_OK == resultCode) {
                    setPoiInfoWithIntent(data);
                }
                break;
            }
        }
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 检查各输入的有效性，并且发布微博
     */
    private void checkAndPost() {
        String weiboContent = mEditNewWeiboContent.getText().toString();
        Oauth2AccessToken token = mApp.getAccessToken();
        boolean fileValid = photoValid();
        String content = TextUtils.isEmpty(weiboContent) ? getString(R.string.text_share_photo) : weiboContent;
        
        if(TextUtils.isEmpty(mPoiid)) {
            //没有选择位置
            SmartToast.showLongToast(this, R.string.hint_pls_select_poi, true);
        } else if(!fileValid) {
            //没有放图片
            SmartToast.showLongToast(this, R.string.hint_pls_attach_photo, true);
        } else if(content.length() > WeiboDefines.MAX_STATUS_CONTENT_LENGTH) {
            //字数太多
            SmartToast.showLongToast(this, R.string.hint_word_cnt_exceed_limit, true);
        } else if(token == null){
            //授权信息无效
            SmartToast.showLongToast(this, R.string.hint_auth_invalid, true);
        } else {
            PlaceAPI place = new PlaceAPI(token);
            /*String latitude = "0.0";
            String longtitude = "0.0";
            if(mApp.getCurrentLocation().isValid()) {
                latitude = String.valueOf(mApp.getCurrentLocation().latitude);
                longtitude = String.valueOf(mApp.getCurrentLocation().longtitude);
            }*/
            
            int tempId = mApp.getNextNewWeiboTempId();
            Status tempStatus = Status.getNewWeiboTempStatus(tempId,
                                                             mPoiid,
                                                             mPoiTitle,
                                                             content,
                                                             mLastPicPath);
            
            place.poisAddCheckin(mPoiid,
                                 content,
                                 mLastPicPath,
                                 true,
                                 new AddWeiboListener(this, tempStatus));

            //发出广播，让主界面显示临时微博
            sendBroadCast(BROADCAST_ACTION_NEW_WEIBO_SEND, tempStatus);
            
            //关闭此Activity
            finish();
        }
    }
    
    /**
     * 设置界面上的位置信息显示
     */
    @SuppressWarnings("unused")
    private void setPostionDisplay() {
        Position pos = mApp.getCurrentLocation();
        if(pos.isValid()) {
            String loc = "";
            if(!TextUtils.isEmpty(pos.address)) {
                loc = pos.address;
            } else {
                loc = String.format("(%s,%s)", pos.latitude, pos.longtitude);
            }
            mTextLocation.setText(loc);
        } else {
            mTextLocation.setText(R.string.text_get_location);
        }
    }
    
    /**
     * 判断照片是否有效
     * @return
     * @author caisenchuan
     */
    private boolean photoValid() {
        boolean ret = false;
        
        if(mLastPicPath != null) {
            File file = new File(mLastPicPath);
            if(file != null && file.exists()) {
                ret = true;
            }
        }
        
        return ret;
    }
    
    /**
     * 启动拍照
     * @author caisenchuan
     */
    private void takePhoto() {
        mLastPicPath = ImageUtils.takePhoto(this, REQUEST_CODE_TAKE_PHOTO);
    }
    
    /**
     * 删除本地照片
     * @author caisenchuan
     */
    private void deletePhoto() {
        if(mLastPicPath != null) {
            File file = new File(mLastPicPath);
            if(file != null && file.exists()) {
                file.delete();
            }
            mLastPicPath = null;
        }
    }
    
    /**
     * 使用Intent中的数据设置poi信息
     * @param data
     */
    private void setPoiInfoWithIntent(Intent data) {
        if(data != null) {
            String poiid = data.getStringExtra(INTENT_EXTRA_POI_ID);
            String poi_title = data.getStringExtra(INTENT_EXTRA_POI_TITLE);
            if(!TextUtils.isEmpty(poiid)) {
                KLog.d(TAG, "poiid : %s", poiid);
                mPoiid = poiid;
                mPoiTitle = poi_title;
                mTextLocation.setText(poi_title);
            }
        }
    }
    
    /**
     * 发出广播
     * @param action
     * @param status
     */
    private void sendBroadCast(String action, Status status) {
        KLog.d(TAG, "sendBroadCast , %s", action);
        Intent it = new Intent();
        it.setAction(action);
        it.putExtra(INTENT_EXTRA_WEIBO_STATUS_OBJ, status);
        sendBroadcast(it);
    }
}
