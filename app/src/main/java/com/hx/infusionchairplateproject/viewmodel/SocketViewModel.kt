package com.hx.infusionchairplateproject.viewmodel

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow


/**
 * Application 数据模型
 * 用于WebSocket的数据管理
 */
class SocketViewModel(application: Application) : AndroidViewModel(application){

    // 二维码状态
    val SCAN_STATE_DEFAULT = 0
    val SCAN_STATE_OK = 1
    val SCAN_STATE_NO = 2
    val SCAN_STATE_REFUSE = 3

    // 投放状态
    var isPutIn = MutableStateFlow(false)

    // 扫码状态（投放页面）
    var putInIsScan = MutableStateFlow(SCAN_STATE_DEFAULT)

    // 扫码状态 （锁屏页面）
    var screenIsScan = MutableStateFlow(SCAN_STATE_DEFAULT)

}