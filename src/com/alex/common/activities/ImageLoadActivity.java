package com.alex.common.activities;

import com.alex.common.utils.SmartToast;
import com.alex.common.views.ZoomImageView;
import com.alex.funweibo.R;
import com.ta.util.bitmap.TABitmapCacheWork;
import com.ta.util.bitmap.TABitmapCallBackHanlder;
import com.ta.util.bitmap.TADownloadBitmapHandler;
import com.ta.util.extend.draw.DensityUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
        }
        
        @Override
        public void onSuccess(ImageView imageView, Object data, byte[] buffer) {
            super.onSuccess(imageView, data, buffer);
            mProgress.setVisibility(View.GONE);
        }
        
        @Override
        public void onFailure(ImageView t, Object data) {
            super.onFailure(t, data);
            mProgress.setVisibility(View.GONE);
            SmartToast.showLongToast(ImageLoadActivity.this, R.string.hint_loading_img_faild, false);
        }
    }

    /*--------------------------
     * 成员变量
     *-------------------------*/
    //数据
    private String mUrl = "";
    private TABitmapCacheWork mImageFetcher = null;
    
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
        
        mImage = (ZoomImageView)findViewById(R.id.image_load);
        mProgress = (ProgressBar)findViewById(R.id.prog_load_image);
        
        handleIntent();
        initImageFetcher();
        startLoad();
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
}
