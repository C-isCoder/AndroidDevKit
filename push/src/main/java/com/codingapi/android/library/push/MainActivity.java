package com.codingapi.android.library.push;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.codingapi.android.library.push.server.SocketService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SocketService.start(this);
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        SocketService.stop(this);
        System.exit(1);
    }
}
