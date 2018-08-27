package com.codingapi.android.request;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.baichang.android.config.ConfigurationImpl;

/**
 * Created by iCong.
 * Time:2016/12/5-11:31.
 */

public class NetWorkTool {
    public static boolean isNetworkConnected() {
        Context context = ConfigurationImpl.get().getAppContext();
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnectedOrConnecting();
        }
    }
}
