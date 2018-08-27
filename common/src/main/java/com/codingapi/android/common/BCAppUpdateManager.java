package com.codingapi.android.common;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import com.codingapi.android.library.logger.Logger;
import java.io.File;

/**
 * Created by iscod.
 * Time:2016/11/10-16:37.
 */

public class BCAppUpdateManager {
    private static final String TAG = BCAppUpdateManager.class.getSimpleName();
    private String mFilePath;
    private Context mContext;
    //是否强制升级
    private boolean mCoerce;
    //提示语
    private String updateMsg = "检查到更新版本，是否更新？";
    //返回的安装包url
    private String apkUrl = "";
    private DownloadManager mDownloadManager;
    private long downloadID;
    private boolean isDownloadSuccess = false;
    private BCDownloadReceiver receiver;

    public BCAppUpdateManager(Context context, String apkUrl, String updateMsg) {
        this.mContext = context;
        this.apkUrl = apkUrl;
        this.updateMsg = updateMsg;
        this.mCoerce = false;
        mFilePath = context.getExternalFilesDir("update").getAbsolutePath();
        registerReceiver();
    }

    public BCAppUpdateManager(Context context, String apkUrl, String updateMsg, boolean coerce) {
        this.mContext = context;
        this.apkUrl = apkUrl;
        this.updateMsg = updateMsg;
        this.mCoerce = coerce;
        mFilePath = context.getExternalFilesDir("update").getAbsolutePath();
        registerReceiver();
    }

    private void registerReceiver() {
        receiver = new BCDownloadReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        mContext.registerReceiver(receiver, filter);
    }

    private void unregisterReceiver() {
        if (mContext != null) {
            mContext.unregisterReceiver(receiver);
        }
    }

    //外部接口让主Activity调用
    public void update() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                downloadApk();
                //强制升级
                if (mCoerce) {
                    System.exit(0);
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                //强制升级
                if (mCoerce) {
                    System.exit(0);
                } else {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 下载apk
     */
    private void downloadApk() {
        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        File file = new File(mFilePath, "update.apk");
        if (file.exists()) {
            boolean b = file.delete();
        }
        Uri uri = Uri.fromFile(file);
        request.setDestinationUri(uri);
        PackageManager manager = mContext.getPackageManager();
        try {
            ApplicationInfo info = manager.getApplicationInfo(mContext.getPackageName(), 0);
            request.setTitle(manager.getApplicationLabel(info) + "更新···");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        downloadID = mDownloadManager.enqueue(request);
    }

    /**
     * 安装apk
     */
    private void installApk() {
        try {
            if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
                Uri apkUri = FileProvider.getUriForFile(mContext,
                    mContext.getPackageName() + ".provider", new File(mFilePath, "update.apk"));
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                mContext.startActivity(install);
            } else {
                Intent install = new Intent(Intent.ACTION_VIEW);
                Uri uri = mDownloadManager.getUriForDownloadedFile(downloadID);
                install.setDataAndType(uri, "application/vnd.android.package-archive");
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(install);
            }
        } catch (Exception e) {
            Logger.e(TAG, "update error: " + e.toString());
        } finally {
            unregisterReceiver();
        }
    }

    /**
     * 下载完成广播 and 通知点击广播
     */
    public class BCDownloadReceiver extends BroadcastReceiver {

        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                isDownloadSuccess = true;
                Toast.makeText(mContext, "下载完成，若没有自动安装，请点击通知栏，进行安装", Toast.LENGTH_LONG).show();
                installApk();
            } else if (isDownloadSuccess && action.equals(
                DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                installApk();
            }
        }
    }
}
