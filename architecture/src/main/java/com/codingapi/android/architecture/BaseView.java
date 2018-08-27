package com.codingapi.android.architecture;

public interface BaseView {

    void showMessage(String msg);

    void showMessage(int msgId);

    void showProgress();

    void hideProgress();
}
