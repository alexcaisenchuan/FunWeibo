package com.alex.funweibo.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.alex.common.activities.BaseActivity;
import com.alex.yaha.R;

/**
 * 关于界面
 * @author caisenchuan
 */
public class ActivityAbout extends BaseActivity {
    /*--------------------------
     * 常量
     *-------------------------*/

    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/

    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adout);
        
        mActionBar.setTitle(R.string.title_about);
        
        //设置字体
        TextView tv = (TextView)findViewById(R.id.text_title);  
        //从assert中获取有资源，获得app的assert，采用getAserts()，通过给出在assert/下面的相对路径。在实际使用中，字体库可能存在于SD卡上，可以采用createFromFile()来替代createFromAsset。   
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Structr_Regular.ttf");  
        tv.setTypeface(face);
        
        //设置网页
        WebView webView = (WebView)this.findViewById(R.id.webview);
        webView.getSettings().setSupportZoom(false);//不支持页面放大功能
        webView.loadUrl("file:///android_asset/about.html");
    }

    /*--------------------------
     * private方法
     *-------------------------*/

}
