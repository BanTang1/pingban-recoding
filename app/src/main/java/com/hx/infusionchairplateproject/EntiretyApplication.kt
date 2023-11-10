package com.hx.infusionchairplateproject

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import cn.wch.uartlib.WCHUARTManager
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.CustomToastStyle
import com.hx.infusionchairplateproject.ch340.CH34xManager
import com.hx.infusionchairplateproject.databeen.AndroidVersion
import com.hx.infusionchairplateproject.network.DownloadMgr
import com.hx.infusionchairplateproject.network.NetworkManager
import com.hx.infusionchairplateproject.tools.CommandTool
import com.hx.infusionchairplateproject.tools.GeneralUtil
import com.hx.infusionchairplateproject.tools.SPTool
import com.hx.infusionchairplateproject.ui.AllAppActivity
import com.hx.infusionchairplateproject.ui.LockScreenActivity
import com.hx.infusionchairplateproject.ui.WifiSettingActivity
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.URI


class EntiretyApplication : Application() {

    private val TAG = "liudehua_EntiretyApplication"
    private var debug: Boolean = false

    companion object {
        lateinit var context: Context
    }

    private lateinit var snAddress: String
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var client: WebSocketClient
    private var isConnected = false

    private var ch34xManager: CH34xManager? = null

    private val appPkgList = mutableListOf<String>()

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        initAppPkgList()

        // 初始化 Toast 框架
        Toaster.init(this)

        snAddress = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        SPTool.putString("mMACaddress", snAddress)

        socketViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this).create(SocketViewModel::class.java)

        realReconnect()
        startHeartbeat()

        // 初始化 CH340驱动
        WCHUARTManager.getInstance().init(this)
        ch34xManager = CH34xManager.getCH34xManager()

        // 服务器连接网络状态监测
        serverNetworkDetection()
    }

    private fun serverNetworkDetection() {
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                if (!isConnected){
                    delay(5000L)
                    if (GeneralUtil.isActivityTop(this@EntiretyApplication,WifiSettingActivity::class.java)){
                        continue
                    }
                    val params = ToastParams()
                    params.text = "与服务器断开连接，请检查网络状态！"
                    params.style = CustomToastStyle(R.layout.toast_error);
                    Toaster.show(params)
                }
            }
        }
    }

    private fun initAppPkgList() {
        appPkgList.clear()
        appPkgList.add("com.qiyi.video.pad")
        appPkgList.add("com.youku.phone")
        appPkgList.add("tv.danmaku.bilibilihd")
        appPkgList.add("air.com.miracle.gobang")
        appPkgList.add("com.tinmanarts.JoJoSherlock")
        appPkgList.add("com.sinyee.babybus.kitchen")
        appPkgList.add("com.cdkaw.etyzmg")
        appPkgList.add("com.sinyee.babybus.findCha")
        appPkgList.add("com.tencent.qqlivekid")
        appPkgList.add("com.tencent.qqlive")
        appPkgList.add("com.sinyee.babybus.splice")
        appPkgList.add("com.sinyee.babybus.cultivation")
        appPkgList.add("com.sinyee.babybus.shopping")
        appPkgList.add("com.sinyee.babybus.superman")
        appPkgList.add("com.zhangpei.pinyin")
        appPkgList.add("com.sinyee.education.color_new")
        appPkgList.add("com.bb.happykids")
        appPkgList.add("com.qiyi.video.child")
        appPkgList.add("com.sinyee.babybus.initiation")
    }

    fun getSnAddress(): String {
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
                // disconnect ,sleep 2s. Wait for connection...
                delay(2000L)
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

                val jsonObject: JSONObject = JSONObject(message)
                val type = jsonObject.optInt("type")
                val other = jsonObject.optString("other")

                when (type) {
                    0 -> {
                        socketViewModel.isPutIn.value = false
                        send(getOnMessageWriteBack("NOT_RELEASED"))
                    }

                    1 -> {
                        socketViewModel.isPutIn.value = true
                        send(getOnMessageWriteBack("YES_RELEASED"))
                    }

                    2 -> {
                        socketViewModel.putInIsScan.value = socketViewModel.SCAN_STATE_OK
                        stateDelayChange("putIn", 2000L)
                        send(getOnMessageWriteBack("PUTIN-SCAN_OK"))
                    }

                    3 -> {
                        socketViewModel.putInIsScan.value = socketViewModel.SCAN_STATE_NO
                        stateDelayChange("putIn", 2000L)
                        send(getOnMessageWriteBack("PUTIN-SCAN_NO"))
                    }

                    4 -> {
                        socketViewModel.putInIsScan.value = socketViewModel.SCAN_STATE_REFUSE
                        stateDelayChange("putIn", 2000L)
                        send(getOnMessageWriteBack("PUTIN_SCAN_REFUSE"))
                    }

                    5 -> {
                        val showtime: Int = other.toInt()
                        SPTool.putLong("unlockTime", System.currentTimeMillis() + showtime * 1000L * 60)
                        if (showtime > 0) {
                            if (!GeneralUtil.isActivityTop(this@EntiretyApplication, AllAppActivity::class.java)) {
                                val intent = Intent(this@EntiretyApplication, AllAppActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                send(getOnMessageWriteBack("UNLOCK"))
                            }
                        } else {
                            if (!GeneralUtil.isActivityTop(this@EntiretyApplication, LockScreenActivity::class.java)) {
                                val intent = Intent(this@EntiretyApplication, LockScreenActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                send(getOnMessageWriteBack("LOCK"))
                                handleLockExtra()
                            }
                        }
                    }

                    6 -> {
                        socketViewModel.screenIsScan.value = socketViewModel.SCAN_STATE_OK
                        stateDelayChange("lockScreen", 2000L)
                        send(getOnMessageWriteBack("SCREEN_SCAN_OK"))
                    }

                    7 -> {
                        socketViewModel.screenIsScan.value = socketViewModel.SCAN_STATE_NO
                        stateDelayChange("lockScreen", 2000L)
                        send(getOnMessageWriteBack("SCREEN_SCAN_NO"))
                    }

                    8 -> {
                        socketViewModel.screenIsScan.value = socketViewModel.SCAN_STATE_REFUSE
                        stateDelayChange("lockScreen", 2000L)
                        send(getOnMessageWriteBack("SCREEN_SCAN_REFUSE"))
                    }

                    9 -> {
                        val intent = Intent(this@EntiretyApplication, LockScreenActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("control", "abort")
                        startActivity(intent)
                        socketViewModel.isPutIn.value = false
                        send(getOnMessageWriteBack("PUTOUT"))
                        SPTool.putLong("unlockTime", 0)
                    }

                    10 -> {
                        send(getOnMessageWriteBack("REBOOT"))
                        CommandTool.execSuCMD("reboot")
                    }

                    11 -> {
                        updateAndroidVersion()
                    }

                    12 -> {
                        //无需实现，三方应用只需要在后台上架下载即可，在进入三方App页面的时候会自动同步服务最新的App信息
                    }

                    13 -> {
                        // TODO 进入WIFI 界面  WIFI 界面待实现
                    }

                    14 -> {
                        //TODO 退出WIFI 界面，  WIFI界面待实现
                    }

                    15 -> {
                        if (ch34xManager?.isUSBConnect != true) {
                            send(getOnMessageWriteBack("CHARGE_USB_DISABLE"))
                            return
                        }
                        val usbChargeTime = other.toLong()
                        SPTool.putLong("usb_time", System.currentTimeMillis() + usbChargeTime * 1000L * 60)
                        ch34xManager?.sendOpen()
                        ch340StateFeedback("open")
                    }

                    16 -> {
                        if (ch34xManager?.isUSBConnect != true) {
                            send(getOnMessageWriteBack("CHARGE_USB_DISABLE"))
                            return
                        }
                        SPTool.putLong("usb_time", 0)
                        ch34xManager?.sendClose()
                        ch340StateFeedback("close")
                    }

                    17 -> {
                        if (ch34xManager?.isUSBConnect != true) {
                            send(getOnMessageWriteBack("CHARGE_USB_DISABLE"))
                            return
                        }
                        ch34xManager?.sendState()
                        ch340StateFeedback("state")
                    }

                    18 -> {
                        send(getOnMessageWriteBack("CLEAR_ALL_APPS"))
                        CoroutineScope(Dispatchers.IO).launch {
                            CommandTool.execSuCMD("rm -rf /sdcard/TripartiteApp")
                            //  /vendor 目录下  无法卸载，可卸载更新过的版本
                            for (pak in appPkgList) {
                                CommandTool.execSuCMD("pm uninstall $pak")
                            }
                        }
                    }

                    19 -> {
                        send(getOnMessageWriteBack("CLEAR_DESIGNATE_APP"))
                        CoroutineScope(Dispatchers.IO).launch {
                            val parts: List<String> = other.split(",")
                            for (part in parts) {
                                //  /vendor 目录下  无法卸载，可卸载更新过的版本
                                CoroutineScope(Dispatchers.IO).launch {
                                    CommandTool.execSuCMD("pm uninstall $part")
                                }
                            }
                        }
                    }
                }

            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                if (debug) Log.d(TAG, "onClose: 连接关闭 --> code=$code  reason=$reason  remote=$remote")
                isConnected = false
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


    private fun handleLockExtra() {
        CoroutineScope(Dispatchers.IO).launch {
            // tell to server ,lock screen
            NetworkManager.getInstance().requestApi.lockScreen(snAddress).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {

                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }

            })
            // clear app data
            for (appPkg in appPkgList) {
                CommandTool.execSuCMD("pm clear $appPkg")
            }
            // uninstall useless apps
            unInstallThreeApp()
        }
    }

    /**
     * 1.获取非系统应用包名
     * 2.排除我们自己的三方app的包名
     * 3.卸载与我们无关的三方app
     */
    private fun unInstallThreeApp() {
        // one
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES)
        val threePackages = ArrayList<String>()
        for (info in installedPackages) {
            val pkg = info.packageName
            if (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                threePackages.add(pkg)
            }
        }
        // two
        val difference = ArrayList(threePackages)
        difference.removeAll(appPkgList)
        difference.remove("com.emoji.keyboard.touchpal.go") // 保留SDK内置的触宝输入法
        difference.remove("com.sohu.inputmethod.sogou") // 保留搜狗输入法
        difference.remove("com.android.calendar") // 保留系统自带的日历
        difference.remove("com.android.dreams.basic") // 保留系统自带的屏保
        difference.remove("com.android.musicfx") // 保留系统自带的音乐均衡器
        difference.remove("com.android.calculator2") // 保留系统自带的计算器
        // three
        for (pkg in difference) {
            CommandTool.execSuCMD("pm uninstall $pkg")
        }
    }

    private fun getSocketFirstConnectMsg(): String {
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


    private fun getSocketHeartbeatMsg(): String {
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

    private fun getOnMessageWriteBack(feedbackType: String): String {
        val jsonString = """{
            "type":"FEEDBACK",
            "data":{
                "deviceId":"$snAddress",
                "val":"$feedbackType"
            }   
        }"""
        if (debug) Log.d(TAG, "OnMessage Write Back: send -> $jsonString")
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

    fun getSocketViewModel(): SocketViewModel {
        return socketViewModel

    }


    private fun stateDelayChange(who: String, delayTime: Long) {
        GlobalScope.launch {
            delay(delayTime)
            if (who == "putIn") {
                socketViewModel.putInIsScan.value = socketViewModel.SCAN_STATE_DEFAULT
            } else if (who == "lockScreen") {
                socketViewModel.screenIsScan.value = socketViewModel.SCAN_STATE_DEFAULT
            }
        }
    }


    /**
     * 检查版本更新
     */
    private fun updateAndroidVersion() {
        CoroutineScope(Dispatchers.IO).launch {

            val versionInfo = NetworkManager.getInstance().requestApi.androidVersion
            versionInfo.enqueue(object : Callback<AndroidVersion> {
                override fun onResponse(call: Call<AndroidVersion>, response: Response<AndroidVersion>) {
                    if (!response.isSuccessful) {
                        if (debug) Log.d(TAG, "onResponse: 版本响应异常")
                        return
                    }
                    if (response.code() != 200) {
                        if (debug) Log.d(TAG, "onResponse: 版本响应码异常 Code = ${response.code()}")
                        return
                    }

                    val versionInfo = response.body()

                    if (versionInfo == null) {
                        if (debug) Log.d(TAG, "onResponse: 版本响应为空")
                        return
                    }

                    if (versionInfo.status != 200) {
                        if (debug) Log.d(TAG, "onResponse: 版本响应状态码异常 Status = ${versionInfo.status}")
                        return
                    }

                    // version info  success
                    val packageInfo: PackageInfo = this@EntiretyApplication.packageManager.getPackageInfo(this@EntiretyApplication.packageName, 0)
                    val localVersionCode = packageInfo.longVersionCode
                    val remoteVersionCode = versionInfo.data.versionCode

                    if (localVersionCode >= remoteVersionCode) {
                        if (debug) Log.d(TAG, "onResponse: 当前版本已是最新版本")
                        client.send(getOnMessageWriteBack("UPDATE_NO"))
                        Toaster.show("当前版本已是最新版本")
                        return
                    }

                    if (!GeneralUtil.isActivityTop(this@EntiretyApplication, LockScreenActivity::class.java)) {
                        if (debug) Log.d(TAG, "onResponse: 用户正在使用，取消本次更新")
                        Toaster.showShort("用户正在使用，取消本次更新")
                        return
                    }

                    // real start update
                    if (debug) Log.d(TAG, "onResponse: 小于最低版本,开始更新")
                    client.send("UPDATE_YES")
                    Toaster.showShort("小于最低版本,开始更新")

                    // first delete file, then real update
                    val file = File(Environment.getExternalStorageDirectory().path + "/hxAndroidV")
                    if (file.exists()) {
                        file.deleteRecursively()
                    }
                    downLoadLockApp(versionInfo.data.url)
                }

                override fun onFailure(call: Call<AndroidVersion>, t: Throwable) {
                    if (debug) Log.d(TAG, "onFailure: t = $t")
                }

            })
        }
    }

    /**
     * 锁屏软件更新
     */
    private fun downLoadLockApp(url:String) {
        DownloadMgr.getInstance().addTask(url,"/sdcard/hxAndroidV/${url.substringAfterLast("/")}",object :DownloadMgr.Callback(){
            override fun onSuccess(url: String, l: Long) {
                CommandTool.execSuCMD("pm install -r /sdcard/hxAndroidV/${url.substringAfterLast("/")}")
            }
        })
    }

    /**
     * CH340状态反馈
     */
    private fun ch340StateFeedback(type:String) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000L)
            val result = ch34xManager!!.checkResult()
            when(type) {
                "open" -> {
                    if (result.equals("open")){
                        client.send(getOnMessageWriteBack("CHARGE_OPEN"))
                    } else {
                        client.send(getOnMessageWriteBack("CHARGE_ERROR"))
                    }
                }

                "close" -> {
                    if (result.equals("close")){
                        client.send(getOnMessageWriteBack("CHARGE_CLOSE"))
                    } else {
                        client.send(getOnMessageWriteBack("CHARGE_ERROR"))
                    }
                }

                "state" -> {
                    if (result.equals("state is open")){
                        client.send(getOnMessageWriteBack("CHARGE_STATE_OPEN"))
                    } else if (result.equals("state is close")){
                        client.send(getOnMessageWriteBack("CHARGE_STATE_CLOSE"))
                    } else {
                        client.send(getOnMessageWriteBack("CHARGE_STATE_UNKNOW"))
                    }
                }

            }
        }
    }

}