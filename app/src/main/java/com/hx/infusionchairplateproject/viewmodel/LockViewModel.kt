package com.hx.infusionchairplateproject.viewmodel

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import com.hx.infusionchairplateproject.databeen.BaseBean
import com.hx.infusionchairplateproject.databeen.ScreenInfo
import com.hx.infusionchairplateproject.network.NetworkManager
import com.hx.infusionchairplateproject.tools.GeneralUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 锁屏页数据模型
 */
class LockViewModel : ViewModel() {

    private val TAG:String = "liudehua_LockViewModel"
    private val debug:Boolean = false
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var isScreenLoadSuccess:Boolean = false
    private var isPutInSuccess:Boolean = false
    private val netManager: NetworkManager = NetworkManager.getInstance()

    // 二维码
    private var _qrCodeBitmap = MutableStateFlow("")
    val qrCodeBitmap = _qrCodeBitmap

    // 收费标准
    private var _priceInformation = MutableStateFlow(emptyList<String>())
    val priceInformation = _priceInformation

    // 轮播图
    private var _imageList = MutableStateFlow(emptyList<String>())
    val imageList = _imageList

    // 版本号
    private var _version = MutableStateFlow("")
    val version = _version

    // 网络状态
    private var _netState = MutableStateFlow(false)
    val netState = _netState

    // 投放状态
    private var _putInState = MutableStateFlow(true)
    val putInState = _putInState

    fun updateInfo(sn: String) {
        uiScope.launch {
            do {
                isScreenLoadSuccess = false
                withContext(Dispatchers.IO) {
                    realUpdateInfo(sn)
                    delay(2000L)
                }
            } while (!isScreenLoadSuccess)

//            while (!isScreenLoadSuccess){
//                withContext(Dispatchers.IO) {
//                    realUpdateInfo(sn)
//                    delay(2000L)
//                }
//            }
        }
    }

    fun updatePutInState(sn:String) {
        uiScope.launch {
            do {
                isPutInSuccess = false
                withContext(Dispatchers.IO) {
                realUpdatePutInState(sn)
                delay(2000L)
                }
            } while (!isPutInSuccess)


//            while (!isPutInSuccess){
//                withContext(Dispatchers.IO) {
//                    realUpdatePutInState(sn)
//                    delay(2000L)
//                }
//            }
        }
    }

    fun updatePromptMessage(context:Context){
        try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            _version.value = versionName
        } catch (e: PackageManager.NameNotFoundException) {
            _version.value = ""
        }
    }

    fun updateNetState(context: Context){
        uiScope.launch {
            while (true){
                withContext(Dispatchers.IO) {
                    _netState.value = (GeneralUtil.isNetWorkConnected(context))
                    delay(2000L)
                }
            }
        }
    }

    private  fun realUpdateInfo(sn: String) {
        val screenInfo: Call<ScreenInfo> = netManager.requestApi.getScreenInfo(sn)
        screenInfo.enqueue(object : Callback<ScreenInfo> {
            override fun onResponse(call: Call<ScreenInfo>, response: Response<ScreenInfo>) {
                if (!response.isSuccessful) {
                    if (debug) Log.d(TAG, "onResponse: 锁屏响应异常")
                    return
                }
                if(response.code() != 200) {
                    if (debug) Log.d(TAG, "onResponse: 锁屏响应码异常 Code = ${response.code()}")
                    return
                }

                val screenInfo = response.body()

                if (screenInfo == null) {
                    if (debug) Log.d(TAG, "onResponse: 锁屏响应数据为空")
                    return
                }
                if (screenInfo.status != 200) {
                    if (debug) Log.d(TAG, "onResponse: 锁屏响应数据中的 status(code) ！= 200")
                    return
                }

                // 标志 ：锁屏数据请求成功
                isScreenLoadSuccess = true

                // 套餐信息
                val feePackages : MutableList<ScreenInfo.DataDataBeen.FeePackagesDataBeen> =screenInfo.data.feePackages
                if (feePackages.isEmpty()) {
                    if (debug) Log.d(TAG, "onResponse: 套餐信息为空")
                    return
                }
                _priceInformation.value = emptyList()
                for (i in 0 until feePackages.size) {
                    val feePackagesDataBeen:  ScreenInfo.DataDataBeen.FeePackagesDataBeen = feePackages[i]
                    if ("TABLET" != feePackagesDataBeen.type) {
                       if (debug) Log.d(TAG, "onResponse: feePackages 的 type字段 不为 TABLET")
                        return
                    }
                    if (feePackagesDataBeen.rules.isEmpty()) {
                        if (debug) Log.d(TAG, "onResponse: feePackages 的 rules字段 为空")
                        return
                    }
                    for (j in 0 until feePackagesDataBeen.rules.size) {
                        val feeStr = feePackagesDataBeen.rules[j].feeStr
                        _priceInformation.value = _priceInformation.value.plus(feeStr)
                    }
                }

                // 二维码信息
                val qrUrl = screenInfo.data.url
                _qrCodeBitmap.value = qrUrl

                // 轮播图
                val imageList = screenInfo.data.images
                _imageList.value = imageList

            }

            override fun onFailure(call: Call<ScreenInfo>, t: Throwable) {
                if (debug) Log.d(TAG, "onFailure: t = $t")
                isScreenLoadSuccess = false
            }

        })
    }

    private fun realUpdatePutInState(sn: String) {
        val deviceStatus: Call<BaseBean<String>> = netManager.requestApi.getDeviceStatus(sn)
        deviceStatus.enqueue(object : Callback<BaseBean<String>> {
            override fun onResponse(call: Call<BaseBean<String>>, response: Response<BaseBean<String>>) {
                if (!response.isSuccessful) {
                    if (debug) Log.d(TAG, "onResponse: 投放状态响应异常")
                    return
                }
                if(response.code() != 200) {
                    if (debug) Log.d(TAG, "onResponse: 投放状态响应码异常 Code = ${response.code()}")
                    return
                }

                val deviceState = response.body()

                if (deviceState == null) {
                    if (debug) Log.d(TAG, "onResponse: 投放状态响应数据为空")
                    return
                }
                if (deviceState.status != 200) {
                    if (debug) Log.d(TAG, "onResponse: 投放状态响应数据中的 status(code) ！= 200")
                    return
                }

                // 标志 ： 获取设备是否投放信息成功
                isPutInSuccess = true

                // 投放状态
                _putInState.value = deviceState.data.equals("PUT_ON")

            }

            override fun onFailure(call: Call<BaseBean<String>>, t: Throwable) {
                if (debug) Log.d(TAG, "onFailure: t = $t")
                isPutInSuccess = false
            }

        })
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
