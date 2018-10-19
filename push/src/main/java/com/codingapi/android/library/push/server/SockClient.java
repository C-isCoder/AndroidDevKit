package com.codingapi.android.library.push.server;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SockClient extends AsyncTask<Void, Void, Void> {

    private static final String TAG = SockClient.class.getSimpleName();

    private Socket socket;

    public SockClient(String address, int port) {
        try {
            socket = new Socket(address, port);
        } catch (IOException e) {
            Log.e(TAG, "create socket client error", e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            if (null != socket) {
                OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
                PrintWriter out = new PrintWriter(new BufferedWriter(writer), true);
                out.println("hello");
            }
            StringBuilder sb = new StringBuilder();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int bytesRead;
            InputStream inputStream = socket.getInputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                sb.append(outputStream.toString("UTF-8"));
            }
            Log.i(TAG, "<<<------" + sb.toString());
            if (null != socket) {
                OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
                PrintWriter out = new PrintWriter(new BufferedWriter(writer), true);
                out.println("client-ack:(ok)");
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "UnknownHostException", e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
