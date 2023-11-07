package com.hx.infusionchairplateproject.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import com.hjq.toast.Toaster
import com.hx.infusionchairplateproject.databeen.BaseBean
import com.hx.infusionchairplateproject.databeen.PadApkList
import com.hx.infusionchairplateproject.databeen.PadApkLists
import com.hx.infusionchairplateproject.databeen.StartRound
import com.hx.infusionchairplateproject.network.DownloadMgr
import com.hx.infusionchairplateproject.network.NetworkManager
import com.hx.infusionchairplateproject.tools.CommandTool
import com.hx.infusionchairplateproject.tools.GeneralUtil
import com.hx.infusionchairplateproject.tools.SPTool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


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

    fun downLoadApk(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            realDownloadApk(url)
        }
    }


    /**
     * 三方App使用记录-开始
     */
    fun startDeviceLog(sn: String, appId: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val startLog = NetworkManager.getInstance().requestApi.startDeviceLog(sn,appId)
                startLog.enqueue(object : Callback<StartRound>{
                    override fun onResponse(call: Call<StartRound>, response: Response<StartRound>) {
                        if (!response.isSuccessful) {
                            if (debug) Log.d(TAG, "onResponse: 三方App使用记录-开始 响应异常")
                            return
                        }
                        if (response.code() != 200){
                            if (debug) Log.d(TAG, "onResponse: 三方App使用记录-开始响应码异常 Code = ${response.code()}")
                            return
                        }
                        val startRound = response.body()
                        if (startRound == null) {
                            if (debug) Log.d(TAG, "onResponse: 三方App使用记录-开始响应为空")
                            return
                        }
                        if (startRound.status != 200) {
                            if (debug) Log.d(TAG, "onResponse: 三方App使用记录-开始响应数据中的 status(code) ！= 200")
                            return
                        }

                        // 标志： 成功

                        SPTool.putString("startRecordAppId",startRound.data.id)
                    }

                    override fun onFailure(call: Call<StartRound>, t: Throwable) {
                        if (debug) Log.d(TAG, "onFailure: t = $t")
                    }

                })
            }
        }
    }

    /**
     * 三方App使用记录-结束
     */
    fun endDeviceLog(appId: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val endLog = NetworkManager.getInstance().requestApi.endDeviceLog(appId)
                endLog.enqueue(object : Callback<BaseBean<String>>{
                    override fun onResponse(call: Call<BaseBean<String>>, response: Response<BaseBean<String>>) {
                        if (!response.isSuccessful) {
                            if (debug) Log.d(TAG, "onResponse: 三方App使用记录-结束 响应异常")
                            return
                        }
                        if (response.code() != 200){
                            if (debug) Log.d(TAG, "onResponse: 三方App使用记录-结束 响应码异常 Code = ${response.code()}")
                            return
                        }
                        val endRound = response.body()
                        if (endRound == null) {
                            if (debug) Log.d(TAG, "onResponse: 三方App使用记录-结束 响应为空")
                            return
                        }
                        if (endRound.status != 200) {
                            if (debug) Log.d(TAG, "onResponse: 三方App使用记录-结束 响应数据中的 status(code) ！= 200")
                            return
                        }

                        // 标志 ： 成功

                        SPTool.putString("startRecordAppId","")
                    }

                    override fun onFailure(call: Call<BaseBean<String>>, t: Throwable) {
                        if (debug) Log.d(TAG, "onFailure: t = $t")
                        SPTool.putString("startRecordAppId","")
                    }

                })
            }
        }
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
                    val appInfo = AppInfo(data[i].id, data[i].name, data[i].icon, data[i].packageName, data[i].apkUrl)
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
                for (apk in localAPK) {
                    if (apk !in serverAPK) {
                        val file = File("/sdcard/TripartiteApp/$apk")
                        if (file.exists()) {
                            file.deleteRecursively()
                        }
                    }
                }
                localAPK.retainAll { it in serverAPK }

                if (differentData.isNotEmpty()) {
                    for (apkName in differentData) {
                        // go to download
                        realDownloadApk("https://llm-huxing.oss-cn-beijing.aliyuncs.com/attachment/$apkName")
                    }
                }


            }

            override fun onFailure(call: Call<PadApkLists>, t: Throwable) {
                if (debug) Log.d(TAG, "onFailure: t = $t")
            }

        })
    }

    /**
     * 正式下载并更新下载进度
     * link method : see updateDownloadProgress(url: String, progress: Long)
     */
    @SuppressLint("SdCardPath")
    private fun realDownloadApk(url: String) {
        // update app state
        updateDownloadProgress(url, 0)
        // add download task
        DownloadMgr.getInstance().addTask(url, "/sdcard/TripartiteApp/${url.substringAfterLast("/")}", object : DownloadMgr.Callback() {
            override fun onStart(url: String) {
                updateDownloadProgress(url, 0)
            }

            override fun onProgress(url: String, progress: Long, total: Long) {
                updateDownloadProgress(url, progress * 100 / total)
            }

            override fun onSuccess(url: String, l: Long) {
                // start install app
                installApp(url)
            }

            override fun onFailed(url: String, cancelled: Boolean, msg: String?) {
                Toaster.showShort(msg)
                updateDownloadProgress(url, -1)
            }
        })
    }

    /**
     * 安装指定APP
     * url 中包含文件名
     */
    private fun installApp(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            CommandTool.execSuCMD("pm install -r /sdcard/TripartiteApp/${url.substringAfterLast("/")}")
            updateDownloadProgress(url, (-1).toLong())
        }
    }


    /**
     * url ： 指定App，唯一标识符
     * progress : 进度 ， 含义如下
     * 0 : 开始 ;
     * 0..99 : 进度 ;
     * 100 : 开始安装 ;
     * -1 : 默认状态（可用）
     */
    private fun updateDownloadProgress(url: String, progress: Long) {
        val tmpVideoApkList = _videoApkList.value.toMutableList()
        tmpVideoApkList.mapIndexed { index, appInfo ->
            if (appInfo.url == url) {
                tmpVideoApkList[index] = appInfo.copy(progress = progress)
            }
        }
        _videoApkList.value = tmpVideoApkList

        val tmpGameApkList = _gameApkList.value.toMutableList()
        tmpGameApkList.mapIndexed { index, appInfo ->
            if (appInfo.url == url) {
                tmpGameApkList[index] = appInfo.copy(progress = progress)
            }
        }
        _gameApkList.value = tmpGameApkList

        val tmpPaintApkList = _paintApkList.value.toMutableList()
        tmpPaintApkList.mapIndexed { index, appInfo ->
            if (appInfo.url == url) {
                tmpPaintApkList[index] = appInfo.copy(progress = progress)
            }
        }
        _paintApkList.value = tmpPaintApkList
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}


data class AppInfo(
    var id: String,
    var name: String,
    var icon: String,
    var packageName: String,
    var url: String,
    var progress: Long = -1
)