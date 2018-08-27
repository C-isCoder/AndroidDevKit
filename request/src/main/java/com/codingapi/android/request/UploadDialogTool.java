package com.codingapi.android.request;

import android.app.ProgressDialog;
import android.content.Context;
import java.lang.ref.WeakReference;

/**
 * Created by iCong.
 * Time:2017年7月6日
 */

public class UploadDialogTool {
    private static ProgressDialog sDialog;
    private static final String TIPS = "正在上传...";

    public static void show(Context context) {
        try {
            WeakReference<Context> wc = new WeakReference<Context>(context);
            if (sDialog == null) {
                sDialog = new ProgressDialog(wc.get(), ProgressDialog.THEME_HOLO_LIGHT);
                sDialog.setCanceledOnTouchOutside(false);
                sDialog.setCancelable(false);
                sDialog.setMessage(TIPS);
                sDialog.show();
            } else {
                sDialog.show();
            }
        } catch (Exception ignore) {
        }
    }

    public static void dismiss() {
        try {
            if (sDialog != null) {
                sDialog.dismiss();
                sDialog = null;
            }
        } catch (Exception ignored) {
        }
    }
}
