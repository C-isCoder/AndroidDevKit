package com.codingapi.android.library.push;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.codingapi.android.library.push.server.SocketService;

public class MainActivity extends AppCompatActivity {
    private TextView tvText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvText = findViewById(R.id.text);
        registerReceiver(mReceiver, new IntentFilter(SocketService.MESSAGE));
        SocketService.start(this);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String ole = tvText.getText().toString();
            tvText.setText(ole + intent.getStringExtra(SocketService.MESSAGE));
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SocketService.stop(this);
        System.exit(1);
    }
}
