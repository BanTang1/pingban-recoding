package com.hx.infusionchairplateproject

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


/**
 * create by zh on 2023/11/2
 */
open class BaseActivity : AppCompatActivity() {

    var isShow by mutableStateOf(0)

    companion object{
        var handler:Handler? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                isShow = msg.what
            }
        }
    }

    @Composable
    fun BaseContent(){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            if (isShow == 1) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 10.dp
                )
            }
        }
    }

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