package com.codingapi.android.library.push.server;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import com.codingapi.android.library.push.R;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SocketService extends Service {
    private static final int NOTICE_ID = 1024;
    private static final String TAG = SocketService.class.getSimpleName();
    private static final String HOST = "58.87.75.118";
    private static final int PORT = 6666;

    private MediaPlayer mMediaPlayer;
    private Socket mSocket;
    private InetSocketAddress mAddress;

    public SocketService() {
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SocketService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, SocketService.class);
        context.stopService(intent);
    }

    @Override public void onCreate() {
        super.onCreate();
        mAddress = new InetSocketAddress(HOST, PORT);
        mSocket = new Socket();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 80, AudioManager.FLAG_PLAY_SOUND);
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent);
        mMediaPlayer.setLooping(true);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("通知");
        builder.setContentText("通知服务正在运行中...");
        startForeground(NOTICE_ID, builder.build());
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Thread thread = new Thread(runnable);
        thread.start();
        return START_STICKY;
    }

    private Runnable runnable = new Runnable() {
        @Override public void run() {
            try {
                startPlayMusic();
                mSocket.setKeepAlive(true);
                mSocket.connect(mAddress);
                OutputStreamWriter writer = new OutputStreamWriter(mSocket.getOutputStream());
                PrintWriter out = new PrintWriter(new BufferedWriter(writer), true);
                out.println("hello");
                StringBuilder sb = new StringBuilder();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = mSocket.getInputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    sb.append(outputStream.toString("UTF-8"));
                }
                Log.i(TAG, "<<<------" + sb.toString() + "-time: " + getTime());
                out.println("client-ack:(ok)");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void startPlayMusic() {
        if (mMediaPlayer != null) {
            Log.d(TAG, "启动后台播放音乐");
            mMediaPlayer.start();
        }
    }

    private void stopPlayMusic() {
        if (mMediaPlayer != null) {
            Log.d(TAG, "关闭后台播放音乐");
            mMediaPlayer.stop();
        }
    }

    @Override public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "任务管理器划掉");
        stopPlayMusic();
        // 重启自己
        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        startService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stopPlayMusic();
        // 重启自己
        //Intent intent = new Intent(getApplicationContext(), SocketService.class);
        //startService(intent);
    }

    private static final SimpleDateFormat sData = new SimpleDateFormat("HH:mm:ss", Locale.CHINESE);

    private String getTime() {
        return sData.format(System.currentTimeMillis());
    }
}
