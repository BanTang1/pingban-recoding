package com.hx.infusionchairplateproject.viewmodel

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow


/**
 * Application 数据模型
 * 用于WebSocket的数据管理
 */
class SocketViewModel(application: Application) : AndroidViewModel(application){

    // 投放状态
    var isPutIn = MutableStateFlow(false)

    // 扫码状态
    val SCAN_STATE_DEFAULT = "SCAN_STATE_DEFAULT"
    val SCAN_STATE_OK = "SCAN_STATE_OK"
    val SCAN_STATE_NO = "SCAN_STATE_NO"
    var isScan = MutableStateFlow(SCAN_STATE_DEFAULT)

}