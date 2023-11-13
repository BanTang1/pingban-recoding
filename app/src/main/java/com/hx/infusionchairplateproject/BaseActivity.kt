package com.hx.infusionchairplateproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hx.infusionchairplateproject.ch340.CH34xManager
import com.hx.infusionchairplateproject.ui.WifiSettingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

        ActivityTask.addActivity(this)

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                isShow = msg.what
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityTask.removeActivity(this)
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

    /**
     * 打开CH340
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            CoroutineScope(Dispatchers.IO).launch {
                CH34xManager.getCH34xManager().openDevices()
            }
        }
    }


    /**
     * 手势跳转设置界面
     * 目前支持 X 和 左上角长按5s
     */
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var firstLineDrawn = false
    private var firstLineTime: Long = 0
    val runnable = Runnable { //执行跳转操作
        val intent = Intent(this@BaseActivity, WifiSettingActivity::class.java)
        intent.putExtra("status", true)
        startActivity(intent)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                if (startX < 300 && startY < 300) {
                    handler?.postDelayed(runnable, 5000)
                }
            }

            MotionEvent.ACTION_UP -> {
                handler?.removeCallbacks(runnable)
                endX = event.x
                endY = event.y

                // 计算用户抬起手指时的坐标与按下时的坐标差
                val deltaX = endX - startX
                val deltaY = endY - startY

                // 判断用户画的线是否足够长
                if (Math.hypot(deltaX.toDouble(), deltaY.toDouble()) < 1650) {
                    return super.onTouchEvent(event)
                }

                // 判断用户画的线的斜率是否接近 1 或 -1
                val slope = deltaY / deltaX
                if (Math.abs(slope) > 0.4 && Math.abs(slope) < 1) {
                    if (!firstLineDrawn) {
                        // 判断第一条对角线的起点是否在屏幕左上角附近
                        if (startX > 300 || startY > 300) {
                            return super.onTouchEvent(event)
                        }

                        // 判断第一条对角线的终点是否在屏幕右下角附近
                        val screenHeight = windowManager.defaultDisplay.height
                        val screenWidth = windowManager.defaultDisplay.width
                        if (endX < screenWidth - 300 || endY < screenHeight - 300) {
                            return super.onTouchEvent(event)
                        }
                        firstLineDrawn = true
                        firstLineTime = System.currentTimeMillis()
                    } else {
                        // 判断两条对角线之间的时间间隔是否超过 2 秒
                        if (System.currentTimeMillis() - firstLineTime > 2000) {
                            firstLineDrawn = false
                            return super.onTouchEvent(event)
                        }
                        // 判断第二条对角线的起点是否在屏幕左下角附近
                        val screenHeight = windowManager.defaultDisplay.height
                        if (startX > 300 || startY < screenHeight - 300) {
                            firstLineDrawn = false
                            return super.onTouchEvent(event)
                        }

                        // 判断第二条对角线的终点是否在屏幕右上角附近
                        val screenWidth = windowManager.defaultDisplay.width
                        if (event.x < screenWidth - 300 || event.y > 300) {
                            firstLineDrawn = false
                            return super.onTouchEvent(event)
                        }

                        // 跳转到某个页面
                        val intent = Intent(this@BaseActivity, WifiSettingActivity::class.java)
                        intent.putExtra("status", true)
                        startActivity(intent)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

}