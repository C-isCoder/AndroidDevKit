package com.codingapi.android.library.printer.gpsdk;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.codingapi.android.library.printer.R;
import com.gprinter.io.PortManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by iCong
 */
public class PrintDeviceManager {
    private static final String TAG = PrintDeviceManager.class.getSimpleName();
    /**
     * ESC查询打印机实时状态指令
     */
    private byte[] esc = { 0x10, 0x04, 0x02 };

    /**
     * ESC查询打印机实时状态 缺纸状态
     */
    private static final int ESC_STATE_PAPER_ERR = 0x20;

    /**
     * ESC指令查询打印机实时状态 打印机开盖状态
     */
    private static final int ESC_STATE_COVER_OPEN = 0x04;

    /**
     * ESC指令查询打印机实时状态 打印机报错状态
     */
    private static final int ESC_STATE_ERR_OCCURS = 0x40;

    /**
     * TSC查询打印机状态指令
     */
    private byte[] tsc = { 0x1b, '!', '?' };

    /**
     * TSC指令查询打印机实时状态 打印机缺纸状态
     */
    private static final int TSC_STATE_PAPER_ERR = 0x04;

    /**
     * TSC指令查询打印机实时状态 打印机开盖状态
     */
    private static final int TSC_STATE_COVER_OPEN = 0x01;

    /**
     * TSC指令查询打印机实时状态 打印机出错状态
     */
    private static final int TSC_STATE_ERR_OCCURS = 0x80;
    private static PrintDeviceManager sDeviceManager;
    private static final int READ_DATA = 10000;
    private static final String READ_DATA_CNT = "read_data_cnt";
    private static final String READ_BUFFER_ARRAY = "read_buffer_array";
    public static final String ACTION_CONN_STATE = "action_connect_state";
    public static final String STATE = "state";
    public static final int CONN_STATE_DISCONNECT = 0x90;
    public static final int CONN_STATE_CONNECTING = CONN_STATE_DISCONNECT << 1;
    public static final int CONN_STATE_FAILED = CONN_STATE_DISCONNECT << 2;
    public static final int CONN_STATE_CONNECTED = CONN_STATE_DISCONNECT << 3;
    /**
     * 判断打印机所使用指令是否是ESC指令
     */
    private PrinterCommand currentPrinterCommand;
    private byte[] sendCommand;
    private PrinterReader mPrinterReader;
    private PortManager mPort;
    private Context mContext;
    private boolean isOpenPort;
    // 蓝牙适配器
    private BluetoothAdapter mAdapter;
    // 蓝牙 io
    private BluetoothSocket mSocket;
    // 蓝牙设备
    private BluetoothDevice mDevice;
    // 输入流
    private InputStream mInputStream;
    // 读取流
    private OutputStream mOutputStream;
    // UUID
    private static final UUID sUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private PrintDeviceManager(Context context) {
        this.mContext = context;
    }

    public static PrintDeviceManager getInstance(Context context) {
        if (sDeviceManager == null) {
            synchronized (PrintDeviceManager.class) {
                if (sDeviceManager == null) {
                    WeakReference<Context> weakReference = new WeakReference<>(context);
                    sDeviceManager = new PrintDeviceManager(weakReference.get());
                }
            }
        }
        return sDeviceManager;
    }

    /**
     * 连接打印机
     */
    public void connection(String address) {
        sendStateBroadcast(CONN_STATE_CONNECTING);
        Log.i(TAG, "开始连接接打印机...");
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {
            showMessage(R.string.print_not_bluetooth);
            Log.e(TAG, "打印机连接失败，设备不支持蓝牙");
        } else if (!mAdapter.isEnabled()) {
            showMessage("请打开蓝牙");
            Log.e(TAG, "打印机连接失败，未开启蓝牙");
        } else {
            mDevice = mAdapter.getRemoteDevice(address);
            try {
                mSocket = mDevice.createRfcommSocketToServiceRecord(sUUID);
                mAdapter.cancelDiscovery();
                mSocket.connect();
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
                Log.i(TAG, "打印机连接成功...");
                sendCommand("PRINT 10\n");
            } catch (IOException ioe) {
                Log.e(TAG, "打印机连接失败", ioe);
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ioe.printStackTrace();
            }
        }
    }

    private void sendCommand(String message) {
        byte[] bytes = message.getBytes();
        try {
            mOutputStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "向打印机发送是指令失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 查询当前连接打印机所使用打印机指令（ESC（EscCommand.java）、TSC（LabelCommand.java））
     */
    private void queryCommand() {
        //开启读取打印机返回数据线程
        mPrinterReader = new PrinterReader();
        mPrinterReader.start();
        //查询打印机所使用指令
        queryPrinterCommand();
    }

    /**
     * 已经连接
     */
    public boolean isConnected() {
        return isOpenPort;
    }

    /**
     * 关闭端口
     */
    private void closePort() {
        if (this.mPort != null) {
            this.mPort.closePort();
            isOpenPort = false;
            currentPrinterCommand = null;
        }
        sendStateBroadcast(CONN_STATE_DISCONNECT);
    }

    /**
     * 获取当前打印机指令
     *
     * @return PrinterCommand
     */
    public PrinterCommand getCurrentPrinterCommand() {
        return sDeviceManager.currentPrinterCommand;
    }

    public void sendDataImmediately(final Vector<Byte> data) {
        if (this.mPort == null) {
            return;
        }
        try {
            Log.e(TAG,
                "data -> " + new String(com.gprinter.utils.Utils.convertVectorByteTobytes(data),
                    "gb2312"));
            this.mPort.writeDataImmediately(data, 0, data.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int readDataImmediately(byte[] buffer) throws IOException {
        return this.mPort.readData(buffer);
    }

    /**
     * 查询打印机当前使用的指令（TSC、ESC）
     */
    private void queryPrinterCommand() {
        ThreadPool.getInstantiation().addTask(new Runnable() {
            @Override public void run() {
                //发送ESC查询打印机状态指令
                sendCommand = esc;
                Vector<Byte> data = new Vector<>(esc.length);
                for (byte anEsc : esc) {
                    data.add(anEsc);
                }
                sendDataImmediately(data);
                //开启计时器，隔2000毫秒没有没返回值时发送TSC查询打印机状态指令
                final ThreadFactoryBuilder thread =
                    new ThreadFactoryBuilder(PrintDeviceManager.class.getSimpleName());
                final ScheduledExecutorService scheduled =
                    new ScheduledThreadPoolExecutor(1, thread);
                scheduled.schedule(thread.newThread(new Runnable() {
                    @Override public void run() {
                        if (currentPrinterCommand == null
                            || currentPrinterCommand != PrinterCommand.ESC) {
                            Log.e(TAG, Thread.currentThread().getName());
                            //发送TSC查询打印机状态指令
                            sendCommand = tsc;
                            Vector<Byte> data = new Vector<>(tsc.length);
                            for (byte aTsc : tsc) {
                                data.add(aTsc);
                            }
                            sendDataImmediately(data);
                            //开启计时器，隔2000毫秒打印机没有响应者停止读取打印机数据线程并且关闭端口
                            scheduled.schedule(thread.newThread(new Runnable() {
                                @Override public void run() {
                                    if (currentPrinterCommand == null) {
                                        if (mPrinterReader != null) {
                                            mPrinterReader.cancel();
                                            mPort.closePort();
                                            isOpenPort = false;
                                            sendStateBroadcast(CONN_STATE_FAILED);
                                        }
                                    }
                                }
                            }), 2000, TimeUnit.MILLISECONDS);
                        }
                    }
                }), 2000, TimeUnit.MILLISECONDS);
            }
        });
    }

    class PrinterReader extends Thread {
        private boolean isRun;

        private byte[] buffer = new byte[100];

        PrinterReader() {
            isRun = true;
        }

        @Override public void run() {
            try {
                while (isRun) {
                    //读取打印机返回信息
                    int len = readDataImmediately(buffer);
                    if (len > 0) {
                        Message message = Message.obtain();
                        message.what = READ_DATA;
                        Bundle bundle = new Bundle();
                        bundle.putInt(READ_DATA_CNT, len);
                        bundle.putByteArray(READ_BUFFER_ARRAY, buffer);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }
            } catch (Exception e) {
                if (sDeviceManager != null) {
                    closePort();
                }
            }
        }

        void cancel() {
            isRun = false;
        }
    }

    private static final String printer_conn_normal = "打印机连接正常";
    private static final String printer_out_of_paper = "打印机缺纸";
    private static final String printer_open_cover = "打印机开盖";
    private static final String printer_error = "打印机出错";

    @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_DATA:
                    int cnt = msg.getData().getInt(READ_DATA_CNT);
                    byte[] buffer = msg.getData().getByteArray(READ_BUFFER_ARRAY);
                    //这里只对查询状态返回值做处理，其它返回值可参考编程手册来解析
                    if (buffer == null) {
                        return;
                    }
                    int result = judgeResponseType(buffer[0]);
                    String status = printer_conn_normal;
                    if (sendCommand == esc) {
                        //设置当前打印机模式为ESC模式
                        if (currentPrinterCommand == null) {
                            currentPrinterCommand = PrinterCommand.ESC;
                            sendStateBroadcast(CONN_STATE_CONNECTED);
                        } else if (result == 1) {//查询打印机实时状态
                            if ((buffer[0] & ESC_STATE_PAPER_ERR) > 0) {
                                status += " " + printer_out_of_paper;
                            }
                            if ((buffer[0] & ESC_STATE_COVER_OPEN) > 0) {
                                status += " " + printer_open_cover;
                            }
                            if ((buffer[0] & ESC_STATE_ERR_OCCURS) > 0) {
                                status += " " + printer_error;
                            }
                            Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
                        }
                    } else if (sendCommand == tsc) {
                        //设置当前打印机模式为TSC模式
                        if (currentPrinterCommand == null) {
                            currentPrinterCommand = PrinterCommand.TSC;
                            sendStateBroadcast(CONN_STATE_CONNECTED);
                        } else if (cnt == 1) {//查询打印机实时状态
                            if ((buffer[0] & TSC_STATE_PAPER_ERR) > 0) {//缺纸
                                status += " " + printer_out_of_paper;
                            }
                            if ((buffer[0] & TSC_STATE_COVER_OPEN) > 0) {//开盖
                                status += " " + printer_open_cover;
                            }
                            if ((buffer[0] & TSC_STATE_ERR_OCCURS) > 0) {//打印机报错
                                status += " " + printer_error;
                            }
                            Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // 发送广播
    private void sendStateBroadcast(int state) {
        Intent intent = new Intent(ACTION_CONN_STATE);
        intent.putExtra(STATE, state);
        mContext.sendBroadcast(intent);
    }

    // 判断是实时状态（10 04 02）还是查询状态（1D 72 01）
    private int judgeResponseType(byte r) {
        return (byte) ((r & 0x10) >> 4);
    }

    // 释放资源
    public void release() {
        if (mPrinterReader != null) {
            if (mPrinterReader.isRun) {
                mPrinterReader.cancel();
            }
            mPrinterReader = null;
        }
        if (sDeviceManager != null) {
            sDeviceManager.closePort();
            sDeviceManager = null;
        }
        if (mContext != null) {
            mContext = null;
        }
        mHandler = null;
    }

    private void showMessage(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private void showMessage(int msgRes) {
        Toast.makeText(mContext, msgRes, Toast.LENGTH_SHORT).show();
    }
}