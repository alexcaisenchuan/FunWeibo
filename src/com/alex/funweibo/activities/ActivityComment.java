package com.alex.funweibo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;

import com.alex.common.activities.BaseActivity;
import com.alex.common.utils.SmartToast;
import com.alex.yaha.R;

/**
 * 评论界面
 * @author caisenchuan
 *
 */
public class ActivityComment extends BaseActivity {
    /*--------------------------
     * 常量
     *-------------------------*/

    /*--------------------------
     * 自定义数据类型
     *-------------------------*/

    /*--------------------------
     * 属性
     *-------------------------*/
    /////////////////////界面元素//////////////////
    /**评论输入*/
    private EditText mEditComment = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        //添加菜单项  
        MenuItem add=menu.add(0, 0, 0, R.string.menu_send);
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                checkAndComment();
                return false;
            }
        });
        return true; 
    }
    
    /*--------------------------
     * protected方法
     *-------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        
        //设置ActionBar
        mActionBar.setTitle(R.string.title_comment);
        
        //绑定界面元素
        mEditComment = (EditText)findViewById(R.id.edit_comment);
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 校验、发出评论
     */
    private void checkAndComment() {
        String comment = mEditComment.getText().toString();
        if(TextUtils.isEmpty(comment)) {
            SmartToast.showLongToast(this, R.string.hint_no_any_content_in_new_weibo, true);
        } else {
            Intent it = new Intent();
            it.putExtra(ActivityDetailWeibo.INTENT_EXTRA_COMMENT_TEXT, comment);
            setResult(RESULT_OK, it);
            finish();
        }
    }
}
