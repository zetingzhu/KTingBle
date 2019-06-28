package com.kting.ble.ktingble;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.ble.blelibrary.blueutil.BleManager;
import com.ble.blelibrary.callback.BleGattCallback;
import com.ble.blelibrary.callback.BleReadCallback;
import com.ble.blelibrary.callback.BleRssiCallback;
import com.ble.blelibrary.callback.BleWriteCallback;
import com.ble.blelibrary.callback.DeviceCallback;
import com.kting.ble.util.ByteUtil;
import com.kting.ble.util.LogPlus;
import com.kting.ble.util.MD5Util;
import com.kting.ble.util.ScreenUtils;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity  implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    ConstraintLayout cl;
    private Button button1, button2, button3 , button4;

    // 获取到需要连接的设备
    BluetoothDevice bleDevice ;

    // 服务UUID
    public static String DEVICE_SERVICE_UUID = "000028af-0000-1000-8000-00805f9b34fb" ;
    // 特征值
    public static String DEVICE_CHARACTERISTIC_UUID_BLUE = "00002aa4-0000-1000-8000-00805f9b34fb" ;


    private String bleManufacturers = "" ;
    private String remoter_id = "" ;

    /**
     * 随便赋值的一个唯一标识码
     */
    public static final int PERMISSIONS_ACCESS_LOCATION=1001;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogPlus.w("屏幕宽：" + ScreenUtils.getScreenWidth());
        LogPlus.w("屏幕高：" + ScreenUtils.getScreenHeight());
        LogPlus.w("屏幕密度dp：" + ScreenUtils.getScreenDensity());
        LogPlus.w("屏幕密度dpi：" + ScreenUtils.getScreenDensityDpi());
        LogPlus.w("屏幕宽(dp)：" + ScreenUtils.getScreenWidthDp());
        LogPlus.w("屏幕高(dp)：" + ScreenUtils.getScreenHeightDp());


        BleManager.getInstance().init(getApplication());


//        bleManufacturers = "415106a2a0a2" ;// 929650132125
        bleManufacturers = "415106a276ea" ;// 9281771311057
        remoter_id = "233061273" ;//

        initView();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);


        // 扫码
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] perms = { Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION };
                if (EasyPermissions.hasPermissions( MainActivity.this , perms)) {
                    // Already have permission, do the thing
                    // 搜索
                    if (BleManager.getInstance().isBlueEnable()){
                        long timeout = 10*1000 ;
                        BleManager.getInstance().scan(new DeviceCallback() {

                            @Override
                            public void onScanResult(ScanResult result) {

                                SparseArray<byte[]> dataArray = result.getScanRecord().getManufacturerSpecificData();
                                Log.i(TAG, "onScanResult1:  " +bleManufacturers+" =" + MD5Util.bytesToHex(dataArray.valueAt(0)));
                                // 需要连接设备的厂商信息
                                String str = bleManufacturers.substring(4);
                                if (str.equalsIgnoreCase(MD5Util.bytesToHex(dataArray.valueAt(0)))) {
                                    // 找到指定设备 , 停止扫描
                                    BleManager.getInstance().cancelScan();
                                    LogPlus.w("找到蓝牙设备，停止扫描");
                                    // 获取查找到的设备
                                    bleDevice = result.getDevice() ;
                                }
                            }

                            @Override
                            public void onScanTimeOut() {
                                LogPlus.w("蓝牙扫描时间超时");
                            }
                        } );
                    }else{
                        LogPlus.w("蓝牙没有打开呀，请打开蓝牙");
                        BleManager.getInstance().enableBluetooth();
                    }
                } else {
                    // Do not have permissions, request them now
                    EasyPermissions.requestPermissions( MainActivity.this, "需要定位权限" , PERMISSIONS_ACCESS_LOCATION, perms);
                }

            }
        });
        // 连接
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bleDevice != null) {
                    BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
                        @Override
                        public void onStartConnect() {
                            LogPlus.w("----- 蓝牙 开始连接");
                        }

                        @Override
                        public void onConnectFail(String str) {

                            LogPlus.w("----- 蓝牙 开始失败" + str );
                        }

                        @Override
                        public void onConnectSuccess(BluetoothGatt gatt, int status) {

                            LogPlus.w("----- 蓝牙 连接成功");
                        }

                        @Override
                        public void onDisConnected(BluetoothGatt gatt, int status) {
                            LogPlus.w("----- 蓝牙 断开连接");
                        }
                    });
                }
            }
        });
        // 读取Rssi
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().readRssi( new BleRssiCallback() {


                    @Override
                    public void onRssiFailure(Exception exception) {
                        LogPlus.w("----- 蓝牙 RSSI 错误：" + exception.getMessage() );
                    }

                    @Override
                    public void onRssiSuccess(int rssi) {
                        LogPlus.w("----- 蓝牙 RSSI ：" + rssi );
                    }
                });
            }
        });
        // 断开
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().disconnect();
            }
        });

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothGattCharacteristic bgc = BleManager.getInstance().getServiceCharacteristic(UUID.fromString( DEVICE_SERVICE_UUID ) , UUID.fromString( DEVICE_CHARACTERISTIC_UUID_BLUE ) );
                if (bgc == null ){
                    LogPlus.w(TAG, "----- 蓝牙 读取特征值信息: 为空");
                }else {
                    LogPlus.w(TAG, "----- 蓝牙 读取特征值信息:" + bgc);
                }
            }
        });

        // 读取特征值
        findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().readCharacteristic(new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        LogPlus.w("----- 蓝牙 读取信息 成功" );
                    }

                    @Override
                    public void onReadFailure(Exception exception) {
                        LogPlus.w("----- 蓝牙 读取信息 错误：" + exception.getMessage() );
                    }

                    @Override
                    public void getDmScState(int scState, byte stateByte) {
                        LogPlus.w(TAG , "----- 蓝牙 获取数据长度:" + scState + "电门状态" + ByteUtil.getBit( stateByte ) );
                    }

                    @Override
                    public void getCmdSn(Integer sn) {
                        LogPlus.w(TAG , "----- 蓝牙 获取CMDsn:" + sn);
                        cmdsn = sn ;
                    }
                });
            }
        });

        // 写入数据
        findViewById(R.id.button7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Byte op = (byte) 0xC2 ;
                Byte cl  = (byte) 0xC1 ;
                if (oldCmd == op ){
                    oldCmd = cl ;
                }else {
                    oldCmd = op ;
                }

                BleManager.getInstance().writeCharacteristic(writeData( oldCmd ), new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        LogPlus.w("----- 蓝牙 写入信息 成功" );
                    }

                    @Override
                    public void onWriteFailure(Exception exception) {
                        LogPlus.w("----- 蓝牙 写入信息 错误：" + exception.getMessage() );
                    }
                });
            }
        });


    }
    private Integer cmdsn = 0 ;
    private byte oldCmd  ;

    /**
     * 0xC1 关
     * 0xC2 开
     * @param bt
     * @return
     */
    public  byte[]  writeData(byte bt   ){
        //蓝牙连接 写入wmd5值 模拟遥控器id：588677840-写入的sn:32288

            byte[] wmd5 =  writeData( bt , remoter_id , bleManufacturers ) ;
        // 蓝牙连接 写入wmd5值：c27e210000000000000000001416f77f69a2322b
            Log.e(TAG, "蓝牙连接 写入wmd5值：" + ByteUtil.bytesToHex2( wmd5 ) );
        return wmd5 ;
    }

    /**
     *  组成类似遥控器的开锁，锁车命令
     * 0xC1 锁车
     * 0xC2 解锁
     * @param bt
     * @return
     *
     *  1009099844
     *  41510695d17e
     */
    public byte[] writeData(byte bt , String keyid , String bleManuft ){
        byte[] writeByte = new byte[20];

        writeByte[0] = bt ;
        byte [] cmdSnByte = ByteUtil.intToByteHex(cmdsn + 1);
        System.arraycopy(cmdSnByte, 0, writeByte, 1, 2);

        writeByte[3] = 0x00 ;
        byte[] dataByte = new byte[8];

        System.arraycopy(dataByte, 0, writeByte, 4, 8);

//        Log.w(TAG, "没有验证md5的时候" + ByteUtil.bytesToHex2(  writeByte ) );

        // md5 验证 需要验证
        //前 12 字节数据 +
        byte[] byte12 = new byte[12] ;
        System.arraycopy(writeByte , 0 ,byte12 , 0 , 12);

        // 遥控器 ID +
        byte[] keyId = ByteUtil.intToByteArrayLittel(Integer.valueOf(keyid))  ;

        // 需配对的产商信息+
        String mHex = "41510695d17e" ;
        byte[] manufByte = ByteUtil.hexToByteArray(bleManuft) ;

        //固定 秘钥("tddfxjoA6wkAqnjPPCUxHtQT")
        byte[] byteStr = "tddfxjoA6wkAqnjPPCUxHtQT".getBytes() ;

        // 组合所有的byte数组
        byte[] byteMd5L = byteMerger(byte12 , keyId  , manufByte , byteStr) ;

//        Log.w(TAG, "没有验证md5的时候 - 加密前：" + ByteUtil.bytesToHex2(  byteMd5L ) );
        // md5 加密后的byte
        byte[] md5DataDigest = MD5Util.getMD5Byte(byteMd5L);
//        Log.w(TAG, "没有验证md5的时候 - 加密后：" + ByteUtil.bytesToHex2(  md5DataDigest ) );
        // md5 截取验证的字符串
        byte[] md5Byte = new byte[8];
        System.arraycopy(md5DataDigest, 4 , md5Byte, 0, 8);
        // 将MD5 数组放入到数据中
        System.arraycopy(md5Byte, 0, writeByte, 12, 8);

//        return  ByteUtil.bytesToHex2(writeByte) ;
        return  writeByte ;
    }

    /**
     * 将多个数组组成一个
     * @param byteN
     * @return
     */
    public static byte[] byteMerger(byte[]... byteN ){
        int byteLength = 0 ;
        for (int i = 0; i <byteN.length ; i++) {
            byteLength += byteN[i].length ;
        }
        Log.i(TAG, "所有数组长度：" + byteLength);

        byte[] byteNew = new byte[byteLength];

        int byteIndex = 0 ;
        for (int j = 0; j < byteN.length ; j++) {
            byte[] bInd = byteN[j] ;
            System.arraycopy(bInd, 0, byteNew, byteIndex, bInd.length);
            byteIndex += bInd.length ;
        }
        return byteNew;
    }
    public void getAndroiodScreenProperty() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        int height = dm.heightPixels; // 屏幕高度（像素）
        float density = dm.density;//屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;//屏幕密度dpi（120 / 160 / 240）
        //屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);//屏幕宽度(dp)
        int screenHeight = (int) (height / density);//屏幕高度(dp)
        LogPlus.e("123", screenWidth + "======" + screenHeight);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // 此处表示权限申请已经成功，可以使用该权限完成app的相应的操作了
        Log.e(TAG , "同意了 权限申请" );
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // 此处表示权限申请被用户拒绝了，此处可以通过弹框等方式展示申请该权限的原因，以使用户允许使用该权限

        //(可选的)检查用户是否拒绝授权权限，并且点击了“不再询问”（测试如果不点击 不再询问也会调用这个方法，所以只要拒绝就会调用这个方法）
        //下面的语句，展示一个对话框指导用户在应用设置里授权权限
        Log.e(TAG , "拒绝了 权限申请" );
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle("申请权限")
                    .setRationale("应用需要这个权限")
                    .build()
                    .show();
            Log.e(TAG , "引导设置 申请权限" );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            // 当用户从应用设置界面返回的时候，可以做一些事情，比如弹出一个土司。
            Toast.makeText(this, "权限设置界面返回" , Toast.LENGTH_SHORT).show();
        }

    }
}
