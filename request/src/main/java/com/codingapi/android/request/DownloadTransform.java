package com.codingapi.android.request;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;
import com.baichang.android.config.ConfigurationImpl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by iCong.
 * Time:2016/9/25-13:33.
 * eg:  DialogUtils.showProgressDialog(this, "正在下载···");
 * DownloadTransform.download("下载地址", file -> {
 * });
 */

public class DownloadTransform {
    //提示语
    private static final String DOWN_LOADING = "正在下载...";
    //进度条
    private static ProgressDialog sDialog;
    //成功回调
    private static HttpListener<File> sSuccessListener;

    public static void down(Observable<ResponseBody> observable,
        HttpListener<File> listener) {
        sSuccessListener = listener;
        setProgress();
        observable.subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.io())
            .subscribe(new DownloadSubscriber(null));
    }

    public static void down(String fileName, Observable<ResponseBody> observable,
        HttpListener<File> listener) {
        sSuccessListener = listener;
        setProgress();
        observable.subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.io())
            .subscribe(new DownloadSubscriber(fileName));
    }

    public static File writeResponseBodyToFile(ResponseBody body, String fileName) {
        MediaType mediaType = body.contentType();
        String type = "";
        if (mediaType != null) {
            type = mediaType.toString();
        }
        String fileSuffix = MimeTypeTool.getFileSuffix(type);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            Context context = ConfigurationImpl.get().getAppContext();
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo =
                packageManager.getApplicationInfo(context.getPackageName(), 0);
            String appName = (String) packageManager.getApplicationLabel(applicationInfo);
            String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + appName
                + "下载";
            String path = File.separator + getCurrentTime() + fileSuffix;
            if (!TextUtils.isEmpty(fileName)) {
                path = File.separator + fileName + fileSuffix;
            }
            File dir = new File(fileDir);
            if (!dir.exists()) {
                boolean b = dir.mkdirs();
            }
            File file = new File(dir, path);
            if (file.exists()) {
                return file;
            }
            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);
            byte[] fileReader = new byte[4096];
            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;
            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;
                if (progressListener != null) {
                    progressListener.progress(
                        (int) (((float) fileSizeDownloaded / fileSize) * 100));
                }
            }
            outputStream.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取当前系统时间
     */
    public static String getCurrentTime() {
        String str = "";
        Calendar c = Calendar.getInstance();
        str = str + c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(
            Calendar.DAY_OF_MONTH) + " " + +c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(
            Calendar.MINUTE);
        return str;
    }

    private static ProgressListener progressListener;

    interface ProgressListener {
        void progress(int progress);
    }

    private static void setProgress() {
        if (sDialog == null) {
            Context context = ConfigurationImpl.get().getAppContext();
            sDialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
        }
        sDialog.setCanceledOnTouchOutside(false);
        sDialog.setCancelable(false);
        sDialog.setMessage(DOWN_LOADING);
        sDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        sDialog.setMax(100);
        sDialog.setIndeterminate(false);
        sDialog.show();
        progressListener = new ProgressListener() {
            @Override public void progress(int progress) {
                sDialog.setProgress(progress);
            }
        };
    }

    public static class DownloadSubscriber extends Subscriber<ResponseBody> {
        private String fileName;

        public DownloadSubscriber(String fileName) {
            if (TextUtils.isEmpty(fileName)) {
                this.fileName = "";
            } else {
                this.fileName = fileName;
            }
        }

        @Override public void onStart() {
            super.onStart();
            if (!NetWorkTool.isNetworkConnected()) {
                Context context = ConfigurationImpl.get().getAppContext();
                Toast.makeText(context, HttpException.NETWORK_ERROR, Toast.LENGTH_SHORT).show();
                onCompleted();
            }
        }

        @Override public void onCompleted() {
            if (sDialog != null) {
                sDialog.dismiss();
            }
        }

        @Override public void onError(Throwable e) {
            if (sDialog != null) {
                sDialog.dismiss();
            }
            Context context = ConfigurationImpl.get().getAppContext();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override public void onNext(ResponseBody responseBody) {
            File file = writeResponseBodyToFile(responseBody, fileName);
            if (file != null && sSuccessListener != null) {
                sSuccessListener.success(file);
            }
        }
    }
}
