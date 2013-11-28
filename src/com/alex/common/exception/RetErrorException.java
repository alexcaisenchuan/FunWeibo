/**
 * <p>Title: RetErrorException.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-19
 * @version 1.0
 */
package com.alex.common.exception;

/**
 * 请求操作失败
 * @author caisenchuan
 *
 */
public class RetErrorException extends Exception{

    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    /**
     * 错误提示信息
     */
    private static final String ERR_MSG = "Ret of request error";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    private String mErrCode = "";
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 请求操作失败
     */
    public RetErrorException() {
        super(ERR_MSG);
    }
    
    /**
     * 请求操作失败
     * @param err_code - 错误代码
     */
    public RetErrorException(String err_code) {
        super(String.format("%s : %s", ERR_MSG, err_code));
        this.mErrCode = err_code;
    }
    
    /**
     * 读取请求返回的错误代码
     * @author caisenchuan
     * @return 错误代码
     */
    public String getErrCode() {
        return this.mErrCode;
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
