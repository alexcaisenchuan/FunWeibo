package com.alex.common.activities;

import com.alex.common.utils.ImageUtils;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.FileUtils.PathType;
import com.alex.common.views.ZoomImageView;
import com.alex.funweibo.R;
import com.ta.util.bitmap.TABitmapCacheWork;
import com.ta.util.bitmap.TABitmapCallBackHanlder;
import com.ta.util.bitmap.TADownloadBitmapHandler;
import com.ta.util.extend.draw.DensityUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * 加载图片使用的Activity，类似于微博大图查看的功能，包括进度条提示以及其他相关功能
 * @author caisenchuan
 */
public class ImageLoadActivity extends BaseActivity{
    /*--------------------------
     * 常量
     *-------------------------*/
    public static final String TAG = ImageLoadActivity.class.getSimpleName();
    
    //Intent extra
    /**设置图片地址*/
    public static final String INTENT_IMG_URL = "img_url";
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    private class ImageFetchCallBack extends TABitmapCallBackHanlder {
        @Override
        public void onStart(ImageView t, Object data) {
            super.onStart(t, data);
            mProgress.setVisibility(View.VISIBLE);
            mImageBuffer = null;
        }
        
        @Override
        public void onSuccess(ImageView imageView, Object data, byte[] buffer) {
            super.onSuccess(imageView, data, buffer);
            mProgress.setVisibility(View.GONE);
            mImageBuffer = buffer;
        }
        
        @Override
        public void onFailure(ImageView t, Object data) {
            super.onFailure(t, data);
            mProgress.setVisibility(View.GONE);
            SmartToast.showLongToast(ImageLoadActivity.this, R.string.hint_loading_img_faild, false);
            mImageBuffer = null;
        }
    }

    /*--------------------------
     * 成员变量
     *-------------------------*/
    //数据
    /**图片网址*/
    private String mUrl = "";
    /**图片加载器*/
    private TABitmapCacheWork mImageFetcher = null;
    /**图片缓存*/
    private byte[] mImageBuffer = null;
    
    //界面元素
    private ZoomImageView mImage = null;
    private ProgressBar mProgress = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 启动此Activity，加载图片
     */
    public static void loadImage(Context context, String url) {
        Intent it = new Intent(context, ImageLoadActivity.class);
        it.putExtra(INTENT_IMG_URL, url);
        context.startActivity(it);
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);
        
        //设置ActionBar
        mActionBar.setTitle(R.string.title_full_image);
        
        mImage = (ZoomImageView)findViewById(R.id.image_load);
        mProgress = (ProgressBar)findViewById(R.id.prog_load_image);
        
        handleIntent();
        initImageFetcher();
        startLoad();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        //添加菜单项  
        MenuItem add = menu.add(0, 0, 0, R.string.menu_save);
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveImage();
                return false;
            }
        });
        return true; 
    }
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 处理Intent
     */
    private void handleIntent() {
        Intent it = getIntent();
        String url = it.getStringExtra(INTENT_IMG_URL);
        if(!TextUtils.isEmpty(url)) {
            mUrl = url;
        }
    }

    /**
     * 初始化图片加载器
     */
    private void initImageFetcher() {
        TADownloadBitmapHandler f = new TADownloadBitmapHandler(this,
                DensityUtils.dipTopx(this, 256),
                DensityUtils.dipTopx(this, 256));
        ImageFetchCallBack callback = new ImageFetchCallBack();
        callback.setLoadingImage(this, R.drawable.empty_photo);
        mImageFetcher = new TABitmapCacheWork(this);
        mImageFetcher.setProcessDataHandler(f);
        mImageFetcher.setCallBackHandler(callback);
        mImageFetcher.setFileCache(mApp.getFileCache());
    }
    
    /**
     * 开始加载图片
     */
    private void startLoad() {
        mImageFetcher.loadFormCache(mUrl, mImage);
    }
    
    /**
     * 保存图片到SD卡
     */
    private void saveImage() {
        if(mImageBuffer == null) {
            showToastOnUIThread(R.string.hint_loading_img_not_finish);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(mImageBuffer, 0, mImageBuffer.length);
            String path = ImageUtils.savePicToSD(PathType.DOWNLOAD, bitmap);
            showToastOnUIThread(String.format("%s%s", getString(R.string.hint_photo_saved), path));
        }
    }
}
