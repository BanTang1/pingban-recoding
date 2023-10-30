package com.hx.infusionchairplateproject


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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.hx.infusionchairplateproject.viewmodel.LockViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LockScreenActivity : BaseActivity() {

    private val TAG:String = "liudehua-LockScreenActivity"
    private val debug:Boolean = false
    private val lockViewModel:LockViewModel by viewModels()
    private lateinit var snAddress:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    Box(modifier = Modifier.weight(1f)){
                        Banner()
                        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 50.dp, end = 30.dp)) {
                            ShowPromptMessage()
                        }
                    }
                }
            }
        }

//        snAddress = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        snAddress = "7726c6b1e1963a52"
        if (debug) Log.d(TAG, "onCreate: snAddress = $snAddress")


        lockViewModel.apply {
            updateInfo(snAddress)
            updatePromptMessage(this@LockScreenActivity)
            updateNetState(this@LockScreenActivity)
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
            color = if (netState) Color.White else Color.Red)
        Text(text = "版本号: $version",fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
    
    @Composable
    fun PriceInformation(){
        val viewModel:LockViewModel = viewModel()
        val priceInformationList by viewModel.priceInformation.collectAsState()
        if (priceInformationList.isEmpty()) return
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
        if (imageList.isEmpty()) return
        val pagerState = rememberPagerState()
        val isDragged = pagerState.interactionSource.collectIsDraggedAsState()
        val scope = rememberCoroutineScope()
        LaunchedEffect(pagerState.settledPage) {
            delay(3000)
            val scoller =
                if (pagerState.currentPage + 1 == imageList.size) 0 else pagerState.currentPage + 1
            pagerState.animateScrollToPage(scoller)
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
        if(qrCodeBitmap == "") { return }

        val retryCount = remember { mutableStateOf(0) }
        val maxRetries = 3      // reconnect count

        AsyncImage(
            model = ImageRequest
                .Builder(LocalContext.current)
                .data(qrCodeBitmap)
                .scale(Scale.FILL)
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
    }


}


@Preview(showBackground = true, widthDp = 1920, heightDp = 1104)
@Composable
fun show(){

}
