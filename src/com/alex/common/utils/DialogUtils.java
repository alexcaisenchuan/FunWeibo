package com.alex.common.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.alex.funweibo.R;

/**
 * 一些常用的Dialog显示工具类
 * @author caisenchuan
 */
public class DialogUtils {
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
    /**
     * 显示一个带有确认，取消按钮的对话框
     * @param context 上下文
     * @param title 提示文字
     * @param ok_listener 点击确定后执行的函数
     * @author lenovo
     */
    public static void showOKCancelButtonDialog(Context context, String title, AlertDialog.OnClickListener ok_listener) {
        AlertDialog dlg = new AlertDialog.Builder(context).create();
        dlg.setTitle(title);
        dlg.setButton(Dialog.BUTTON_NEGATIVE, context.getString(R.string.button_ok), ok_listener);
        dlg.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.button_cancel), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //...
            }
        });
        dlg.show();
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
