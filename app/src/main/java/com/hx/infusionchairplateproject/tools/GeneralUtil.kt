package com.hx.infusionchairplateproject.tools

import android.content.Context
import android.net.ConnectivityManager

class GeneralUtil {

    companion object {
        /**
         * 检测网络是否可用
         * 判断当前是否有网络连接,但是如果该连接的网络无法上网，也会返回true
         *
         * @return
         */
        fun isNetWorkConnected(context: Context?): Boolean {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable
                }
            }
            return false
        }
    }

}