package com.hx.infusionchairplateproject.tools

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Environment
import android.text.TextUtils
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
         * 判断某个Activity在栈顶
         */
        fun isActivityTop(context: Context, activityClass: Class<*>): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks = activityManager.getRunningTasks(1)
            if (tasks.isNotEmpty()) {
                val topActivity = tasks[0].topActivity
                if (topActivity!!.className == activityClass.name) {
                    return true
                }
            }
            return false
        }

        /**
         * 以列表的形式返回指定目录中的所有文件
         *  /sdcard/TripartiteApp/
         *  /storage/emulated/0/sdcard/TripartiteApp/
         */
        fun getFileNamesInDirectory(directoryPath: String): List<String> {
            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) {
                println("Directory does not exist or is not a directory.")
                return emptyList()
            }
            return directory.listFiles()?.map { it.name } ?: emptyList()
        }

        /**
         * 判断一个服务是否正在运行
         */
        fun isServiceRunning(context: Context, serviceName: String?): Boolean {
            if (TextUtils.isEmpty(serviceName)) {
                return false
            }
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            // 最多获取 200 个正在运行的 Service
            val infos = activityManager.getRunningServices(200)

            // 遍历当前运行的 Service 信息, 如果找到相同名称的服务 , 说明某进程正在运行
            for (info in infos) {
                if (TextUtils.equals(info.service.className, serviceName)) {
                    return true
                }
            }
            return false
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