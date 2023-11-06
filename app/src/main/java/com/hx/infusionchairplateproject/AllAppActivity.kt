package com.hx.infusionchairplateproject

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.wch.uartlib.chipImpl.f.b.y
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.hx.infusionchairplateproject.viewmodel.AllAppViewModel
import com.hx.infusionchairplateproject.viewmodel.AppInfo

/**
 * 解锁界面
 */
class AllAppActivity : BaseActivity() {

    private val TAG: String = "liudehua_AllAppActivity"
    private val debug: Boolean = true

    private val allAppViewModel: AllAppViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background
                Image(painterResource(id = R.mipmap.llm_all_app_bg), contentDescription = "背景")
                Row(modifier = Modifier.fillMaxSize()) {

                    val selectedOption = remember { mutableStateOf("video") }

                    // left button
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(270.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ThreeButton(selectedOption)
                    }

                    // right apps
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f), contentAlignment = Alignment.Center
                    ) {
                        AllApps(selectedOption)
                    }
                    Spacer(modifier = Modifier.width(80.dp))
                }
            }
        }


        allAppViewModel.getAllApkList()


    }

    @Composable
    fun ThreeButton(selectedOption: MutableState<String>) {

        CustomRadioButton(
            selected = selectedOption.value == "video",
            onClick = {
                selectedOption.value = "video"
                allAppViewModel.getPadApkList("视频")
            },
            unselectedImage = R.mipmap.llm_video_img_default,
            selectedImage = R.mipmap.llm_video_img
        )

        Spacer(modifier = Modifier.height(30.dp))
        CustomRadioButton(
            selected = selectedOption.value == "game",
            onClick = {
                selectedOption.value = "game"
                allAppViewModel.getPadApkList("游戏")
            },
            unselectedImage = R.mipmap.llm_game_img_default,
            selectedImage = R.mipmap.llm_game_img
        )

        Spacer(modifier = Modifier.height(30.dp))
        CustomRadioButton(
            selected = selectedOption.value == "paint",
            onClick = {
                selectedOption.value = "paint"
                allAppViewModel.getPadApkList("绘画")
            },
            unselectedImage = R.mipmap.llm_painting_img_default,
            selectedImage = R.mipmap.llm_painting_img
        )
    }

    @Composable
    fun CustomRadioButton(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        @DrawableRes unselectedImage: Int,
        @DrawableRes selectedImage: Int
    ) {

        val context = LocalContext.current
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val image = if (selected) selectedImage else unselectedImage
        Image(
            painter = painterResource(id = image),
            contentDescription = "左侧按钮图标",
            modifier = modifier
                .clickable(
                    onClick = {
                        audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
                        onClick()
                    },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                )
                .padding(start = 10.dp, end = 10.dp)

        )
    }

    @Composable
    fun AllApps(selectedOption: MutableState<String>) {
        when (selectedOption.value) {
            "video" -> {
                val videoApkList by allAppViewModel.videoApkList.collectAsState()
                AppCard(appList = videoApkList)
            }

            "game" -> {
                val gameApkList by allAppViewModel.gameApkList.collectAsState()
                AppCard(appList = gameApkList)
            }

            "paint" -> {
                val paintApkList by allAppViewModel.paintApkList.collectAsState()
                AppCard(appList = paintApkList)
            }
        }
    }

    @Composable
    fun AppCard(appList: List<AppInfo>) {

        val context = LocalContext.current
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        LazyVerticalGrid(
            modifier = Modifier.height(300.dp),
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(20.dp),
            content = {
                items(appList.size) { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                        .padding(bottom = 40.dp)
                        .clickable(
                            onClick = {
                                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
                                // click app  TODO APP点击后的处理逻辑
                                Log.i("zh___", "AppCard: ${appList[item].packageName}")
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )) {

                        val retryCount = remember { mutableStateOf(0) }
                        val maxRetries = 3      // reconnect count

                        AsyncImage(
                            model = ImageRequest
                                .Builder(LocalContext.current)
                                .data(appList[item].icon)
                                .scale(Scale.FILL)
                                .placeholder(R.mipmap.shibai)
                                .error(R.mipmap.shibai)
                                .listener(
                                    onError = { _, _ ->
                                        if (retryCount.value < maxRetries) {
                                            retryCount.value++
                                        }
                                    })
                                .build(),
                            contentDescription = "APP icon",
                            modifier = Modifier
                                .height(70.dp)
                                .width(70.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(text = appList[item].name, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
        )
    }

}