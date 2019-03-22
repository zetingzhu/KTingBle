package com.ble.blelibrary.callback;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * 蓝牙连接操作类
 * Created by zeting
 * Date 19/2/26.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class BleGattCallback extends BluetoothGattCallback {

    /**
     *  开始连接蓝牙
     */
    public abstract void onStartConnect();

    /**
     *  蓝牙连接失败
     */
    public abstract void onConnectFail(String str);

    /**
     *  蓝牙连接成功
     * @param gatt
     * @param status
     */
    public abstract void onConnectSuccess(BluetoothGatt gatt, int status);

    /**
     * 断开蓝牙连接
     * @param gatt
     * @param status
     */
    public abstract void onDisConnected( BluetoothGatt gatt, int status);

}