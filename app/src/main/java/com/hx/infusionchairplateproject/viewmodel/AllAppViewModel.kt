package com.hx.infusionchairplateproject.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.hx.infusionchairplateproject.databeen.PadApkList
import com.hx.infusionchairplateproject.databeen.PadApkLists
import com.hx.infusionchairplateproject.network.DownloadMgr
import com.hx.infusionchairplateproject.network.NetworkManager
import com.hx.infusionchairplateproject.tools.GeneralUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AllAppViewModel : ViewModel() {

    private val TAG: String = "liudehua_AllAppViewModel"
    private var debug: Boolean = true

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val netManager: NetworkManager = NetworkManager.getInstance()


    // 视频类
    private val _videoApkList = MutableStateFlow(emptyList<AppInfo>())
    val videoApkList = _videoApkList

    // 游戏类
    private val _gameApkList = MutableStateFlow(emptyList<AppInfo>())
    val gameApkList = _gameApkList

    // 绘画类
    private val _paintApkList = MutableStateFlow(emptyList<AppInfo>())
    val paintApkList = _paintApkList

    // 服务器保存的APP列表
    private var serverAPK = mutableListOf<String>()

    // 本地保存的App列表
    private var localAPK = mutableListOf<String>()


    fun getPadApkList(type: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                realGetPadApkList(type)
            }
        }
    }

    fun getAllApkList() {
        getPadApkList("视频")
        getPadApkList("游戏")
        getPadApkList("绘画")
    }

    private fun realGetPadApkList(type: String) {
        val padApkList: Call<PadApkList> = netManager.requestApi.getPadApkList(type)
        padApkList.enqueue(object : Callback<PadApkList> {
            override fun onResponse(call: Call<PadApkList>, response: Response<PadApkList>) {
                if (!response.isSuccessful) {
                    if (debug) Log.d(TAG, "onResponse: App信息获取异常")
                    return
                }
                if (response.code() != 200) {
                    if (debug) Log.d(TAG, "onResponse: App信息响应码异常 Code = ${response.code()}")
                    return
                }

                val padApkList = response.body()

                if (padApkList == null) {
                    if (debug) Log.d(TAG, "onResponse: App信息响应为空")
                    return

                }
                if (padApkList.status != 200) {
                    if (debug) Log.d(TAG, "onResponse: App信息响应数据中的 status(code) ！= 200")
                    return
                }

                // 标志 : 请求成功

                val data = padApkList.data
                if (data.isEmpty()) {
                    if (debug) Log.d(TAG, "onResponse: App信息为空")
                }

                when (type) {
                    "视频" -> {
                        _videoApkList.value = emptyList()
                    }

                    "游戏" -> {
                        _gameApkList.value = emptyList()
                    }

                    "绘画" -> {
                        _paintApkList.value = emptyList()
                    }
                }

                for (i in 0 until data.size) {
                    if (data[i].apkUrl == null) {
                        if (debug) Log.d(TAG, "onResponse: App信息中的 apkUrl 为空")
                        continue
                    }
                    val appInfo = AppInfo(data[i].name, data[i].icon, data[i].packageName, data[i].apkUrl)
                    when (type) {
                        "视频" -> {
                            _videoApkList.value = _videoApkList.value.plus(appInfo)
                        }

                        "游戏" -> {
                            _gameApkList.value = _gameApkList.value.plus(appInfo)
                        }

                        "绘画" -> {
                            _paintApkList.value = _paintApkList.value.plus(appInfo)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PadApkList>, t: Throwable) {
                if (debug) Log.d(TAG, "onFailure: t = $t")
            }

        })
    }


    /**
     * 判断是否需要去服务器上下载APK
     * 下载/不下载
     */
    fun initApk() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                realInitApk()
            }
        }
    }

    private fun realInitApk() {
        val padApkLists = netManager.requestApi.padApkLists
        padApkLists.enqueue(object : Callback<PadApkLists> {
            @SuppressLint("SdCardPath")
            override fun onResponse(call: Call<PadApkLists>, response: Response<PadApkLists>) {
                if (!response.isSuccessful) {
                    if (debug) Log.d(TAG, "onResponse: 所有App信息获取异常")
                    return
                }
                if (response.code() != 200) {
                    if (debug) Log.d(TAG, "onResponse: 所有App信息响应码异常 Code = ${response.code()}")
                    return
                }

                val padApkLists = response.body()

                if (padApkLists == null) {
                    if (debug) Log.d(TAG, "onResponse: 所有App信息响应为空")
                    return
                }
                if (padApkLists.status != 200) {
                    if (debug) Log.d(TAG, "onResponse: 所有App信息响应数据中的 status(code) ！= 200")
                    return
                }

                // 标志  ： 成功

                val data = padApkLists.data
                if (data.isEmpty()) {
                    if (debug) Log.d(TAG, "onResponse: APP 下载路径为空")
                    return
                }

                // 服务器列表
                serverAPK = data.map { it.substringAfterLast("/") }.toMutableList()
                // 本地列表
                localAPK = GeneralUtil.getFileNamesInDirectory("/sdcard/TripartiteApp/").toMutableList()

                val differentData = serverAPK.filterNot { it in localAPK }
                localAPK.retainAll { it in serverAPK }

                if (differentData.isNotEmpty()) {
                    // go to download
                    for (apkName in differentData) {
                        downLoadApk("https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/$apkName")
                    }
                }


            }

            override fun onFailure(call: Call<PadApkLists>, t: Throwable) {
                if (debug) Log.d(TAG, "onFailure: t = $t")
            }

        })
    }

    /**
     * 正式下载
     */
    @SuppressLint("SdCardPath")
    private fun downLoadApk(url: String) {
        DownloadMgr.getInstance().addTask(url, "/sdcard/TripartiteApp/${url.substringAfterLast("/")}", object : DownloadMgr.Callback() {
            override fun onStart(url: String?) {
                Log.i("zh___", "onStart: url=$url")
            }

            override fun onProgress(url: String, progress: Long, total: Long) {
                Log.i("zh___", "onProgress: url=$url  process = $progress  total = $total")
            }

            override fun onSuccess(url: String, l: Long) {
                Log.i("zh___", "onSuccess: url=$url l=$l")
            }

            override fun onFailed(url: String, cancelled: Boolean, msg: String?) {
                Log.i("zh___", "onFailed: url=$url cancelled=$cancelled msg=$msg")
            }
        })
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}


data class AppInfo(
    var name: String,
    var icon: String,
    var packageName: String,
    var url: String
)