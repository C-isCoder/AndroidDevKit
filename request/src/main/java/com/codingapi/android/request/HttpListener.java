package com.codingapi.android.request;

/**
 * Created by iCong.
 * Time:2016/9/21-14:12.
 */

public interface HttpListener<T> {
    void onStart();

    void success(T t);

    void error(Throwable e);

    void onComplete();
}
