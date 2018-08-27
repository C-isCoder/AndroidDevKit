package com.codingapi.android.common;

import android.app.Application;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.codingapi.android.common.cache.ACache;

public abstract class BaseApplication extends Application {

    private static BaseApplication sInstance;
    //本地缓存
    public static ACache aCache;
    // 屏幕宽度
    private static int screenWidth;
    // 屏幕高度
    private static int screenHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        init();
        initACache();
        //崩溃日志
        CrashHandler.getInstance().init(getApplicationContext());
    }

    private void init() {
        DisplayMetrics display = getDisplayMetrics();
        screenWidth = display.widthPixels;
        screenHeight = display.heightPixels;
    }

    protected abstract void initACache();

    public static BaseApplication getsInstance() {
        return sInstance;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    /**
     * 获取屏幕尺寸与密度.
     *
     * @return mDisplayMetrics
     */
    public DisplayMetrics getDisplayMetrics() {
        Resources mResources;
        mResources = this.getResources();
        return mResources.getDisplayMetrics();
    }
}
