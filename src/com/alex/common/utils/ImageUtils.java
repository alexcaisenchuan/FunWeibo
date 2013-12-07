/**
 * <p>Title: Photo.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.common.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

/**
 * 关于图片的一些基本操作
 * @author caisenchuan
 *
 */
public class ImageUtils {
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = ImageUtils.class.getSimpleName();
    
    /**应用的基础文件在SD卡上的路径*/
    public static final String APP_DIR = "/wemap/";
    /**图片存储路径*/
    public static final String PIC_DIR = APP_DIR + "pic/";
    /**图片后缀名*/
    public static final String PIC_FILE_EXT = ".jpeg";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 调用系统应用拍照
     * @param activity 拍完照片后接收Result的Activity，您需要在此Activity中实现onActivityResult方法
     * @return 若成功，返回拍摄完照片的存储路径，若失败则返回null
     * */
    public static String takePhoto(Activity activity, int requestCode) {
        String filename = null;
        
        if(activity != null) {
            Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
            
            filename = getPicPath();
            File img = new File(filename);
            Uri imgUri = Uri.fromFile(img);
            KLog.d(TAG, "img uri : " + imgUri);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
            
            activity.startActivityForResult(i, requestCode);
        }
        
        return filename;
    }

    /**
     * 将图片保存到SD卡中
     * @param bm
     * @return 若成功，返回照片的存储路径，若失败则返回null
     * */
    public static String savePicToSD(Bitmap bm) {
        String filename = null;

        // 基本判断
        if (!isSDMount()) {
            return filename;
        }

        // 保存下来
        filename = getPicPath();
        File mPhoto = new File(filename);

        // 保存图片
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(mPhoto));

            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            bos.flush();

            bos.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            filename = null;
        } catch (IOException e) {
            e.printStackTrace();
            filename = null;
        }

        return filename;
    }

    /**
     * 判断SD卡是否挂载
     * */
    public static boolean isSDMount() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取基础路径
     * */
    public static String getDir() {
        File sd = Environment.getExternalStorageDirectory();
        String dir = sd.getPath() + PIC_DIR;
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        return dir;
    }

    /**
     * 获取图片存储路径
     * */
    public static String getPicPath() {
        String picDir = getDir();

        GregorianCalendar calendar = new GregorianCalendar();
        String date_s = String.format("%d%02d%02d_%02d%02d%02d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR), 
                calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        
        String filename = picDir + date_s + PIC_FILE_EXT;
        File file = new File(filename);
        int i = 1;

        // 构造不重名的文件名
        while (file.exists()) {
            filename = picDir + date_s + "(" + i + ")" + PIC_FILE_EXT;
            file = new File(filename);
            i++;
        }

        KLog.d(TAG, "filename : " + filename);

        return filename;
    }
    
    /**
     * 读取图片文件，进行一些压缩
     * @param filePath 图片路径
     * @param width 最大宽度
     * @param height 最大高度
     * @return
     */
    public static Bitmap createNewBitmapAndCompressByFile(String filePath, int width, int height) {
        int offset = 100;
        File file = new File(filePath);
        long fileSize = file.length();
        if (200 * 1024 < fileSize && fileSize <= 1024 * 1024) {
            offset = 90;
        } else if (1024 * 1024 < fileSize) {
            offset = 85;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 为true里只读图片的信息，如果长宽，返回的bitmap为null
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inDither = false;
        /**
         * 计算图片尺寸
         * TODO 按比例缩放尺寸
         */
        BitmapFactory.decodeFile(filePath, options);

        int bmpheight = options.outHeight;
        int bmpWidth = options.outWidth;
        int inSampleSize = bmpheight / height > bmpWidth / width ? 
                           bmpheight / height : bmpWidth / width;
        //if(bmpheight / wh[1] < bmpWidth / wh[0]) 
        //    inSampleSize = inSampleSize * 2 / 3;
        //TODO 如果图片太宽而高度太小，则压缩比例太大。所以乘以2/3
        if (inSampleSize > 1) {
            options.inSampleSize = inSampleSize;// 设置缩放比例
        }
        options.inJustDecodeBounds = false;

        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(is, null, options);
        } catch (OutOfMemoryError e) {
            System.gc();
            bitmap = null;
        }
        if (offset == 100) {
            return bitmap;// 缩小质量
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, offset, baos);
        byte[] buffer = baos.toByteArray();
        options = null;
        if (buffer.length >= fileSize) {
            return bitmap;
        }
        
        return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
    }
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/
}
