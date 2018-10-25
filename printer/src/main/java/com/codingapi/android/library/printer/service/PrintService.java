package com.codingapi.android.library.printer.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.codingapi.android.library.printer.gpsdk.PrintDeviceManager;
import com.codingapi.android.library.printer.gpsdk.PrinterCommand;
import com.codingapi.android.library.printer.gpsdk.ThreadFactoryBuilder;
import com.codingapi.android.library.printer.gpsdk.ThreadPool;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import java.io.Serializable;
import java.util.Vector;

/**
 * Created by iCong on BLUETOOTH_PORT1/BLUETOOTH_PORT3/2BLUETOOTH_PORT18.
 */

public class PrintService extends Service {

    private static final String TAG = PrintService.class.getSimpleName();
    // 打印机蓝牙地址
    public static final String BLUETOOTH_ADDRESS = "bluetooth_address";
    // 打印数据
    public static final String PRINT_DATA = "print_data";
    // 打印模式
    public static final String PRINT_MODEL = "print_model";
    // 蓝牙地址
    private String mBluetoothAddress = "";
    // 蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    // 蓝牙 io
    private BluetoothSocket mBluetoothSocket;
    // 线程池
    private ThreadFactoryBuilder mThreadFactoryBuilder;
    /**
     * 使用打印机指令错误
     */
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    /**
     * 打印机断开连接
     */
    public static final int CONN_STATE_DISCONNECTION = 0x007;
    // 打印内容
    private PrintData mPrintData;
    // 打印模式 默认正常
    private MODEL model = MODEL.NORMAL;
    // 打印机管理
    private PrintDeviceManager mDeviceManager;

    public enum MODEL implements Serializable {
        NORMAL, TEST
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        mThreadFactoryBuilder = new ThreadFactoryBuilder(PrintService.class.getSimpleName());
        IntentFilter filter = new IntentFilter(PrintDeviceManager.ACTION_CONN_STATE);
        registerReceiver(mDeviceStateReceiver, filter);
        mDeviceManager = PrintDeviceManager.getInstance(this);
    }

    @Override public void onDestroy() {
        unregisterReceiver(mDeviceStateReceiver);
        mDeviceManager.release();
        super.onDestroy();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mBluetoothAddress = intent.getStringExtra(BLUETOOTH_ADDRESS);
            if (intent.hasExtra(PRINT_MODEL)) {
                model = (MODEL) intent.getSerializableExtra(PRINT_MODEL);
            }
            if (model != MODEL.TEST && intent.hasExtra(PRINT_DATA)) {
                mPrintData = intent.getParcelableExtra(PRINT_DATA);
            }
        }
        if (isOpenBluetooth()) {
            if (model != MODEL.TEST && isEmptyPrintData()) {
                // 打印数据为空
                Toast.makeText(getContext(), "打印数据不能为空", Toast.LENGTH_LONG).show();
            } else {
                if (mDeviceManager.isConnected()) {
                    // 已连接
                    print();
                } else {
                    // 开始连接打印机
                    connectionPrint();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 连接打印机
    private void connectionPrint() {
        ThreadPool.getInstantiation()
            .addTask(mThreadFactoryBuilder.newThread(new Runnable() {
                @Override public void run() {
                    mDeviceManager.connection(mBluetoothAddress);
                }
            }));
    }

    // 检查蓝牙设备
    private boolean isOpenBluetooth() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (mBluetoothAdapter == null) { // 设备不支持蓝牙
            Toast.makeText(this, "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        } else if (mBluetoothAdapter.isEnabled()) { // 开启蓝牙
            return true;
        } else {
            // 蓝牙未开启
            Toast.makeText(getContext(), "请开启设备蓝牙", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private BroadcastReceiver mDeviceStateReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                //蓝牙连接断开广播
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    new Handler().obtainMessage(CONN_STATE_DISCONNECTION).sendToTarget();
                    break;
                case PrintDeviceManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(PrintDeviceManager.STATE, -1);
                    switch (state) {
                        case PrintDeviceManager.CONN_STATE_DISCONNECT:
                            Log.i(TAG, "连接状态：未连接");
                            Toast.makeText(getContext(), "打印机未连接，请检查打印机是否开启", Toast.LENGTH_LONG)
                                .show();
                            break;
                        case PrintDeviceManager.CONN_STATE_CONNECTING:
                            Log.i(TAG, "连接中...");
                            break;
                        case PrintDeviceManager.CONN_STATE_CONNECTED:
                            Log.i(TAG, "连接状态：已连接");
                            // 连接成功，开始打印
                            print();
                            break;
                        case PrintDeviceManager.CONN_STATE_FAILED:
                            Log.e(TAG, "连接失败！");
                            Toast.makeText(getContext(), "打印机连接失败", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // 打印
    private void print() {
        if (!mDeviceManager.isConnected()) {
            Toast.makeText(getContext(), "请先设置打印机", Toast.LENGTH_LONG).show();
            return;
        }
        ThreadPool.getInstantiation().addTask(new Runnable() {
            @Override public void run() {
                if (mDeviceManager.getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    if (model == MODEL.TEST) {
                        mDeviceManager.sendDataImmediately(getTestData());
                    } else {
                        mDeviceManager.sendDataImmediately(mPrintData.getDatas());
                    }
                } else {
                    new Handler().obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    // 测试数据
    private Vector<Byte> getTestData() {
        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addPrintAndFeedLines((byte) 3);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON,
            EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        // 打印文字
        esc.addText("打印测试");
        esc.addPrintAndLineFeed();
        // 开钱箱
        esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
        esc.addPrintAndFeedLines((byte) 8);
        // 加入查询打印机状态，打印完成后，此时会接收到GpCom.ACTION_DEVICE_STATUS广播
        esc.addQueryPrinterStatus();
        return esc.getCommand();
    }

    private Context getContext() {
        return getApplicationContext();
    }

    private boolean isEmptyPrintData() {
        return mPrintData == null || mPrintData.isEmpty();
    }
}
