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

import java.io.File;

import com.alex.common.BaseActivity;
import com.alex.funweibo.R;
import com.alex.funweibo.model.Position;
import com.alex.common.utils.OnHttpRequestReturnListener;
import com.alex.common.utils.PhotoTake;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.KLog;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.model.Status;
import com.weibo.sdk.android.model.WeiboException;
import com.weibo.sdk.android.org.json.JSONException;

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

        public AddWeiboListener(BaseActivity base) {
            super(base);
        }

        @Override
        public void onComplete(String arg0) {
            try {
                //读取微博
                Status status = new Status(arg0);
                
                if(status != null) {
                    showToastOnUIThread(getString(R.string.hint_add_weibo_success));
                    
                    //删除照片
                    deletePhoto();
                }
            } catch (WeiboException e) {
                KLog.w(TAG, "WeiboException while build status", e);
                showToastOnUIThread(getString(R.string.hint_add_weibo_faild) + e.toString());
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException while build status", e);
                showToastOnUIThread(getString(R.string.hint_json_parse_faild));
            }
        }
        
    }
    
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "ActivityNewWeibo";
    
    ////////////////Activity启动参数///////////////////
    /**启动时是否要启动拍照*/
    public static final String INTENT_EXTRA_TAKE_PHOTO = "take_photo";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**最近一张照片的存储路径*/
    private String mLastPicPath = "";
    
    /////////////////界面元素///////////////////////
    private EditText mEditNewWeiboContent = null;
    private TextView mTextLocation = null;
    private TextView mTextWordsLimit = null;
    private ImageView mImageWeiboPic = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.text_location:
                //定位按钮
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
        mTextWordsLimit = (TextView)findViewById(R.id.text_words_limit);
        mImageWeiboPic = (ImageView)findViewById(R.id.img_weibo_pic);
        
        mTextLocation.setOnClickListener(this);
        mImageWeiboPic.setOnClickListener(this);
        
        setPostionDisplay();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(PhotoTake.REQUEST_CODE_TAKE_PHOTO == requestCode &&
           Activity.RESULT_OK == resultCode) {
            File file = new File(mLastPicPath);
            if(file.exists()) {
                Uri uri = Uri.fromFile(file);
                mImageWeiboPic.setImageURI(uri);
            }
            
            SmartToast.showLongToast(this, 
                                     String.format("%s%s", getString(R.string.hint_photo_saved), mLastPicPath),
                                     true);
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
        
        if(TextUtils.isEmpty(weiboContent) && !fileValid) {
            //既没写字，也没有放图片
            SmartToast.showLongToast(this, R.string.hint_no_any_content_in_new_weibo, true);
        } else if(token == null){
            //授权信息无效
            SmartToast.showLongToast(this, R.string.hint_auth_invalid, true);
        } else if(!mApp.getCurrentLocation().isValid()) {
            //位置信息无效，要求定位后才能发
            SmartToast.showLongToast(this, R.string.hint_location_invalid, true);
        } else {
            StatusesAPI status = new StatusesAPI(token);
            String content = TextUtils.isEmpty(weiboContent) ? getString(R.string.text_share_photo) : weiboContent;
            String latitude = "0.0";
            String longtitude = "0.0";
            if(mApp.getCurrentLocation().isValid()) {
                latitude = String.valueOf(mApp.getCurrentLocation().latitude);
                longtitude = String.valueOf(mApp.getCurrentLocation().longtitude);
            }
            
            if(fileValid) {
                status.upload(content,
                              mLastPicPath,
                              latitude,
                              longtitude,
                              new AddWeiboListener(this));
            } else {
                status.update(content,
                              latitude,
                              longtitude,
                              new AddWeiboListener(this));
            }
            
            //关闭此Activity
            finish();
        }
    }
    
    /**
     * 设置界面上的位置信息显示
     */
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
        mLastPicPath = PhotoTake.takePhoto(this);
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
}
