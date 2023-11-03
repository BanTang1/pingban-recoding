package com.hx.infusionchairplateproject.viewmodel

import androidx.lifecycle.ViewModel
import com.hx.infusionchairplateproject.network.NetworkManager

class UnLockViewModel : ViewModel() {

    private val netManager: NetworkManager = NetworkManager.getInstance()


    fun getPadApkList(type: String) {

    }

}