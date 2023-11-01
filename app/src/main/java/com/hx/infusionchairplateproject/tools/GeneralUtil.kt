package com.hx.infusionchairplateproject.tools

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Environment
import com.hx.infusionchairplateproject.R
import com.yzq.zxinglibrary.encode.CodeCreator
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GeneralUtil {

    companion object {
        /**
         * 检测网络是否可用
         * 判断当前是否有网络连接,但是如果该连接的网络无法上网，也会返回true
         *
         * @return
         */
        fun isNetWorkConnected(context: Context?): Boolean {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable
                }
            }
            return false
        }

        /**
         * 根据设备 SN 码生成二维码图片
         * 此处可设置 生成的二维码图片大小
         */
        @SuppressLint("UseCompatLoadingForDrawables")
        fun getTwoDimensionalMap(sn: String, context: Context): Bitmap {
            val drawable = context.resources.getDrawable(R.mipmap.llm_app_icon_playstore)
            val bitmapDrawable = drawable as BitmapDrawable
            val tmp = bitmapDrawable.bitmap
            return CodeCreator.createQRCode(sn + "text=hx", 350, 350, tmp)
        }

        /**
         * 获取当前WIFI的信号强度
         *
         */
        fun getWifiRssi(context: Context): Int {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            return wifiInfo.rssi
        }

        /**
         * 将字符串信息写入文件
         *  路径 ： /storage/emulated/0/hx/pad/webSocket.text
         *         /sdcard/hx/pad/webSocket.text
         */
        fun writeToFile(content: String) {
            val ymdhms = "yyyy-MM-dd HH:mm:ss"
            val ww = SimpleDateFormat(ymdhms, Locale.CHINA)
            ww.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            val format = ww.format(Date())

            val text = "时间:$format===>webSocket -> $content\n"
            val absolutePath = File(Environment.getExternalStorageDirectory(), "hx/pad/webSocket.text").absolutePath

            realWriteFile(absolutePath, text)
        }


        /**
         * 文件大于 100M ，则清空， 之后再次写入
         */
        private fun realWriteFile(path: String, content: String) {
            val file = File(path)
            if (file.exists() && file.length() > 100L * 1024 * 1024) {
                file.delete()
                Files.createFile(Paths.get(path))
            } else if (!file.exists()) {
                Files.createFile(Paths.get(path))
            }
            Files.write(Paths.get(path), content.toByteArray(), StandardOpenOption.APPEND)

        }
    }
}