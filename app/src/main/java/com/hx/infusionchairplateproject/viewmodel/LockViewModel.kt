package com.hx.infusionchairplateproject.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.hx.infusionchairplateproject.databeen.ScreenInfo
import com.hx.infusionchairplateproject.network.NetRequestManager
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 锁屏页数据
 */
class LockViewModel : ViewModel() {

    private val TAG:String = "liudehua-LockViewModel"
    private val debug:Boolean = true
    private val netManager: NetRequestManager = NetRequestManager.getInstance()

    // 二维码
    private var _qrCodeBitmap = MutableStateFlow("")
    val qrCodeBitmap = _qrCodeBitmap

    // 收费标准
    private var _priceInformation = MutableStateFlow(emptyList<String>())
    val priceInformation = _priceInformation

    // 轮播图
    private var _imageList = MutableStateFlow(emptyList<String>())
    val imageList = _imageList

    fun updateInfo(sn: String) {
        val screenInfo: Call<ScreenInfo> = netManager.requestApi.getScreenInfo(sn)
        screenInfo.enqueue(object : Callback<ScreenInfo> {
            override fun onResponse(call: Call<ScreenInfo>, response: Response<ScreenInfo>) {
                if (!response.isSuccessful) {
                    if (debug) Log.d(TAG, "onResponse: 响应异常")
                    return
                }
                if(response.code() != 200) {
                    if (debug) Log.d(TAG, "onResponse: 响应码异常 Code = ${response.code()}")
                    return
                }

                val screenInfo = response.body()

                if (screenInfo == null) {
                    if (debug) Log.d(TAG, "onResponse: 响应数据为空")
                    return
                }
                if (screenInfo.status != 200) {
                    if (debug) Log.d(TAG, "onResponse: 响应数据中的 status(code) ！= 200")
                    return
                }

                // 套餐信息
                val feePackages : MutableList<ScreenInfo.DataDataBeen.FeePackagesDataBeen> =screenInfo.data.feePackages
                if (feePackages.isEmpty()) {
                    if (debug) Log.d(TAG, "onResponse: 套餐信息为空")
                    return
                }
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
                Log.d(TAG, "onFailure: t = $t")
            }

        })
    }
}
