package com.codingapi.android.config;

import android.content.Context;

/**
 * Created by iscod.
 * Time:2016/12/7-15:03.
 */

public class Configuration implements IConfiguration {
    private static IConfiguration sConfig;
    private static Configuration sInstance;

    private Configuration() {
    }

    public static Configuration get() {
        if (sInstance == null) {
            sInstance = new Configuration();
        }
        return sInstance;
    }

    public static void init(IConfiguration config) {
        sConfig = config;
    }

    @Override
    public String getApiDefaultHost() {
        return sConfig.getApiDefaultHost();
    }

    @Override
    public String getApiWebView() {
        return sConfig.getApiWebView();
    }

    @Override
    public Context getAppContext() {
        return sConfig.getAppContext();
    }

    @Override
    public String getApiUploadImage() {
        return sConfig.getApiUploadImage();
    }

    @Override
    public String getApiLoadImage() {
        return sConfig.getApiLoadImage();
    }

    @Override
    public String getToken() {
        return sConfig.getToken();
    }

    @Override
    public String getApiDownload() {
        return sConfig.getApiDownload();
    }

    @Override
    public String getApiUpload() {
        return sConfig.getApiUpload();
    }

    @Override
    public void refreshToken() {
        sConfig.refreshToken();
    }

    @Override
    public int getAppBarColor() {
        return sConfig.getAppBarColor();
    }

    @Override public boolean isDebug() {
        return sConfig.isDebug();
    }
}
