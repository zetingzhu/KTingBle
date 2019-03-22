package com.ble.blelibrary.callback;


/**
 * 蓝牙读取数据操作
 */
public interface BleReadCallback  {

    void onReadSuccess(byte[] data);

    void onReadFailure(Exception exception);

    /**
     * 获取1s刷新电门锁车状态
     * @param scState 返回的数据长度
     * @param stateByte 电门，锁车状态
     */
    void getDmScState(int scState, byte stateByte) ;

    /** 读取cmdsn号码 */
    void getCmdSn(Integer sn);
}
