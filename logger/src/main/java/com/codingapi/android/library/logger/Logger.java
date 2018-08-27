package com.codingapi.android.library.logger;

import android.util.Log;
import com.codingapi.android.config.Configuration;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Logger {
    private final static String TAG = Logger.class.getSimpleName();
    private static boolean isWriteLog = false;
    private final static String PREFIX_DATE = "yyyy-MM-DD";
    private static final Locale LOCALE = Locale.getDefault();
    private final static SimpleDateFormat sDateFormat = new SimpleDateFormat(PREFIX_DATE, LOCALE);

    private Logger() {
    }

    public static void setWriteLog(boolean isWriteLog) {
        Logger.isWriteLog = isWriteLog;
    }

    private static boolean isWriteLog() {
        return isWriteLog;
    }

    // debug
    public static void d(String tag, String message) {
        if (Configuration.get().isDebug()) {
            Log.d(tag, message);
        }
    }

    // debug
    public static void d(String message) {
        if (Configuration.get().isDebug()) {
            Log.d(TAG, message);
        }
    }

    // info
    public static void i(String tag, String message) {
        if (Configuration.get().isDebug()) {
            Log.i(tag, message);
        }
    }

    // info
    public static void i(String message) {
        if (Configuration.get().isDebug()) {
            Log.i(TAG, message);
        }
    }

    // warn
    public static void w(String tag, String message) {
        if (Configuration.get().isDebug()) {
            Log.w(tag, message);
        }
    }

    // warn
    public static void w(String message) {
        if (Configuration.get().isDebug()) {
            Log.w(TAG, message);
        }
    }

    // error
    public static void e(String tag, String message) {
        if (Configuration.get().isDebug()) {
            Log.e(tag, message);
        }
        if (isWriteLog()) {
            saveLog2File("ERROR", tag, message);
        }
    }

    // error
    public static void e(String tag, String message, Throwable e) {
        if (Configuration.get().isDebug()) {
            Log.e(tag, message, e);
        }
        if (isWriteLog()) {
            saveLog2File("ERROR", tag, message + e.toString());
        }
    }

    // error
    public static void e(String message, Throwable e) {
        if (Configuration.get().isDebug()) {
            Log.e(TAG, message, e);
        }
        if (isWriteLog()) {
            saveLog2File("ERROR", TAG, message + e.toString());
        }
    }

    // error
    public static void e(String message) {
        if (Configuration.get().isDebug()) {
            Log.e(TAG, message);
        }
        if (isWriteLog()) {
            saveLog2File("ERROR", TAG, message);
        }
    }

    /**
     * 日志写入
     *
     * @param type 日志类别 INFO DEBUG ERROR WARN
     * @param tag 标签
     * @param message 日志消息
     */
    public static void r(String type, String tag, String message) {
        if (Configuration.get().isDebug()) {
            Log.w(tag, message);
        }
        if (isWriteLog()) {
            saveLog2File(type, tag, message);
        }
    }

    /**
     * 日志写入
     *
     * @param type 日志类别 INFO DEBUG ERROR WARN
     * @param message 日志消息
     */
    public static void r(String type, String message) {
        if (Configuration.get().isDebug()) {
            Log.w(TAG, message);
        }
        if (isWriteLog()) {
            saveLog2File(type, TAG, message);
        }
    }

    /**
     * 日志写入
     *
     * @param message 日志消息
     */
    public static void r(String message) {
        if (Configuration.get().isDebug()) {
            Log.w(TAG, message);
        }
        if (isWriteLog()) {
            saveLog2File("LOG", TAG, message);
        }
    }

    /**
     * 日志写入缓存
     *
     * @param type 日志类别 INFO DEBUG ERROR WARN
     * @param tag 标签
     * @param message 日志消息
     */
    private static void saveLog2File(String type, String tag, String message) {
        final File logDir = getLogDir();
        if (!logDir.exists()) {
            boolean b = logDir.mkdir();
        }
        final String logName = "log_" + sDateFormat.format(System.currentTimeMillis()) + ".log";
        final File logFile = new File(logDir, logName);
        // 创建一个日历对象
        Calendar calendar = Calendar.getInstance();
        final int Hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int Minute = calendar.get(Calendar.MINUTE);
        final int Second = calendar.get(Calendar.SECOND);
        try {
            final FileWriter fileWriter = new FileWriter(logFile, true);
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            final String format = "[%s] %s %30s   %s";
            final String time = String.format(LOCALE, "%02d:%02d:%02d", Hour, Minute, Second);
            final String writeMessage = String.format(format, time, type, tag, message);
            bufferedWriter.write(writeMessage);
            bufferedWriter.newLine();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getLogDir() {
        return new File(Configuration.get().getAppContext().getCacheDir(), "/log/");
    }
}
