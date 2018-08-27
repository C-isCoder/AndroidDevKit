package com.codingapi.android.request;

import com.codingapi.android.library.logger.Logger;
import rx.Subscriber;

/**
 * Created by iCong.
 *
 * Date:2017年7月6日
 */

public class HttpObserver<T> extends Subscriber<T> {
    private static final String TAG = "HTTP_RESPONSE";
    private HttpListener<T> mResultListener;

    public HttpObserver(HttpListener<T> listener) {
        if (listener == null) {
            throw new NullPointerException("HttpListener not null");
        }
        mResultListener = listener;
    }

    @Override public void onCompleted() {
        mResultListener.onComplete();
    }

    @Override public void onError(Throwable e) {
        mResultListener.error(e);
        Logger.e(TAG, e.getMessage(), e);
    }

    @Override public void onStart() {
        super.onStart();
        mResultListener.onStart();
        if (!NetWorkTool.isNetworkConnected()) {
            mResultListener.onComplete();
            mResultListener.error(new Throwable(HttpException.NETWORK_ERROR));
        }
    }

    @Override public void onNext(T t) {
        mResultListener.success(t);
    }
}
