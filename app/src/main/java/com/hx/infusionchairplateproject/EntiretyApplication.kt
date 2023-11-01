package com.hx.infusionchairplateproject

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.hx.infusionchairplateproject.network.NetworkManager
import com.hx.infusionchairplateproject.tools.GeneralUtil
import com.hx.infusionchairplateproject.tools.GeneralUtil.Companion.getWifiRssi
import com.hx.infusionchairplateproject.viewmodel.LockViewModel
import com.hx.infusionchairplateproject.viewmodel.SocketViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI


class EntiretyApplication : Application() {

    val TAG = "liudehu-EntiretyApplication"

    private var debug = true

    private lateinit var snAddress:String
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var client: WebSocketClient
    private var isConnected = false

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()

//        snAddress = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        snAddress = "7726c6b1e1963a52-test"

        socketViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this).create(SocketViewModel::class.java)

        realReconnect()
        startHeartbeat()
    }

    fun getSnAddress():String{
        return snAddress
    }

    private fun realReconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000L)
            initializeWebSocket()
        }
    }

    private fun startHeartbeat() {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                if (isConnected) {
                    client.send(getSocketHeartbeatMsg())
                    delay(10000L)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initializeWebSocket() {

        client = object : WebSocketClient(URI(NetworkManager.WEBSOCKET_URL)) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                if (debug) Log.d(TAG, "onOpen: 连接成功")
                isConnected = true

                GeneralUtil.writeToFile("WebSocket is connected")
                send(getSocketFirstConnectMsg())

            }

            /**
             * @param message  {
             *  type: ""  //0.未投放  1.已投放 2.扫码成功 3.扫码失败 4.用户已拒绝本次扫码 5.锁屏解锁时间 6.扫码成功(锁屏页) 7.扫码失败(锁屏页）
             *              8.用户已拒绝本次扫码（锁屏页）9.撤机（设备是否撤机） 10.重启  11.锁屏软件升级 12. 三方应用升级   13.进入WIFI界面
             *              14. 退出WIFI界面   15. 打开充电线   16.关闭充电线    17.查看充电线状态(打开或者关闭)      18.删除三方app目录（实际效果=去服务器上下载和安装）
             *              19. 删除指定app
             * }
             */
            override fun onMessage(message: String?) {
                if (message == null) return
                if (debug) Log.d(TAG, "onMessage: message = $message")

                GeneralUtil.writeToFile(message)

                var jsonObject: JSONObject = JSONObject(message)
                val type = jsonObject.optInt("type")
                val other = jsonObject.optString("other")

                when(type) {
                    0 -> {socketViewModel.isPutIn.value = false}
                    1 -> {socketViewModel.isPutIn.value = true}
                    2 -> {
                        socketViewModel.isScan.value = socketViewModel.SCAN_STATE_OK
                        GlobalScope.launch {
                            delay(2000L)
                            socketViewModel.isScan.value = socketViewModel.SCAN_STATE_DEFAULT
                        }
                    }
                    3 -> {
                        socketViewModel.isScan.value = socketViewModel.SCAN_STATE_NO
                        GlobalScope.launch {
                            delay(2000L)
                            socketViewModel.isScan.value = socketViewModel.SCAN_STATE_DEFAULT
                        }
                    }
                }

            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                if (debug) Log.d(TAG, "onClose: 连接关闭 --> code=$code  reason=$reason  remote=$remote")
                isConnected = false
                GeneralUtil.writeToFile("WebSocket is disconnect")
                realReconnect()
            }

            override fun onError(ex: Exception?) {
                if (debug) Log.d(TAG, "onError: ex=$ex")
                isConnected = false
            }
        }

        client.connectionLostTimeout = 110 * 1000
        client.connectBlocking()
    }


    private fun getSocketFirstConnectMsg() : String{
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName: String = packageInfo.versionName
        val versionCode: Long = packageInfo.longVersionCode
        val jsonString = """{
              "type":"ESTABLISH_CONNECTION",
              "data":{
                    "deviceId":"$snAddress",
                    "versionCode":"$versionCode",
                    "versionName":"$versionName"
              }
            }"""
        if (debug) Log.d(TAG, "first connect,send -> $jsonString")
        return jsonString
    }


    private fun getSocketHeartbeatMsg() : String{
        val level = getWifiRssi()
        val jsonString = """{
            "type":"HEARTBEAT",
            "data":{
                "deviceId":"$snAddress",
                "signal":$level
            }
        }"""
        if (debug) Log.d(TAG, "socket heartbeat,send -> $jsonString")
        return jsonString
    }


    private fun getWifiRssi(): Int {
        val wifiRssi: Int = GeneralUtil.getWifiRssi(this)
        var level = 0
        if (wifiRssi <= 0 && wifiRssi >= -50) {
            level = 4
        } else if (wifiRssi < -50 && wifiRssi >= -70) {
            level = 3
        } else if (wifiRssi < -70 && wifiRssi >= -80) {
            level = 2
        } else if (wifiRssi < -80 && wifiRssi >= -100) {
            level = 1
        }
        return level
    }

}