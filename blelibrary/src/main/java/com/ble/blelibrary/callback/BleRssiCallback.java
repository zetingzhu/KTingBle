package com.ble.blelibrary.callback;



public interface BleRssiCallback {

  void onRssiFailure(Exception exception);

    void onRssiSuccess(int rssi);

}