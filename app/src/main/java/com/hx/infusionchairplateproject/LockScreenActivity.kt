package com.hx.infusionchairplateproject


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.hx.infusionchairplateproject.viewmodel.LockViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import com.hx.infusionchairplateproject.tools.GeneralUtil
import com.hx.infusionchairplateproject.viewmodel.SocketViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope


class LockScreenActivity : BaseActivity() {

    private val TAG:String = "liudehua-LockScreenActivity"
    private val debug:Boolean = false
    private val lockViewModel:LockViewModel by viewModels()
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var snAddress:String

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null) return

        val control = intent.getStringExtra("control")
        if (control == "abort"){
            updateAllDate()
        }
    }

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        socketViewModel = (application as EntiretyApplication).getSocketViewModel()

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background
                Image(painterResource(id = R.mipmap.llm3), contentDescription = "背景")
                Row (modifier = Modifier.fillMaxSize()){

                    // left
                    Column(modifier = Modifier
                        .fillMaxHeight()
                        .width(214.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(114.dp))
                        // 二维码
                        TwoDimensionalCode()
                        Spacer(modifier = Modifier.height(100.dp))
                        // 套餐信息
                        Column(modifier = Modifier.weight(1f)) {
                            PriceInformation()
                        }
                    }

                    // right banner
                    Box(modifier = Modifier.fillMaxSize()){
                        Banner()
                        Column(modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 50.dp, end = 30.dp)) {
                            ShowPromptMessage()
                        }

                        Column(modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 50.dp, end = 220.dp)) {
                            Image(
                                painterResource(id = R.mipmap.xiongmao),
                                contentDescription = "熊猫LOGO",
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(100.dp)
                            )
                        }
                    }
                }

                // 设备投放状态
                checkDeviceState()
            }
        }

        snAddress = (application as EntiretyApplication).getSnAddress()
        if (debug) Log.d(TAG, "onCreate: snAddress = $snAddress")

        updateAllDate()

    }

    /**
     * 从服务器获取所有需要的信息
     */
    private fun updateAllDate(){
        lockViewModel.apply {
            updateInfo(snAddress)
            updatePromptMessage(this@LockScreenActivity)
            updateNetState(this@LockScreenActivity)
            updatePutInState(snAddress)
        }
    }

    @Composable
    private fun checkDeviceState() {
        val putInState by lockViewModel.putInState.collectAsState()
        val recvIsPutIn by socketViewModel.isPutIn.collectAsState()
        val recvScanState by socketViewModel.putInIsScan.collectAsState()

        val bitmap:ImageBitmap = GeneralUtil.getTwoDimensionalMap(snAddress, LocalContext.current).asImageBitmap()
        if (!putInState) {
            if (!recvIsPutIn) {
                Box{
                    Image(painterResource(id = R.mipmap.llm_device_id), contentDescription = "投放页背景")
                    Image(bitmap = bitmap, contentDescription = "投放二维码", modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(30.dp, 80.dp))

                    when(recvScanState) {
                        0 -> {}
                        1 -> {
                            Image(bitmap = ImageBitmap.imageResource(R.mipmap.scan_ok), contentDescription = "扫码成功",modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(30.dp, 110.dp))
                        }
                        2 -> {
                            Image(bitmap = ImageBitmap.imageResource(R.mipmap.scan_no), contentDescription = "扫码失败",modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(30.dp, 110.dp))
                        }
                        3 -> {
                            Image(bitmap = ImageBitmap.imageResource(R.mipmap.scan_refuse), contentDescription = "已拒绝本次扫码",modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(30.dp, 110.dp))
                        }
                    }

                }
            }
        }
    }


    @Composable
    fun ShowPromptMessage(){
        val viewModel:LockViewModel = viewModel()
        val version by viewModel.version.collectAsState()
        val netState by viewModel.netState.collectAsState()

        Text(text = "温馨提示",fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(text = "本机维护时间凌晨4~7点",fontWeight = FontWeight.Bold,fontSize = 17.sp)
        Text(text = "维护期间机器暂停使用",fontWeight = FontWeight.Bold,fontSize = 17.sp)
        Text(text = "网络状态: ${if (netState) "网络正常" else "网络异常"}",
            fontWeight = FontWeight.Bold, fontSize = 17.sp,
            color = if (netState) Color.Black else Color.Red)
        Text(text = "版本号: $version",fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
    
    @Composable
    fun PriceInformation(){
        val viewModel:LockViewModel = viewModel()
        val priceInformationList by viewModel.priceInformation.collectAsState()
        if (priceInformationList.size > 5) return
        var type:String
        var id: Int
        for (i in 0 until priceInformationList.size) {
            Spacer(modifier = Modifier.height(10.dp))
            when (i) {
                0 -> {
                    type = "沉浸减压 "
                    id = R.drawable.llm_cjjy_icon
                }
                1 -> {
                    type = "萌娃专享 "
                    id = R.drawable.llm_mwzx_icon
                }
                2 -> {
                    type = "新游速递 "
                    id = R.drawable.llm_xysd_icon
                }
                3 -> {
                    type = "活力畅影 "
                    id = R.drawable.llm_hlcy_icon
                }
                4 -> {
                    type = "快乐加倍 "
                    id = R.drawable.llm_kljb_icon
                }
                else -> {
                    type = "沉浸减压 "
                    id = R.drawable.llm_cjjy_icon
                }
            }
            Row{
                Icon(painter = painterResource(id = id),modifier = Modifier
                    .width(20.dp)
                    .height(20.dp),contentDescription = "套餐图标-$i",
                    tint = Color.Unspecified)
                Text(text = type + priceInformationList[i],fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Banner(){
        val viewModel: LockViewModel = viewModel()
        val imageList by viewModel.imageList.collectAsState()
        val pagerState = rememberPagerState()
        val isDragged = pagerState.interactionSource.collectIsDraggedAsState()
        val scope = rememberCoroutineScope()

        LaunchedEffect(pagerState.settledPage) {
            delay(3000)
            if (imageList.isNotEmpty()) {
                val scoller =
                    if (pagerState.currentPage + 1 == imageList.size) 0 else pagerState.currentPage + 1
                pagerState.animateScrollToPage(scoller)
            }
        }

        Box {
            HorizontalPager(
                state = pagerState,
                pageCount = imageList.size,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val imageScale by animateFloatAsState(
                    targetValue = if (pagerState.currentPage == it) 1f else 0.8f,
                    animationSpec = tween(500), label = "动画"
                )

                val retryCount = remember { mutableStateOf(0) }
                val maxRetries = 3      // reconnect count

                AsyncImage(
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageList[it])
                        .scale(Scale.FILL)
                        .placeholder(R.mipmap.shibai)
                        .error(R.mipmap.shibai)
                        .listener(
                            onError = { _, _->
                                if (retryCount.value < maxRetries) {
                                    retryCount.value++
                                }
                            }
                        )
                        .build(),
                    contentDescription = "图片$it",
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(imageScale),
                    contentScale = ContentScale.FillBounds
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                imageList.indices.forEach { index ->
                    RadioButton(selected = pagerState.currentPage == index, onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    })
                }
            }
        }
    }

    @Composable
    fun TwoDimensionalCode(){
        val viewModel: LockViewModel = viewModel()
        val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()
        val scanState by socketViewModel.screenIsScan.collectAsState()

        val retryCount = remember { mutableStateOf(0) }
        val maxRetries = 3      // reconnect count

        if (qrCodeBitmap != "") {
            Box{
                AsyncImage(
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(qrCodeBitmap)
                        .scale(Scale.FILL)
                        .placeholder(R.mipmap.shibai)
                        .error(R.mipmap.shibai)
                        .listener(
                            onError = { _, _->
                                if (retryCount.value < maxRetries) {
                                    retryCount.value++
                                }
                            })
                        .build(),
                    contentDescription = "二维码",
                    modifier = Modifier
                        .height(100.dp)
                        .width(100.dp),
                    contentScale = ContentScale.FillBounds
                )

                when(scanState) {
                    0 -> {}
                    1 -> {
                        Image(bitmap = ImageBitmap.imageResource(R.mipmap.scan_ok), contentDescription = "扫码成功",modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 30.dp)
                            .width(50.dp)
                            .height(50.dp))
                    }
                    2 -> {
                        Image(bitmap = ImageBitmap.imageResource(R.mipmap.scan_no), contentDescription = "扫码失败",modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 30.dp)
                            .width(50.dp)
                            .height(50.dp))
                    }
                    3 -> {
                        Image(bitmap = ImageBitmap.imageResource(R.mipmap.scan_refuse), contentDescription = "扫码拒绝",modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 30.dp)
                            .width(50.dp)
                            .height(50.dp))
                    }
                }
            }
        }
    }


}
