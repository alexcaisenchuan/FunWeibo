/**
 * <p>Title: BaseActivity.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.common;

import com.alex.funweibo.AppControl;
import com.alex.common.utils.SmartToast;
import com.alex.common.utils.KLog;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

/**
 * 此应用中Activity共同的基础类
 * @author caisenchuan
 *
 */
public abstract class BaseActivity extends FragmentActivity {
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG_BASE = "BaseActivity";
    ///////////////////mHandler msg.what//////////////
    /**显示Toast，内容在obj里*/
    protected static final int MSG_SHOW_TOAST = 100;
    /**子类的消息what从指定数字往上扩展*/
    protected static final int MSG_EXTEND_BASE = 200;
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**应用信息对象*/
    protected AppControl mApp = null;
    /**Handler对象*/
    protected Handler mBaseHandler = null;
    /**ActionBar*/
    protected ActionBar mActionBar = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 当设备配置信息有改动（比如屏幕方向的改变，实体键盘的推开或合上等）时，
     * 并且如果此时有activity正在运行，系统会调用这个函数。
     * 注意：onConfigurationChanged只会监测应用程序在AnroidMainifest.xml中通过
     * android:configChanges="xxxx"指定的配置类型的改动；
     * 而对于其他配置的更改，则系统会onDestroy()当前Activity，然后重启一个新的Activity实例。
     * 
     * @param newConfig 新的设备配置
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 检测屏幕的方向：纵向或横向
        if (this.getResources().getConfiguration().orientation 
                == Configuration.ORIENTATION_LANDSCAPE) {
            //当前为横屏， 在此处添加额外的处理代码
            KLog.d(TAG_BASE, "landscapte");
        }
        else if (this.getResources().getConfiguration().orientation 
                == Configuration.ORIENTATION_PORTRAIT) {
            //当前为竖屏， 在此处添加额外的处理代码
        }
        //检测实体键盘的状态：推出或者合上    
        if (newConfig.hardKeyboardHidden 
                == Configuration.HARDKEYBOARDHIDDEN_NO){ 
            //实体键盘处于推出状态，在此处添加额外的处理代码
        } 
        else if (newConfig.hardKeyboardHidden
                == Configuration.HARDKEYBOARDHIDDEN_YES){ 
            //实体键盘处于合上状态，在此处添加额外的处理代码
        }
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar = getActionBar();
        
        //默认打开ActionBar的返回键
        if(mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        //读取应用信息
        mApp = (AppControl)getApplication();
        //创建通用Handler
        mBaseHandler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.what == MSG_SHOW_TOAST) {
                    //在主线程显示Toast
                    String text = (String)msg.obj;
                    SmartToast.showLongToast(BaseActivity.this, text, true);
                } else if(msg.what > MSG_EXTEND_BASE) {
                    //子类的处理函数
                    handleBaseMessage(msg);
                } else {
                    //其他的交给父类处理
                    super.handleMessage(msg);
                }
            };
        };
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        KLog.d(TAG_BASE, "onOptionsItemSelected , %s , %s", 
                          item.getItemId(), item.getTitle());

        boolean ret = false;
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            
            default:
                ret = super.onOptionsItemSelected(item);
                break;
        }
        
        return ret;
    }
    
    /**
     * 在主线程显示Toast
     * @param id
     * @author caisenchuan
     */
    public void showToastOnUIThread(int id) {
        String str = getApplicationContext().getString(id);
        showToastOnUIThread(str);
    }
    
    /**
     * 在主线程显示Toast
     * @author caisenchuan
     * @param text
     */
    public void showToastOnUIThread(String text) {
        if(mBaseHandler != null) {
            Message msg = new Message();
            msg.what = MSG_SHOW_TOAST;
            msg.obj = text;
            mBaseHandler.sendMessage(msg);
        }
    }
    
    /**
     * 往本Acitvity的UIHandler发消息
     * @author caisenchuan
     * @param type
     */
    protected void sendMessageToBaseHandler(int what) {
        if(mBaseHandler != null) {
            Message msg = new Message();
            msg.what = what;
            mBaseHandler.sendMessage(msg);
        }
    }
    
    /**
     * 往本Acitvity的UIHandler发消息
     * @param what
     * @param arg1
     * @param arg2
     * @param obj
     * @author caisenchuan
     */
    protected void sendMessageToBaseHandler(int what, int arg1, int arg2, Object obj) {
        if(mBaseHandler != null) {
            Message msg = new Message();
            msg.what = what;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.obj  = obj;
            mBaseHandler.sendMessage(msg);
        }
    }
    
    /**
     * 处理BaseHandler的消息
     * @param msg
     * @author caisenchuan
     */
    protected void handleBaseMessage(Message msg) {
        // TODO Auto-generated method stub
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/

}
