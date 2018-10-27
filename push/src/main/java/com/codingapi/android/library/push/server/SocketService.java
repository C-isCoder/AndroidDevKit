package com.codingapi.android.library.push.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.codingapi.android.library.push.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;

public class SocketService extends Service {
    private static final String TAG = SocketService.class.getSimpleName();
    public static final String MESSAGE = SocketService.class.getName();

    private static final int NOTICE_ID = 1024;
    private static final String HOST = "58.87.75.118";
    private static final int PORT = 6666;

    private MediaPlayer mMediaPlayer;
    private Socket mSocket;
    private static final InetSocketAddress SOCKET_ADDRESS = new InetSocketAddress(HOST, PORT);

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

    @Override
    public void onCreate() {
        super.onCreate();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 80, AudioManager.FLAG_PLAY_SOUND);
        }
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent);
        mMediaPlayer.setLooping(true);
        startForeground();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPlayMusic();
        Thread thread = new Thread(runnable);
        thread.start();
        return START_STICKY;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    if (mSocket == null) {
                        mSocket = new Socket();
                        mSocket.connect(SOCKET_ADDRESS);
                    }
                    if (mSocket.isClosed() || !mSocket.isConnected()) {
                        mSocket.connect(SOCKET_ADDRESS);
                    }
                    OutputStreamWriter writer = new OutputStreamWriter(mSocket.getOutputStream());
                    PrintWriter out = new PrintWriter(new BufferedWriter(writer), true);
                    out.println("client-ack:(ok)");
                    StringBuilder sb = new StringBuilder();
                    InputStream inputStream = mSocket.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    int value;
                    while ((value = in.read()) != -1) {
                        if ((char) value == '.') {
                            break;
                        }
                        sb.append((char) value);
                    }
                    final Intent intent = new Intent(MESSAGE);
                    intent.putExtra(MESSAGE, "<<<------" + sb.toString() + "-time: " + getTime() + "\n");
                    sendBroadcast(intent);
                    Log.i(TAG, "<<<------" + sb.toString() + "-time: " + getTime());
                    Thread.sleep(60 * 1000 * 5);
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "UnknownHostException", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException", e);
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
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

    private void startForeground() {
        String channelId;
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("my_service", "My Background Service");
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
            notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("通知")
                    .setContentText("通知服务正在运行中...")
                    .setPriority(PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        } else {
            Notification.Builder builder = new Notification.Builder(this);
            notification = builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("通知")
                    .setContentText("通知服务正在运行中...").build();
        }
        startForeground(NOTICE_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String id, String name) {
        NotificationChannel chan = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (service != null) {
            service.createNotificationChannel(chan);
        }
        return id;
    }
}
