package com.hx.infusionchairplateproject.tools

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import com.hx.infusionchairplateproject.R
import com.yzq.zxinglibrary.encode.CodeCreator

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

        /**
         * 根据设备 SN 码生成二维码图片
         * 此处可设置 生成的二维码图片大小
         */
        @SuppressLint("UseCompatLoadingForDrawables")
        fun getTwoDimensionalMap(sn: String, context: Context): Bitmap {
            val drawable = context.resources.getDrawable(R.mipmap.llm_app_icon_playstore)
            val bitmapDrawable = drawable as BitmapDrawable
            val tmp = bitmapDrawable.bitmap
            return CodeCreator.createQRCode(sn + "text=hx", 350, 350, tmp)
        }

    }

}