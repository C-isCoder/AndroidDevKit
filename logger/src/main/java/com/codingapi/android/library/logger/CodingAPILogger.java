package com.codingapi.android.library.logger;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.dianping.logan.Logan;
import com.dianping.logan.LoganConfig;
import com.dianping.logan.OnLoganProtocolStatus;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

public class CodingAPILogger {

    private final static String TAG = CodingAPILogger.class.getSimpleName();
    private final static String PREFIX_DATE = "yyyy-MM-dd";
    private final static String AES_KEY = "codingapi1234567";
    private static final Locale LOCALE = Locale.CHINESE;
    private final static SimpleDateFormat sDateFormat = new SimpleDateFormat(PREFIX_DATE, LOCALE);
    private static final String FILE_NAME = "CodingAPI_android_logger";
    private static boolean isDebug;
    private static CodingAPILoggerReport sCodingAPILoggerReport;

    public enum TYPE {
        /**
         * 信息类别
         */
        INFO,
        /**
         * 调试类别
         */
        DEBUG,
        /**
         * 错误类别
         */
        ERROR,
        /**
         * 网络类别
         */
        NETWORK,
        /**
         * 业务逻辑
         */
        LOGIC
    }

    private CodingAPILogger() {
    }

    public static void init(Context context, String uploadUrl) {
        LoganConfig config = new LoganConfig.Builder()
          .setCachePath(context.getFilesDir().getAbsolutePath())
          .setPath(
            context.getExternalFilesDir(null).getAbsolutePath() + File.separator + FILE_NAME
          )
          .setEncryptKey16(AES_KEY.getBytes())
          .setEncryptIV16(AES_KEY.getBytes())
          .build();
        Logan.init(config);
        Logan.setOnLoganProtocolStatus(new OnLoganProtocolStatus() {
            @Override
            public void loganProtocolStatus(String cmd, int code) {
                Log.d(TAG, "clogan > cmd : " + cmd + " | " + "code : " + code);
            }
        });
        sCodingAPILoggerReport = new CodingAPILoggerReport(uploadUrl);
    }

    public static void setDebugMode() {
        isDebug = true;
        Logan.setDebug(true);
    }

    // debug
    public static void d(String tag, String message) {
        if (isDebug) {
            Log.d(tag, message);
        }
    }

    // debug
    public static void d(String message) {
        if (isDebug) {
            Log.d(TAG, message);
        }
    }

    // info
    public static void i(String tag, String message) {
        if (isDebug) {
            Log.i(tag, message);
        }
    }

    // info
    public static void i(String message) {
        if (isDebug) {
            Log.i(TAG, message);
        }
    }

    // warn
    public static void w(String tag, String message) {
        if (isDebug) {
            Log.w(tag, message);
        }
    }

    // warn
    public static void w(String message) {
        if (isDebug) {
            Log.w(TAG, message);
        }
    }

    // error
    public static void e(String tag, String message) {
        if (isDebug) {
            Log.e(tag, message);
        }
        saveLog2File(TYPE.ERROR, tag, message);
    }

    // error
    public static void e(String tag, String message, Throwable e) {
        if (isDebug) {
            Log.e(tag, message, e);
        }
        saveLog2File(TYPE.ERROR, tag, message + e.toString());
    }

    // error
    public static void e(String message, Throwable e) {
        if (isDebug) {
            Log.e(TAG, message, e);
        }
        saveLog2File(TYPE.ERROR, TAG, message + e.toString());
    }

    // error
    public static void e(String message) {
        if (isDebug) {
            Log.e(TAG, message);
        }
        saveLog2File(TYPE.ERROR, TAG, message);
    }

    /**
     * 日志写入
     *
     * @param type 日志类别 INFO DEBUG ERROR WARN
     * @param tag 标签
     * @param message 日志消息
     */
    public static void write(TYPE type, String tag, String message) {
        if (isDebug) {
            Log.w(tag, message);
        }
        saveLog2File(type, tag, message);
    }

    /**
     * 日志写入
     *
     * @param type 日志类别 INFO DEBUG ERROR WARN
     * @param message 日志消息
     */
    public static void write(TYPE type, String message) {
        if (isDebug) {
            Log.w(TAG, message);
        }
        saveLog2File(type, TAG, message);
    }

    /**
     * 日志写入缓存
     *
     * @param type 日志类别 INFO DEBUG ERROR WARN
     * @param tag 标签
     * @param message 日志消息
     */
    private static void saveLog2File(TYPE type, String tag, String message) {
        Logan.w(tag + " <-> " + message, type.ordinal());
    }

    /**
     * 获取本地所有日志信息
     *
     * @return key为日期，value为日志文件大小（Bytes）。
     */
    public static Map<String, Long> getLogs() {
        return Logan.getAllFilesInfo();
    }

    /**
     * 上报日志，默认当天
     */
    public static void upload() {
        upload(new String[] { sDateFormat.format(System.currentTimeMillis()) });
    }

    /**
     * 上报某一天日志
     *
     * @param date 日期，格式：“2018-11-08”
     */
    public static void upload(String date) {
        if (TextUtils.isEmpty(date)) {
            throw new NullPointerException("Date not null");
        }
        final String[] s = date.split("-");
        if (s.length < 3 || s[0].length() != 4 || s[1].length() != 2 || s[2].length() != 2) {
            throw new IllegalArgumentException("check date error , please check date is right. "
              + "eg: 2018-11-08");
        }
        upload(new String[] { date });
    }

    /**
     * 上报所有日志
     */
    public static void uploadAll() {
        final Map<String, Long> info = Logan.getAllFilesInfo();
        if (info == null) {
            Log.i(TAG, "未找到日志文件");
            return;
        }
        upload(info.keySet().toArray(new String[] {}));
    }

    /**
     * 上报日志
     *
     * @param dates 日期数组，格式：“2018-11-08”
     */
    public static void upload(String[] dates) {
        if (dates == null) {
            throw new NullPointerException("dates not null");
        }
        Logan.s(dates, sCodingAPILoggerReport);
    }
}
