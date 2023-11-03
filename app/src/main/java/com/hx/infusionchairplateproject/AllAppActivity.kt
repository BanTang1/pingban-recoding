package com.hx.infusionchairplateproject

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hx.infusionchairplateproject.viewmodel.UnLockViewModel

/**
 * 解锁界面
 */
class AllAppActivity : BaseActivity() {

    private val TAG: String = "liudehua_AllAppActivity"
    private val debug: Boolean = true
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
    }

    @Composable
    fun ThreeButton(selectedOption: MutableState<String>) {

        CustomRadioButton(
            selected = selectedOption.value == "video",
            onClick = {
                selectedOption.value = "video"
            },
            unselectedImage = R.mipmap.llm_video_img_default,
            selectedImage = R.mipmap.llm_video_img
        )

        Spacer(modifier = Modifier.height(30.dp))
        CustomRadioButton(
            selected = selectedOption.value == "game",
            onClick = {
                selectedOption.value = "game"
            },
            unselectedImage = R.mipmap.llm_game_img_default,
            selectedImage = R.mipmap.llm_game_img
        )

        Spacer(modifier = Modifier.height(30.dp))
        CustomRadioButton(
            selected = selectedOption.value == "paint",
            onClick = {
                selectedOption.value = "paint"
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

                val unLockViewModel: UnLockViewModel = viewModel()
                unLockViewModel.getPadApkList("")

                val items = (1..10).toList()

                var TmpModifier: Modifier = Modifier
                if (items.size > 10) {
                    TmpModifier = Modifier.width(600.dp).height(270.dp)
                } else if (items.size <= 5) {
                    TmpModifier = Modifier.offset(y = (-80).dp)
                }

                LazyVerticalGrid(
                    modifier = TmpModifier,
                    columns = GridCells.Fixed(5),
                    contentPadding = PaddingValues(20.dp),
                    content = {
                        items(items.size) { item ->
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),

                                ) {
                                Column {
                                    Image(painterResource(id = R.mipmap.scan_ok), contentDescription = "右侧APP图标")
                                    Text(text = "腾讯视屏")
                                }
                            }
                        }
                    }
                )

            }

            "game" -> {

            }

            "paint" -> {

            }
        }
    }

}