package com.ble.blelibrary.callback;

import android.bluetooth.le.ScanResult;

/**
 * 扫码蓝牙接口
 * Created by zeting
 * Date 19/2/26.
 */

public interface DeviceCallback {
    /*
      * 扫描
      * */
    void onScanResult(ScanResult result);

    void onScanTimeOut();
}
