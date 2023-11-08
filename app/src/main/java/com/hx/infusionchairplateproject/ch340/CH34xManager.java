package com.hx.infusionchairplateproject.ch340;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hjq.toast.Toaster;
import com.hx.infusionchairplateproject.EntiretyApplication;
import com.hx.infusionchairplateproject.tools.SPTool;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import cn.wch.uartlib.WCHUARTManager;
import cn.wch.uartlib.callback.IDataCallback;
import cn.wch.uartlib.callback.IUsbStateChange;
import cn.wch.uartlib.exception.ChipException;
import cn.wch.uartlib.exception.NoPermissionException;
import cn.wch.uartlib.exception.UartLibException;

/**
 * CH340 芯片通信管理类
 */
public class CH34xManager {

    private UsbDevice usbDevice;
    private static CH34xManager ch34xManager;
    private static UsbManager usbManager;
    private String dataCallbackResult = "NULL";

    private static final String SEND_OPEN = "48010B0101010101015B58";
    private static final String RECV_OPEN = "48010B0201010101015B58";
    private static final String SEND_CLOSE = "48010B0101010101025B58";
    private static final String RECV_CLOSE = "48010B0201010101025C58";
    private static final String SEND_STATE = "48010B0102005E58";
    private static final String RECV_STATE_OPEN = "48010B0202010201015D58";
    private static final String RECV_STATE_CLOSE = "48010B0202010201025E58";

    private CH34xManager() {
        // 监听USB 插拔状态， 以及用户授予权限
        monitorUSBState();
    }

    public static CH34xManager getCH34xManager() {
        if (ch34xManager == null) {
            ch34xManager = new CH34xManager();
            usbManager = (UsbManager) EntiretyApplication.Companion.getContext().getSystemService(Context.USB_SERVICE);
        }
        return ch34xManager;
    }

    /**
     * 同步操作
     * 若发生意外重启，根据充电线时间是否到期决定是否继续解锁充电
     * 此操作不会通知后端
     */
    private void updateUSBTime() {
        long time = SPTool.getLong("usb_time");
        if (System.currentTimeMillis() < time) {
            sendOpen();
        } else {
            sendClose();
        }
    }

    public void openDevices() {
        try {
            ArrayList<UsbDevice> usbDeviceArrayList = WCHUARTManager.getInstance().enumDevice();
            usbDevice = usbDeviceArrayList.get(0);
            if (isConnected()) {
                showToast("当前设备已经打开");
                return;
            }

            boolean b = WCHUARTManager.getInstance().openDevice(usbDevice);
            if (b) {
                showToast("设备打开成功");
                registerDataCallback(usbDevice);
                if (setSerialParameter()) {
                    showToast("串口参数配置成功");
                    updateUSBTime();
                } else {
                    showToast("串口参数设置失败");
                }
            } else {
                showToast("打开失败");
            }

        } catch (ChipException e) {

        } catch (UartLibException e) {

        } catch (NoPermissionException e) {
            requestPermission(usbDevice);
            // 模拟点击方框勾选按钮
            Handler handler = new Handler(Looper.myLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                        Tools.excuteSuCMD("input tap 527 609");     // 点击方框勾选
                    excuteSuCMD("input tap 1375 709");    // 点击确定
                }
            }, 1000);
        } catch (Exception e) {
            showToast("请先连接设备");
        }
    }


    public void closeDevices() {
        try {
            if (WCHUARTManager.getInstance().isConnected(usbDevice)) {
                WCHUARTManager.getInstance().removeDataCallback(usbDevice);
                WCHUARTManager.getInstance().disconnect(usbDevice);
                WCHUARTManager.getInstance().close(EntiretyApplication.Companion.getContext());
            }
        } catch (Exception e) {
            // 如果没有注册过 广播 ,回调 之类的会崩溃
            // 此处捕捉不做处理
        }
    }

    /**
     * 配置串口参数
     *
     * @return
     */
    private boolean setSerialParameter() {
        int serialNumber = 0;       // USB 设备序号
        int baud = 115200;          // 波特率
        int data = 8;               // 数据位
        int stop = 1;               // 停止位
        int parity = 4;             // 校验位
        boolean flow = false;       // SPACE flow

        try {
            Boolean b = WCHUARTManager.getInstance().setSerialParameter(usbDevice, serialNumber, baud, data, stop, parity, flow);
            return b;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送16进制数据到CH340
     */
    private void sendHexData(String s) {
        if (!isConnected()) {
            return;
        }
        byte[] bytes = FormatUtil.hexStringToBytes(s);
        try {
            int ret = WCHUARTManager.getInstance().writeData(usbDevice, 0, bytes, bytes.length, 2000);
            if (ret > 0) {
                showToast("发送成功");
            } else {
                showToast("发送失败");
            }
        } catch (Exception e) {
//            throw new RuntimeException(e);
        }
    }

    /**
     * 打开充电
     */
    public void sendOpen() {
        sendHexData(SEND_OPEN);
    }

    /**
     * 关闭充电
     */
    public void sendClose() {
        sendHexData(SEND_CLOSE);
    }

    /**
     * 获取充电状态
     */
    public void sendState() {
        sendHexData(SEND_STATE);
    }

    /**
     * 查询 打开/关闭/查询状态  等操作是否成功
     * 调用该方法，需在调用 打开/关闭/状态查询   !建议延时查询!
     */
    public String checkResult() {
        if (dataCallbackResult.equals(RECV_OPEN)) {
            return "open";
        } else if (dataCallbackResult.equals(RECV_CLOSE)) {
            return "close";
        } else if (dataCallbackResult.equals(RECV_STATE_OPEN)) {
            return "state is open";
        } else if (dataCallbackResult.equals(RECV_STATE_CLOSE)) {
            return "state is close";
        } else {
            return "unKnow";
        }
    }

    /**
     * 数据接收回调
     *
     * @param usbDevice
     */
    private void registerDataCallback(UsbDevice usbDevice) {
        try {
            WCHUARTManager.getInstance().registerDataCallback(usbDevice, new IDataCallback() {
                @Override
                public void onData(int serialNumber, byte[] buffer, int length) {
                    // 谁调用该方法，就在哪个线程
                    // 如果发生了线程切换，就会有消耗，产生一定的延时
                    byte[] data = new byte[length];
                    System.arraycopy(buffer, 0, data, 0, data.length);
                    dataCallbackResult = FormatUtil.bytesToHexString(data, length);
                }
            });
        } catch (Exception e) {

        }
    }

    /**
     * 申请USB使用权限
     *
     * @param usbDevice
     */
    private void requestPermission(@NonNull UsbDevice usbDevice) {
        try {
            WCHUARTManager.getInstance().requestPermission(EntiretyApplication.Companion.getContext(), usbDevice);
        } catch (Exception e) {

        }
    }

    /**
     * 监测USB的状态
     */
    private void monitorUSBState() {
        WCHUARTManager.getInstance().setUsbStateListener(new IUsbStateChange() {
            @Override
            public void usbDeviceDetach(UsbDevice device) {
                //设备移除
                showToast("设备移除");
            }

            @Override
            public void usbDeviceAttach(UsbDevice device) {
                //设备插入
                showToast("设备插入");
                openDevices();
            }

            @Override
            public void usbDevicePermission(UsbDevice device, boolean result) {
                //请求打开设备权限结果
                String s = "权限请求结果 = " + result;
                showToast(s);
                openDevices();
            }
        });
    }


    /**
     * 弹窗提示， 根据场景选择是否打开Toast弹窗
     *
     * @param s
     */
    private void showToast(String s) {
//        Toaster.showShort(s);
    }

    private void excuteSuCMD(String command) {
        String realCommand = command + "\n";
        Process process = null;
        DataOutputStream out = null;
        BufferedReader errorStream = null;
        try {
            // 请求root
            process = Runtime.getRuntime().exec("su");
            out = new DataOutputStream(process.getOutputStream());
            out.write(realCommand.getBytes(Charset.forName("utf-8")));
            out.flush();
            out.writeBytes("exit\n");
            out.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;
            while ((line = errorStream.readLine()) != null) {
                msg += line;
            }
            if (!msg.contains("Failure")) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return WCHUARTManager.getInstance().isConnected(usbDevice);
    }

    /**
     * 检测USB 是否连接
     */
    public boolean isUSBConnect() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (!deviceList.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}
