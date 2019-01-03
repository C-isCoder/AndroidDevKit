package com.codingapi.android.common;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class BaseFragment extends Fragment {
    public static final int RESULT_OK = -1;

    public void toast(Object content) {
        if (content instanceof String) {
            Toast.makeText(getActivity(), (CharSequence) content, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), this.getString((Integer) content), Toast.LENGTH_SHORT)
                .show();
        }
    }

    public Context getContext() {
        return getActivity();
    }

    protected Fragment getFragment() {
        return this;
    }
}
