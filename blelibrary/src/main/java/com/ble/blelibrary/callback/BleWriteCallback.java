package com.ble.blelibrary.callback;



public abstract class BleWriteCallback {

    public abstract void onWriteSuccess(int current, int total, byte[] justWrite);

    public abstract void onWriteFailure( Exception exception);

}
