package com.hx.infusionchairplateproject

import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    /**
     * 全屏沉浸隐藏底部导航栏 设置状态篮字体颜色View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 黑
     *
     * @param hasFocus
     */
    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }

    /**
     * Back键和Menu键可以通过重写onKeyDown()方法进行屏蔽：
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val key = event.keyCode
        when (key) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_HOME -> return true
            else -> {}
        }
        return super.onKeyDown(keyCode, event)
    }

}