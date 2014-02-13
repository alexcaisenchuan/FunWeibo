package com.alex.common.activities;

import com.alex.common.utils.ImageUtils;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.FileUtils.PathType;
import com.alex.common.views.ZoomImageView;
import com.alex.funweibo.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
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

    /*--------------------------
     * 成员变量
     *-------------------------*/
    //数据
    /**图片网址*/
    private String mUrl = "";
    /**图片缓存*/
    private Bitmap mImageBuffer = null;
    
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
     * 开始加载图片
     */
    private void startLoad() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheOnDisc(true)
        .cacheInMemory(true)
        .displayer(new FadeInBitmapDisplayer(50))
        .bitmapConfig(Bitmap.Config.RGB_565)
        .imageScaleType(ImageScaleType.EXACTLY)
        .build();
        
        ImageLoadingListener listener = new ImageLoadingListener() {
            
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgress.setVisibility(View.VISIBLE);
                mImageBuffer = null;
            }
            
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgress.setVisibility(View.GONE);
                SmartToast.showLongToast(ImageLoadActivity.this, R.string.hint_loading_img_faild, false);
                mImageBuffer = null;
            }
            
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgress.setVisibility(View.GONE);
                mImageBuffer = loadedImage;
            }
            
            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgress.setVisibility(View.GONE);
                mImageBuffer = null;
            }
        };
        
        ImageLoader.getInstance().displayImage(mUrl, mImage, defaultOptions, listener);
    }
    
    /**
     * 保存图片到SD卡
     */
    private void saveImage() {
        if(mImageBuffer == null) {
            showToastOnUIThread(R.string.hint_loading_img_not_finish);
        } else {
            Bitmap bitmap = mImageBuffer;
            String path = ImageUtils.savePicToSD(PathType.DOWNLOAD, bitmap);
            showToastOnUIThread(String.format("%s%s", getString(R.string.hint_photo_saved), path));
        }
    }
}
