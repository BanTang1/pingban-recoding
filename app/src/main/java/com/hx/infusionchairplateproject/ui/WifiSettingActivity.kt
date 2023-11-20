package com.hx.infusionchairplateproject.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hx.infusionchairplateproject.BaseActivity
import com.hx.infusionchairplateproject.R
import com.hx.infusionchairplateproject.tools.GeneralUtil
import com.hx.infusionchairplateproject.tools.SPTool


/**
 * WIFI 设置界面
 */
class WifiSettingActivity : BaseActivity() {

    private val TAG = "liudehua_WifiSettingActivity"
    private var debug: Boolean = false

    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager

    // Wifi列表
    var wifiList = mutableStateListOf<ScanResult>()

    // Wifi 开关状态
    var wifiSwitchState = mutableStateOf(false)

    // 已保存的WIFI SSID
    var savedWifiList = mutableStateListOf<WifiConfiguration>()

    // Wifi 连接状态
    var wifiConnectState = mutableStateOf("")

    // 当前已连接的Wifi SSID
    var connectedWifiSSID = mutableStateOf("")

    // 接收扫描结果  监听Wifi开关状态
    val wifiScanReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    if (success) {
                        // Check the currently connected Wifi BSSID
                        val connectionInfo = wifiManager.connectionInfo
                        if (connectionInfo != null && connectionInfo.bssid != null) {
                            if (debug) Log.d(TAG, "onReceive: connectedBSSID = ${connectionInfo.bssid}  SSID = ${connectionInfo.ssid}")
                        }

                        // check the saved Wifi BSSID
                        val savedNetworks = wifiManager.configuredNetworks
                        if (savedNetworks != null) {
                            savedWifiList.clear()
                            savedWifiList.addAll(savedNetworks)
                            if (debug) Log.d(TAG, "onReceive: savedNetworks = ${savedWifiList.joinToString { it.SSID }}")
                        }

                        // get scan results , filter and sort
                        val scanResults = wifiManager.scanResults
                            .filter { it.SSID.isNotEmpty() }
                            .distinctBy { it.BSSID }
                            .sortedWith(
                                compareBy(
                                    { it.BSSID == connectionInfo.bssid },
                                    { savedWifiList.any { saved -> saved.SSID.trim('\"') == it.SSID } },
                                    { it.level })
                            )
                            .distinctBy { it.SSID }
                            .reversed()

                        // update UI
                        wifiList.clear()
                        wifiList.addAll(scanResults)
                    } else {
                        if (debug) Log.e(TAG, "onReceive: wifi scan failed")
                    }
                }

                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                        WifiManager.WIFI_STATE_ENABLED -> {
                            wifiManager.startScan()
                        }

                        WifiManager.WIFI_STATE_DISABLED -> {
                            wifiList.clear()
                        }
                    }

                }

                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    val connectedWifiInfo = (context.getSystemService(WIFI_SERVICE) as WifiManager).connectionInfo
                    val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO) ?: return
                    when(info.state) {
                        NetworkInfo.State.DISCONNECTED -> {wifiConnectState.value = "连接已断开"}
                        NetworkInfo.State.CONNECTED -> {
                            wifiConnectState.value = "已连接到网络：${connectedWifiInfo.ssid}"
                            connectedWifiSSID.value = connectedWifiInfo.ssid
                        }
                        else -> {
                            val state = info.detailedState
                            if (state == NetworkInfo.DetailedState.CONNECTING) {
                                wifiConnectState.value = "连接中..."
                            } else if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                                wifiConnectState.value = "正在验证身份信息..."
                            } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                                wifiConnectState.value = "正在获取IP地址..."
                            } else if (state == NetworkInfo.DetailedState.FAILED) {
                                wifiConnectState.value = "连接失败..."
                            } else if (state == NetworkInfo.DetailedState.SUSPENDED) {
                                wifiConnectState.value = "连接被挂起..."
                            }
                        }
                    }

                }
            }
        }
    }

    // 由LauncherActivity跳转过来需要在网络连接后自动跳转至锁屏界面
    val mHandler = Handler(Looper.myLooper()!!)
    val r: Runnable = object : Runnable {
        override fun run() {
            if (GeneralUtil.isNetWorkConnected(this@WifiSettingActivity)) {
                startActivity(Intent(this@WifiSettingActivity, LockScreenActivity::class.java))
                finish()
                return
            }
            // 如果系统未自动连接Wifi，本地尝试连接最近一次连接过的Wifi
            val lastWifiSSID: String = SPTool.getString("lastWifiSSID")
            val wifiConfig = findWifiConfigurationBySSID(lastWifiSSID)
            if (wifiConfig != null) {
                wifiManager.enableNetwork(wifiConfig.networkId, true)
            }
            mHandler.postDelayed(this, 3000)
        }
    }

    /**
     * 在已保存的Wifi列表中找到指定的配置
     * @param ssid      名称， 传入的时候需加上 “” , 如 "xxxxxx"
     * @return          WifiConfiguration
     */
    @SuppressLint("MissingPermission")
    private fun findWifiConfigurationBySSID(ssid: String): WifiConfiguration? {
        for (config in wifiManager.configuredNetworks) {
            if (config.SSID == ssid) {
                return config
            }
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiSwitchState.value = wifiManager.isWifiEnabled

        val intent = intent
        if (intent != null) {
            val status = intent.getBooleanExtra("status", false)
            if (!status) {
                mHandler.postDelayed(r, 3000)
            }
        }

        setContent {
            WifiScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiScanReceiver)
    }


    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WifiScreen() {
        var showDialog by remember { mutableStateOf(false) }
        var ssid by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisibility by remember { mutableStateOf(false) }
        var isWifiSaved by remember { mutableStateOf(false) }

        // Wifi连接信息
        val connectionInfo = wifiManager.connectionInfo
        val networkConfig = savedWifiList.find { it.SSID == "\"$ssid\"" }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    val isHaveNet = GeneralUtil.isNetWorkConnected(this@WifiSettingActivity)
                    if (isHaveNet) {
                        finish()
                    }
                }) {
                    Icon(painterResource(id = R.mipmap.ic_arrow_back_white_24dp), contentDescription = "返回箭头")
                    Text(text = "返回", fontSize = 24.sp)
                }
                Text(text = "网络设置（WiFi）", fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterVertically))
                Switch(checked = wifiSwitchState.value,
                    onCheckedChange = {
                        wifiManager.isWifiEnabled = !wifiSwitchState.value
                        wifiSwitchState.value = it
                    }
                )
            }
            Text(text = wifiConnectState.value, modifier = Modifier.align(Alignment.CenterHorizontally))

            if (!wifiSwitchState.value) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Wifi 已关闭", fontSize = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                }
                return
            }

            if (this@WifiSettingActivity.wifiList.size == 0) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(text = "正在扫描WiFi...", fontSize = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                }
                return
            }

            LazyColumn {
                items(wifiList) { network ->
                    WifiNetworkRow(scanResult = network) {
                        ssid = network.SSID
                        password = ""
                        showDialog = true
                        isWifiSaved = savedWifiList.any { it.SSID.trim('\"') == network.SSID }
                    }
                }
            }
        }

        if (showDialog) {
            if (isWifiSaved) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        wifiManager.startScan()
                    },
                    title = { Text(ssid) },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            if (connectionInfo != null && connectionInfo.ssid == "\"$ssid\"") {
                                if (networkConfig != null) {
                                    wifiManager.disconnect()
                                    wifiManager.disableNetwork(networkConfig.networkId)
                                    wifiManager.saveConfiguration()
                                }
                            } else {
                                if (networkConfig != null) {
                                    wifiManager.disconnect()
                                    wifiManager.enableNetwork(networkConfig.networkId, true)
                                    wifiManager.reconnect()
                                    SPTool.putString("lastWifiSSID",networkConfig.SSID)
                                }
                            }
                        }) {
                            if (connectionInfo != null && connectionInfo.ssid == "\"$ssid\"") {
                                Text("断开")
                            } else {
                                Text("连接")
                            }
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showDialog = false
                            savedWifiList.removeIf { it.SSID == "\"$ssid\"" }
                            if (networkConfig != null) {
                                // 删除网络
                                wifiManager.removeNetwork(networkConfig.networkId)
                                wifiManager.saveConfiguration()
                            }
                        }) {
                            Text("取消保存")
                        }
                    }

                )
            } else {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        wifiManager.startScan()
                    },
                    title = { Text(ssid) },
                    text = {
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("请输入Wifi密码：") },
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                    Icon(
                                        painterResource(id = if (passwordVisibility) R.mipmap.eye_show else R.mipmap.eye_no_show),
                                        contentDescription = "密码可见性切换"
                                    )
                                }
                            }
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            val wifiConfig = WifiConfiguration()
                            wifiConfig.SSID = String.format("\"%s\"", ssid);
                            wifiConfig.preSharedKey = String.format("\"%s\"", password)
                            val netId = wifiManager.addNetwork(wifiConfig)
                            wifiManager.disconnect()
                            wifiManager.enableNetwork(netId, true)
                            wifiManager.reconnect()
                            isWifiSaved = true
                            SPTool.putString("lastWifiSSID",wifiConfig.SSID)
                        }) {
                            Text("连接")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showDialog = false
                            val wifiConfig = WifiConfiguration()
                            wifiConfig.SSID = java.lang.String.format("\"%s\"", ssid)
                            wifiConfig.preSharedKey = String.format("\"%s\"", password)
                            // 添加网络配置
                            val netId = wifiManager.addNetwork(wifiConfig)
                            if (netId == -1) {
                                if (debug) Log.e(TAG, "Unable to add network");
                            } else {
                                // 禁用自动连接
                                wifiManager.enableNetwork(netId, false);
                                savedWifiList.add(wifiConfig)
                            }
                        }) {
                            Text("保存")
                        }
                    }
                )
            }
        } else {
            passwordVisibility = false
        }
    }

    /**
     * 每一个需要展示的 Wifi 信息
     * 此处只获取，不参与任何状态的更改
     */
    @Composable
    fun WifiNetworkRow(scanResult: ScanResult, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .clickable(onClick = onClick)
        ) {
            Row {

                val isConnecting: Boolean
                val connectionInfo = wifiManager.connectionInfo
                isConnecting = connectionInfo != null && connectionInfo.ssid == "\"${scanResult.SSID}\""

                val isSaved = savedWifiList.any { it.SSID.trim('\"') == scanResult.SSID }
                var textState = "(未连接)"
                if (isSaved) {
                    textState = "(已保存)"
                }
                if (isConnecting) {
                    textState = "(正在连接)"
                }
                Log.i("zh___", "WifiNetworkRow: ${connectedWifiSSID.value}     ${scanResult.SSID}")
                if (connectedWifiSSID.value.trim('\"') == scanResult.SSID) {
                    textState = "(已连接)"
                }


                var wifiLevel: Int = R.mipmap.wifi_0
                if (scanResult.level <= 0 && scanResult.level >= -50) {
                    wifiLevel = R.mipmap.wifi_4
                } else if (scanResult.level < -50 && scanResult.level >= -70) {
                    wifiLevel = R.mipmap.wifi_3
                } else if (scanResult.level < -70 && scanResult.level >= -80) {
                    wifiLevel = R.mipmap.wifi_2
                } else if (scanResult.level < -80 && scanResult.level >= -100) {
                    wifiLevel = R.mipmap.wifi_1
                }

                var isWifiEncryption: Boolean = scanResult.capabilities != "[ESS]"

                Text(
                    text = scanResult.SSID,
                    fontSize = 15.sp,
                    color = if (isConnecting) Color.Blue else Color.Unspecified,
                    fontWeight = if (isConnecting) FontWeight.Bold else null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(16.dp)
                )
                Text(
                    text = textState,
                    fontSize = 15.sp,
                    color = if (isConnecting) Color.Blue else Color.Unspecified,
                    fontWeight = if (isConnecting) FontWeight.Bold else null,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painterResource(id = wifiLevel),
                    contentDescription = "WiFi信号",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .align(Alignment.CenterVertically)
                )
                if (isWifiEncryption) {
                    Icon(
                        painterResource(id = R.mipmap.wifi_suo),
                        contentDescription = "Wifi有密码",
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .offset(x = (-15).dp, y = (-10).dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(17.dp))
                }
            }
        }
    }

}