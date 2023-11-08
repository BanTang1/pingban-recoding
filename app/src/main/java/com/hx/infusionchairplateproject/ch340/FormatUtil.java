package com.hx.infusionchairplateproject.ch340;

import android.hardware.usb.UsbDevice;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * CH340 配套工具类
 */
public class FormatUtil {

    public static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }

        return builder.toString();
    }

    public static String bytesToHexString(byte[] src,int length) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0 || length>src.length) {
            return null;
        }
        String hv;
        int min = Math.min(src.length, length);
        for (int i = 0; i < min; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }

        return builder.toString();
    }

    /**
     * 将Hex String转换为Byte数组
     *
     * @param hexString the hex string
     * @return the byte [ ]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString==null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index  > hexString.length() - 1) {
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }

    public static String getReadBufferLogPrefix(@NonNull UsbDevice usbDevice, int serialNumber,int count){
        return String.format(Locale.getDefault(),"%s(串口%d)(总计%d字节):",usbDevice.getDeviceName(),serialNumber,count);
    }

    public static String getSerialKey(@NonNull UsbDevice device,int serialNumber){
        return String.format(Locale.getDefault(),"%s_%d",device.getDeviceName(),serialNumber);
    }
}
