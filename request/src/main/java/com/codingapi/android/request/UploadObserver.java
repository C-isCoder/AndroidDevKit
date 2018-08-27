package com.codingapi.android.request;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;
import com.codingapi.android.library.logger.Logger;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import rx.Subscriber;

/**
 * Created by iCong.
 * Time:2017年7月6日
 */

public abstract class UploadObserver<T> extends Subscriber<T> {
    private Context mContext = null;
    private ProgressDialog mDialog = null;
    private static final String TAG = "Upload";

    public UploadObserver(Context context) {
        if (context == null) throw new NullPointerException("Context not null");
        WeakReference<Context> wf = new WeakReference<Context>(context);
        mContext = wf.get();
    }

    public UploadObserver(ProgressDialog dialog) {
        if (dialog == null) throw new NullPointerException("ProgressDialog not null");
        mDialog = dialog;
    }

    @Override public void onStart() {
        super.onStart();
        if (!NetWorkTool.isNetworkConnected()) {
            Toast.makeText(mContext, HttpException.NETWORK_ERROR, Toast.LENGTH_SHORT).show();
            onCompleted();
        }
        if (mDialog != null) {
            mDialog.show();
        } else {
            UploadDialogTool.show(mContext);
        }
    }

    @Override public void onCompleted() {
        if (mDialog != null) {
            mDialog.dismiss();
        } else {
            UploadDialogTool.dismiss();
        }
    }

    @Override public void onError(Throwable e) {
        if (mDialog != null) {
            mDialog.dismiss();
        } else {
            UploadDialogTool.dismiss();
        }
        if (e instanceof SocketTimeoutException) {
            Toast.makeText(mContext, HttpException.NET_REQUEST_TIME_OUT, Toast.LENGTH_SHORT).show();
        } else if (e instanceof ConnectException) {
            Toast.makeText(mContext, HttpException.NET_CONNECT_ERROR, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            Logger.e(TAG, e.getMessage(), e);
        }
    }

    @Override public void onNext(T t) {
    }
}
