package com.codingapi.android.common;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppManager.getAppManager().addActivity(this);
    }

    public void back(View view) {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
    }

    protected Activity getActivity() {
        return this;
    }

    public void toast(Object content) {
        if (content instanceof String) {
            Toast.makeText(this, (CharSequence) content, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, this.getString((Integer) content), Toast.LENGTH_SHORT).show();
        }
    }
}
