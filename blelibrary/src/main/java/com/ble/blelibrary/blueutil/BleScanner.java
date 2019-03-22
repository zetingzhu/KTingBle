package com.ble.blelibrary.blueutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;


import com.ble.blelibrary.callback.BleErrorImp;
import com.ble.blelibrary.callback.DeviceCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙扫码类
 * Created by zeting
 * Date 19/2/26.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleScanner {
    // 单例蓝牙对象
    private static volatile BleScanner bleScanner;
    // 蓝牙扫描
    private BluetoothLeScanner scanner;
    private List<ScanFilter> bleScanFilters;
    private ScanSettings scanSettings;

    private DeviceCallback mDeviceCallback ;

    /**
     *  错误信息接口
     */
    private BleErrorImp mBleErrorImp ;
    // 扫描超时时间
    private long scanTimeout = -1 ;

    /**
     * 创建单例模式单例模式
     */
    public static BleScanner getInstance() {
        if (bleScanner == null) {
            synchronized (BleScanner.class) {
                if (bleScanner == null) {
                    bleScanner = new BleScanner();
                }
            }
        }
        return bleScanner;
    }

    public void setScanTimeout(long scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

    public void initErrorImp(BleErrorImp bleErrorImp){
        mBleErrorImp = bleErrorImp ;
    }

    public void init(BluetoothAdapter bluetoothAdapter) {
        bleScanFilters = new ArrayList<>();
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        scanner = bluetoothAdapter.getBluetoothLeScanner();
    }


    /**
     * 开始扫描蓝牙设备
     */
    public synchronized void startScanDevice( DeviceCallback callback ) {
        if (scanner != null ) {
            if (mScanCallback != null) {
                scanner.stopScan(mScanCallback);
            }
            if (mDeviceCallback != null) {
                mDeviceCallback = null;
            }
            mDeviceCallback = callback;
            scanner.startScan(bleScanFilters, scanSettings, mScanCallback);
            scanTimeout();
        }else {
            if (mBleErrorImp !=null ){
                mBleErrorImp.bleErrorMsg("初始化蓝牙扫描失败");
            }
        }
    }

    /**
     * 设置扫描超时时间
     */
    public void scanTimeout(){
        if (scanTimeout != -1) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mDeviceCallback != null ) {
                        mDeviceCallback.onScanTimeOut();
                        stopLeScan();
                    }
                }
            }, scanTimeout ) ;
        }
    }

    /**
     * 停止扫码蓝牙
     */
    public synchronized void stopLeScan() {
        if (scanner != null ) {
            scanner.stopScan(mScanCallback);
        }
    }


    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (mDeviceCallback != null ) {
                mDeviceCallback.onScanResult(result);
            }
        }
    };
}
