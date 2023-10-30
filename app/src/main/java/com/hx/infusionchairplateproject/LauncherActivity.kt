package com.hx.infusionchairplateproject

import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.ImageView
import com.hx.infusionchairplateproject.tools.GeneralUtil

/**
 * 启动界面
 */
class LauncherActivity : BaseActivity() {

    private var wifiManager: WifiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mImageView = ImageView(this)
        mImageView.setImageResource(R.drawable.beikang)
        setContentView(mImageView)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            if (!GeneralUtil.isNetWorkConnected(this@LauncherActivity)) {
                // TODO 假设网络已经连接上了; 实际需跳转自身的Wifi界面，暂未实现
//                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
//                finish()
                startActivity(Intent(this@LauncherActivity, LockScreenActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@LauncherActivity, LockScreenActivity::class.java))
                finish()
            }
        }, 0)
        requestWriteSettings()
    }

    /**
     * 申请权限 更改系统熄屏时间 不休眠
     */
    private fun requestWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //大于等于23 请求权限
            if (!Settings.System.canWrite(applicationContext)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 1)
            } else {
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    Int.MAX_VALUE
                )
            }
        } else {
            //小于23直接设置
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                Int.MAX_VALUE
            )
        }
    }
}