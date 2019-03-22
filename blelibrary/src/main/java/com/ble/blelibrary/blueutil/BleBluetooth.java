package com.ble.blelibrary.blueutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;


import com.ble.blelibrary.callback.BleGattCallback;
import com.ble.blelibrary.callback.BleReadCallback;
import com.ble.blelibrary.callback.BleRssiCallback;
import com.ble.blelibrary.callback.BleWriteCallback;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 蓝牙连接读取操作类
 * Created by zeting
 * Date 19/2/26.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleBluetooth {
    private static final String TAG = "BleBluetooth";
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mCharacteristic;
    /**蓝牙连接监听*/
    private BleGattCallback bleGattCallback;
    /**蓝牙读取rssi值接口*/
    private BleRssiCallback bleRssiCallback ;
    /**读取数据接口**/
    private BleReadCallback bleReadCallback ;
    // 设备是否连接
    private boolean isActiveConnect = false;
    // 是否需要自动连接，如果是自动断开的需要自动连接，手动断开的不需要自动连接
    private boolean isAutoConnect = false;
    // 连接蓝牙的地址
    private String blueAddressConnect = "" ;

    public BleBluetooth(  BluetoothAdapter blueAdapter ) {
        mBluetoothAdapter = blueAdapter ;
    }


    /**
     * 获取 Bluetooth GATT 。例如重新连接蓝牙设备，发现蓝牙设备的 Service 等等。
     * @return
     */
    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    /**
     * 获取gatt service
     * @return
     */
    public BluetoothGattService getGattService(){
        return mGattService ;
    }

    /**
     * 获取特征值
     * @return
     */
    public BluetoothGattCharacteristic getGattCharacteristic(){
        return mCharacteristic ;
    }

    /**
     * 设备蓝牙操作状态接口
     * @param callback
     */
    public synchronized void addConnectGattCallback(BleGattCallback callback) {
        bleGattCallback = callback;
    }

    /**
     * 添加读取rssi接口
     * @param callback
     */
    public synchronized void addRssiCallback(BleRssiCallback callback) {
        bleRssiCallback = callback;
    }

    /**
     * 添加读取数据接口
     * @param callback
     */
    public synchronized void addReadCharaCallback(BleReadCallback callback) {
        bleReadCallback = callback;
    }

    /**
     * 是否连接蓝牙成功
     * @return
     */
    public boolean isConnectBlue(){
        return isActiveConnect ;
    }

    public synchronized BluetoothGatt connect(String address , BleGattCallback callback ) {
        if (address == null){
            return null;
        }

        if (blueAddressConnect.equals(address) && isConnectBlue() ){
            Log.i(TAG , "蓝牙设备已连接成功" );
            return null ;
        }
        blueAddressConnect = address ;

        // 移除所有监听
        removeCallback();
        /**连接蓝牙*/
        if (mBluetoothGatt != null ){
            Log.d(TAG , "再次连接蓝牙的时候已经有了原来的对象 " );
            disconnectGatt();
            refreshDeviceCache();
        }
        addConnectGattCallback(callback);

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            if (bleGattCallback != null) {
                bleGattCallback.onConnectFail("获取蓝牙设备信息错误");
            }
        }
        // 连接的时候设置如果异常断开了就自动连接上
        isAutoConnect = true ;

        mBluetoothGatt = device.connectGatt( BleManager.getInstance().getContext() , false, mGattCallback);
        if (mBluetoothGatt != null) {
            if (bleGattCallback != null) {
                bleGattCallback.onStartConnect();
            }
        } else {
            disconnectGatt();
            closeBluetoothGatt();
            if (bleGattCallback != null) {
                bleGattCallback.onConnectFail("连接蓝牙失败");
            }
        }
        return mBluetoothGatt;
    }


    /**
     *  根据uuid 获取特征值
     * @param serviceUUID
     * @param characteristicUUID
     */
    public BluetoothGattCharacteristic getServiceUUID(UUID serviceUUID, UUID characteristicUUID) {
        if (serviceUUID != null && mBluetoothGatt != null) {
            mGattService = mBluetoothGatt.getService(serviceUUID);
        }
        if (mGattService != null && characteristicUUID != null) {
            mCharacteristic = mGattService.getCharacteristic(characteristicUUID);
        }
        return mCharacteristic;
    }



    /**
     * 读取特征值
     */
    public void readCharacteristic(BleReadCallback bleReadCallback ) {
        if (mCharacteristic != null ) {
            // 添加接口道蓝牙读取操作类中
            addReadCharaCallback(bleReadCallback);
            if (!mBluetoothGatt.readCharacteristic(mCharacteristic)) {
                if (bleReadCallback != null) {
                    bleReadCallback.onReadFailure(new Exception("gatt readCharacteristic fail"));
                }
            }
        } else {
            if (bleReadCallback != null)
                bleReadCallback.onReadFailure(new Exception("this characteristic not support read!"));
        }
    }


    /**
     * 写入命令
     */
    public void writeCharacteristic(byte[] data, BleWriteCallback bleWriteCallback ) {
        if (data == null || data.length <= 0) {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new  Exception("the data to be written is empty"));
            return;
        }

        if (mCharacteristic == null ){
            if (bleWriteCallback != null) {
                bleWriteCallback.onWriteFailure(new  Exception("this characteristic not support write!"));
            }
            return;
        }
        mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        if (mCharacteristic.setValue(data)) {
            if (!mBluetoothGatt.writeCharacteristic(mCharacteristic)) {
                if (bleWriteCallback != null)
                    bleWriteCallback.onWriteFailure(new  Exception("gatt writeCharacteristic fail"));
            }
        } else {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new  Exception("Updates the locally stored value of this characteristic fail"));
        }
    }


    /**
     * rssi
     */
    public void readRemoteRssi(BleRssiCallback bleRssiCallback) {
        if (isConnectBlue()) {
            // 添加接口道蓝牙读取操作类中
            addRssiCallback(bleRssiCallback);
            if (mBluetoothGatt != null ) {
                if (!mBluetoothGatt.readRemoteRssi()) {
                    if (bleRssiCallback != null) {
                        bleRssiCallback.onRssiFailure(new Exception("读RSSI取信息错误"));
                    }
                }
            }
        }else {
            if (bleRssiCallback != null) {
                bleRssiCallback.onRssiFailure(new Exception("设备没有连接"));
            }
        }
    }


    /**
     * 调用此处断开蓝牙连接
     */
    public synchronized void disconnect() {
        isActiveConnect = false;
        disconnectGatt();
    }

    /**
     * 断开蓝牙连接
     */
    private synchronized void disconnectGatt() {
        removeCallback();
        isAutoConnect = false ;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * 关闭蓝牙连接
     */
    private synchronized void closeBluetoothGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }

    /**
     * 移除设备信息
     */
    private synchronized void refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && mBluetoothGatt != null) {
                boolean success = (Boolean) refresh.invoke(mBluetoothGatt);
                Log.i(TAG , "refreshDeviceCache, is success:  " + success);
            }
        } catch (Exception e) {
            Log.i(TAG , "exception occur while refreshing device: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * 断开连接的时候移除各种监听
     */
    public void removeCallback(){
        // 移除 监听状态
        if (bleGattCallback != null){
            bleGattCallback = null ;
        }
        // 移除 监听rssi
        if (bleRssiCallback != null){
            bleRssiCallback = null ;
        }
        // 移除 读取数据获取sn号码
        if (bleReadCallback != null ) {
            bleReadCallback = null ;
        }

    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            mBluetoothGatt = gatt ;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "蓝牙状态改变连接成功.");
                boolean discoverServiceResult = mBluetoothGatt.discoverServices();
                Log.i(TAG, "读取蓝牙状态:" + discoverServiceResult );
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "蓝牙状态改变断开连接.");
                if (bleGattCallback != null) {
                    bleGattCallback.onDisConnected( gatt, status);
                }
                if (isAutoConnect){
                    mBluetoothGatt.connect();
                }else {
                    mBluetoothGatt.close();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "蓝牙连接 成功");
                isActiveConnect = true ;
                if (bleGattCallback != null) {
                    bleGattCallback.onConnectSuccess( gatt  , status);
                }
            } else {
                Log.i(TAG, "蓝牙连接 失败");
                isActiveConnect = false ;
                if (bleGattCallback != null) {
                    bleGattCallback.onConnectFail( "蓝牙连接 失败" );
                }
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            byte[] mKeyByte = new byte[2];
            System.arraycopy(characteristic.getValue(), 5, mKeyByte, 0, 2);
            int snStr1 = BleByteUtil.byteHexToInt(mKeyByte);
            if (bleReadCallback != null ) {
                bleReadCallback.getCmdSn(snStr1);
            }
            if (status == BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG,
                        "onCharRead "
                                + gatt.getDevice().getName()
                                + " read "
                                + characteristic.getUuid().toString()
                                + " -> "
                                + BleByteUtil.bytesToHex2(characteristic.getValue()));
                // 4 为锁车状态 ， 7，电门状态
                // 锁车状态
//                    byte byteSCState =  characteristic.getValue()[4];
//                    int LockState = byteSCState ;
                // 数据长度
                byte byteSCState =  characteristic.getValue()[3];
                int longState = byteSCState ;
                // 电门，锁车 状态
                byte byteState =  characteristic.getValue()[8];
                Log.d(TAG, "得到的状态 电门，锁车:" +  BleByteUtil.getBit( byteState )  + "--请求cmdsn:" + snStr1 +" - 得到的cmdsn:"+ snStr1  );

                if (bleReadCallback != null ){
                    // 每一次刷新数据获取电门锁车数据状态
                    bleReadCallback.getDmScState(longState , byteState );
                }
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }



        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(TAG , "读取到的设备信息：" + rssi );
            if (bleRssiCallback != null) {
                bleRssiCallback.onRssiSuccess(rssi);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

    };
}
