package com.codingapi.android.common;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import com.codingapi.android.library.logger.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 12-12-10
 * Time: 上午12:56
 * Description:
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();
    //CrashHandler实例
    private static CrashHandler sInstance;
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> infoMap = new HashMap<String, String>();
    //用于格式化日期,作为日志文件名的一部分
    private final static String PREFIX_DATE = "yyyyMMdd-HH:mm:ss";
    private static DateFormat sDateFormat = new SimpleDateFormat(PREFIX_DATE, Locale.getDefault());

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (sInstance == null) {
            synchronized (CrashHandler.class) {
                if (sInstance == null) {
                    sInstance = new CrashHandler();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        mContext = context;
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Logger.e(TAG, "error : ", e);
        }
        //不加上不打印
        boolean result = handleException(ex);
        if (result) {
            Logger.w(TAG, "handler crash success");
        } else {
            Logger.w(TAG, "handler crash failed");
        }
        //重新启动程序
        PackageManager manager = mContext.getPackageManager();
        Intent intent = manager.getLaunchIntentForPackage(mContext.getPackageName());
        if (intent == null) return;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "1");
        mContext.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //收集设备参数信息
        collectDeviceInfo();
        //保存日志文件
        final String fileName = saveCrashInfo2File(ex);
        Logger.w(TAG, String.format("crash file name is %s", fileName));
        ex.printStackTrace();
        return true;
    }

    /**
     * 收集设备参数信息
     */
    private void collectDeviceInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infoMap.put("versionName", versionName);
                infoMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infoMap.put(field.getName(), field.get(null).toString());
                Logger.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Logger.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    private File getCrashLogFolder() {
        File cacheDir = mContext.getCacheDir();
        File crashDir = new File(cacheDir, "/crash/");
        if (!crashDir.exists()) {
            boolean b = cacheDir.mkdir();
        }
        return crashDir;
    }

    /**
     * 保存错误信息到文件中
     *
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infoMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            final String time = sDateFormat.format(new Date());
            final String fileName = "Crash_" + time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                final String crashDir = getCrashLogFolder().getAbsolutePath();
                Logger.d(TAG, String.format("crash log dir is %s", crashDir));
                final File file = new File(getCrashLogFolder(), fileName);
                Logger.d(TAG, String.format("crash log file is %s", file.getAbsolutePath()));
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Logger.e(TAG, "an error occur while writing file...", e);
        }
        return null;
    }
}
