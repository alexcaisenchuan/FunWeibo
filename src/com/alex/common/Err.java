package com.alex.common;

/**
 * 通用的错误代码类，统一工程中的错误代码
 * */
public class Err {
    /*--------------------------
     * 常量
     *-------------------------*/
    /*错误代码*/
    public final static int NO_ERR                 = 0;    //无错误
    public final static int ERR_COMMON             = -1;   //通用错误
    public final static int ERR_PASSWD             = -2;   //用户名或密码错误
    public final static int ERR_NETWORK            = -3;   //网络错误
    public final static int ERR_DATABASE           = -4;   //数据库错误
    public final static int ERR_PHONE_EXIST        = -5;   //手机号已注册
    public final static int ERR_NETWORK_TIMEOUT    = -6;   //网络超时
    public final static int ERR_USER_OFFLINE       = -7;   //用户不在线
    public final static int ERR_IO                 = -8;   //IO操作错误，例如文件读写等
    public final static int ERR_FILE_NOT_FOUND     = -9;   //找不到文件
    public final static int ERR_USER_INVALID       = -10;  //无效用户
    public final static int ERR_NOT_LOGIN          = -11;  //未登录
}

