package com.codingapi.android.common;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import com.baichang.android.config.ConfigurationImpl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 更新版本提示 下载
 *
 * @author Genng
 * @since 2013.10.28
 */
public class BCUpdateManager {

    private static Context mContext;

    //是否强制升级
    private boolean mCoerce;

    //提示语
    private String updateMsg = "检查到更新版本，是否更新？";

    //返回的安装包url
    private String apkUrl = "";

    /* 下载包安装路径 */
    private static final String savePath = BCFolderUtil.getUpdate(mContext);

    private static final String saveFileName = savePath + "/update.apk";

    private ProgressDialog dialog;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;


    private static String message = "";

    private int progress;

    private boolean interceptFlag = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    dialog.setProgress(progress);
                    break;
                case DOWN_OVER:
                    dialog.dismiss();
                    installApk();
                    break;
                default:
                    break;
            }
        }

    };

    public BCUpdateManager(Context context, String apkUrl, String updateMsg) {
        mContext = context;
        this.apkUrl = apkUrl;
        this.updateMsg = updateMsg;
        message = updateMsg;
        this.mCoerce = false;
    }

    public BCUpdateManager(Context context, String apkUrl, String updateMsg, boolean coerce) {
        mContext = context;
        this.apkUrl = apkUrl;
        this.updateMsg = updateMsg;
        message = updateMsg;
        this.mCoerce = coerce;
    }

    //外部接口让主Activity调用
    public void checkUpdateInfo(int colorRes) {
        showNoticeDialog(colorRes);
    }

    //外部接口让主Activity调用
    public void checkUpdateInfo() {
        showNoticeDialog(ConfigurationImpl.get().getAppBarColor());
    }

    @SuppressLint("NewApi")
    private void showNoticeDialog(final int colorRes) {
        Builder builder = null;
        builder = new Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle("提示");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("是", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog(colorRes);
            }
        });
        builder.setNegativeButton("否", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //强制升级
                if (mCoerce) {
                    AppManager.getAppManager().AppExit(mContext);
                } else {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog noticeDialog = builder.create();
        noticeDialog.show();
        setDialogTitleColor(noticeDialog, colorRes);
    }

    /**
     * 设置Dialog的颜色
     *
     * @param dialog Dialog
     * @param color  颜色
     */
    private static void setDialogTitleColor(Dialog dialog, int color) {
        try {
            Context context = dialog.getContext();
            int dividerId = context.getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(context.getResources().getColor(color));

            int alertTitleId = context.getResources().getIdentifier("alertTitle", "id", "android");
            TextView alertTitle = (TextView) dialog.findViewById(alertTitleId);
            alertTitle.setTextColor(context.getResources().getColor(color));
        } catch (Exception ignored) {

        }
    }

    public void setMsg(String msg) {
        BCUpdateManager.message = msg;
    }

    private void showDownloadDialog(int colorRes) {

        dialog = new ProgressDialog(mContext, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setTitle("版本更新");
        dialog.setMessage(message);
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                interceptFlag = true;
            }
        });
        dialog.show();
        dialog.setCancelable(false);
        setDialogTitleColor(dialog, colorRes);
        downloadApk();
    }

    private Runnable mDownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdir();
                }
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numRead = is.read(buf);
                    count += numRead;
                    progress = (int) (((float) count / length) * 100);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numRead <= 0) {
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numRead);
                } while (!interceptFlag);//点击取消就停止下载.

                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    /**
     * 下载apk
     */

    private void downloadApk() {
        Thread downLoadThread = new Thread(mDownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     */
    private void installApk() {
        File apkFile = new File(saveFileName);
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }
}
