package com.ble.blelibrary.blueutil;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;


import com.ble.blelibrary.callback.BleErrorImp;
import com.ble.blelibrary.callback.BleGattCallback;
import com.ble.blelibrary.callback.BleReadCallback;
import com.ble.blelibrary.callback.BleRssiCallback;
import com.ble.blelibrary.callback.BleWriteCallback;
import com.ble.blelibrary.callback.DeviceCallback;

import java.util.UUID;

/**
 * ble蓝牙操作工具类
 * Created by zeting
 * Date 19/2/25.
 */

public class BleManager {
    private static final String TAG = "BleManager";
    // 单例蓝牙对象
    private static volatile BleManager bleManager;
    // 上下文对象
    private Application context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private BleErrorImp mBleErrorImp ;
    // 连接的蓝牙的操作类
    private BleBluetooth bleBluetooth ;

    /**
     * 创建单例模式单例模式
     */
    public static BleManager getInstance() {
        if (bleManager == null) {
            synchronized (BleManager.class) {
                if (bleManager == null) {
                    bleManager = new BleManager();
                }
            }
        }
        return bleManager;
    }

    public void initErrorImp(BleErrorImp bleErrorImp){
        mBleErrorImp = bleErrorImp ;
    }

    public void init(Application app) {
        if (context == null && app != null) {
            context = app;
            if (isSupportBle()) {
                bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            }
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

    /**
     * 获取蓝牙适配器
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    /**
     * 获取连接上的蓝牙的bluegatt
     * @return
     */
    public BleBluetooth getBleBluetooth() {
        return bleBluetooth;
    }


    public Context getContext() {
        return context;
    }


    /**
     *  扫描ble蓝牙
     * @param callback
     */
    public void scan(DeviceCallback callback){
        scan( callback , -1 ) ;
    }

    public void scan(DeviceCallback callback , long scanTimeout ){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleScanner.getInstance().initErrorImp(mBleErrorImp);
            BleScanner.getInstance().init(bluetoothAdapter);
            BleScanner.getInstance().setScanTimeout(scanTimeout);
            BleScanner.getInstance().startScanDevice(callback);
        }
    }


    /**
     * 停止扫描蓝牙
     */
    public void cancelScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleScanner.getInstance().stopLeScan();
        }
    }


    /**
     *  连接蓝牙
     * @param bleDevice
     * @return
     */
    public BleBluetooth connect(BluetoothDevice bleDevice , BleGattCallback bleGattCallback ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleDevice == null || bleDevice.getAddress() == null) {
                Log.d(TAG, "设备信息错误");
            } else {
                if ( bleBluetooth == null ) {
                    bleBluetooth = new BleBluetooth(bluetoothAdapter);
                }
                String address = bleDevice.getAddress() ;
                BluetoothGatt connectBlueGatt = bleBluetooth.connect( address , bleGattCallback);
                return bleBluetooth;
            }
        }
        return null ;
    }

    /**
     * 断开设备连接
     * @return
     */
    public void disconnect( ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleBluetooth != null) {
                bleBluetooth.disconnect();
            }
        }
    }

    /**
     * 获取特征值
     * @param serviceUUID
     * @param characteristicUUID
     * @return
     */
    public BluetoothGattCharacteristic getServiceCharacteristic(UUID serviceUUID, UUID characteristicUUID){
        BluetoothGattCharacteristic mCharacteristic ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleBluetooth != null) {
                mCharacteristic = bleBluetooth.getServiceUUID(serviceUUID , characteristicUUID ) ;
                Log.w(TAG , "蓝牙 获取特征值信息 - 成功获取");
                return mCharacteristic ;
            }
        }
        Log.w(TAG , "蓝牙 获取特征值信息错误");
        return null ;
    }

    /**
     * 读取特征值信息
     */
    public void readCharacteristic(BleReadCallback bleReadCallback ){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleBluetooth != null) {
                bleBluetooth.readCharacteristic(bleReadCallback);
            }else {
                if (bleReadCallback != null) {
                    bleReadCallback.onReadFailure(new Exception("蓝牙未连接"));
                }
            }
        }
    }

    /**
     * 向特征值中写入数据
     */
  public void writeCharacteristic(byte[] data, BleWriteCallback bleWriteCallback ){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleBluetooth != null) {
                bleBluetooth.writeCharacteristic(data , bleWriteCallback);
            }else {
                if (bleWriteCallback != null) {
                    bleWriteCallback.onWriteFailure(new Exception("蓝牙未连接"));
                }
            }
        }
    }


    /**
     * 读取RSSI值
     * @param callback
     */
    public void readRssi(BleRssiCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (callback == null) {
                throw new IllegalArgumentException("BleRssiCallback can not be Null!");
            }
            if (bleBluetooth == null) {
                callback.onRssiFailure(new Exception("这个蓝牙连接信息错误"));
            } else {
                bleBluetooth.readRemoteRssi(callback);
            }
        }
    }

    /**
     * 判断当前Android设备是否支持BLE。
     *
     * @return
     */
    public boolean isSupportBle() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }


    /**
     * 打开蓝牙
     */
    public void enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled())
                bluetoothAdapter.disable();
        }
    }


    /**
     * 判断蓝牙是否打开
     *
     * @return
     */
    public boolean isBlueEnable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }



}
