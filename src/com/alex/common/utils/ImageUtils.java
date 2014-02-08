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

import com.alex.common.utils.FileUtils.PathType;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
    
    /**图片后缀名*/
    private static final String PIC_FILE_EXT = ".jpeg";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 调用系统应用拍照
     * @param activity 拍完照片后接收Result的Activity，您需要在此Activity中实现onActivityResult方法
     * @param requestCode 启动Activity时附带的requestCode，在onActivityResult需要用此进行判断
     * @return 若成功，返回拍摄完照片的存储路径，若失败则返回null
     * */
    public static String takePhoto(Activity activity, int requestCode) {
        String filename = null;
        
        if(activity != null) {
            Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
            
            filename = FileUtils.getUniqPath(PathType.PHOTO, PIC_FILE_EXT);
            File img = new File(filename);
            Uri imgUri = Uri.fromFile(img);
            KLog.d(TAG, "img uri : " + imgUri);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
            
            activity.startActivityForResult(i, requestCode);
        }
        
        return filename;
    }
    
    /**
     * 从相册选择照片
     * @param activity 拍完照片后接收Result的Activity，您需要在此Activity中实现onActivityResult方法
     * @param requestCode 启动Activity时附带的requestCode，在onActivityResult需要用此进行判断
     */
    public static void selectPhotoFromAlbum(Activity activity, int requestCode) {
        if(activity != null) {
            Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
            getAlbum.setType("image/*");
            activity.startActivityForResult(getAlbum, requestCode);
        } else {
            KLog.w(TAG, "activity == null!");
        }
    }

    /**
     * 将图片保存到SD卡中
     * @param type 文件路径类型，决定了要把图片存在哪个目录下
     * @param bm 图片bitmap
     * @return 若成功，返回照片的存储路径，若失败则返回null
     * */
    public static String savePicToSD(PathType type, Bitmap bm) {
        String filename = null;

        // 基本判断
        if (!FileUtils.isSDMount()) {
            return filename;
        }

        // 保存下来
        filename = FileUtils.getUniqPath(type, PIC_FILE_EXT);
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
     * private方法
     *-------------------------*/
}
