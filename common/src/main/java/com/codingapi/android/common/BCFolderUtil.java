package com.codingapi.android.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import java.io.File;

/**
 * 磁盘储存地址
 *
 * @author Marcello
 */
public class BCFolderUtil {

    public static String getUpdate(Context context) {
        return getDiskCacheDir(context, "update");
    }

    public static String getImage(Context context) {
        return getDiskCacheDir(context, "image");
    }

    public static String getOther(Context context) {
        return getDiskCacheDir(context, "other");
    }

    @SuppressLint("NewApi")
    public static String getDiskCacheDir(Context context, String folderName) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return String.format("%s/%s", cachePath, folderName);
    }

    @SuppressLint("NewApi")
    public static File getDiskFile(Context context, String folderName) {
        File cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalFilesDir(folderName);
        } else {
            cachePath = context.getFileStreamPath(folderName);
        }
        return cachePath;
    }
}
