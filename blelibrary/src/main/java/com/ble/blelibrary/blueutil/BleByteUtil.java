package com.ble.blelibrary.blueutil;

/**
 * Created by zeting
 * Date 18/12/5.
 */

public class BleByteUtil {


    public static String bytesToHex2(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 数组转换成十六进制，在转换成int
     * @return
     */
    public static Integer byteHexToInt(byte[] bb){
        String sssss =  BleByteUtil.bytesToHex2(bb);
        //将十六进制转化成十进制
        int valueTen = Integer.parseInt(sssss,16);
        return valueTen ;
    }

    public static String getBit(byte by){
        StringBuffer sb = new StringBuffer();
        sb.append((by>>7)&0x1)
                .append((by>>6)&0x1)
                .append((by>>5)&0x1)
                .append((by>>4)&0x1)
                .append((by>>3)&0x1)
                .append((by>>2)&0x1)
                .append((by>>1)&0x1)
                .append((by>>0)&0x1);
        return sb.toString();
    }

}
