//package com.hx.infusionchairplateproject.ui;
//
//import static android.view.View.VISIBLE;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.media.AudioManager;
//import android.os.CountDownTimer;
//import android.util.DisplayMetrics;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.SeekBar;
//
//import com.hx.infusionchairplateproject.R;
//import com.lzf.easyfloat.EasyFloat;
//import com.lzf.easyfloat.anim.DefaultAnimator;
//import com.lzf.easyfloat.enums.ShowPattern;
//import com.lzf.easyfloat.enums.SidePattern;
//import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
//import com.lzf.easyfloat.interfaces.OnInvokeView;
//import com.lzf.easyfloat.utils.DefaultDisplayHeight;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//public class FloatingWindow {
//
//    Context context;
//    CountDownTimer myCountDownTimer;
//    ImageView room_rl_header;
//    SeekBar mCarBrightnessVolume;
//    RelativeLayout ll_suo;
//    ImageView imgR;
//    ImageView imgV;
//    AudioManager audio;
//
//    public FloatingWindow(Context context){
//        this.context = context;
//    }
//
//    private int getScreenHeight() {
//        DisplayMetrics metric = new DisplayMetrics();
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        wm.getDefaultDisplay().getMetrics(metric);
//        return metric.heightPixels;
//    }
//
//    private int getScreenWidth() {
//        DisplayMetrics metric = new DisplayMetrics();
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        wm.getDefaultDisplay().getMetrics(metric);
//        return metric.widthPixels;
//    }
//
//    private int dip2px(float dip) {
//        float density = context.getResources().getDisplayMetrics().density;
//        return (int) (dip * density + 0.5f);
//    }
//
//    public void showFlow() {
//        int[] location = new int[2];
//        audio = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
//        if (EasyFloat.getFloatView("tag_showFlow") == null) {
//            EasyFloat.with(((Activity) context).getApplication())
//                    .setTag("tag_showFlow")
//                    .setShowPattern(ShowPattern.ALL_TIME)
//                    .setSidePattern(SidePattern.RESULT_RIGHT)
//                    .setLocation(getScreenWidth() - dip2px(32), dip2px(40))
//                    .setAnimator(null)
//                    .setDragEnable(true)
//                    .setMatchParent(false, false)
//                    .setAnimator(new DefaultAnimator())
//                    .setImmersionStatusBar(true)
//                    .setDisplayHeight(new DefaultDisplayHeight())
//                    .setFilter(
//                            LockScreenActivity.class
//                    ).registerCallbacks(new OnFloatCallbacks() {
//                        @Override
//                        public void createdResult(boolean b, @Nullable String s, @Nullable View view) {
//
//                        }
//
//                        @Override
//                        public void show(@NotNull View view) {
//
//                        }
//
//                        @Override
//                        public void hide(@NotNull View view) {
//
//                        }
//
//                        @Override
//                        public void dismiss() {
//
//                        }
//
//                        @Override
//                        public void touchEvent(@NotNull View view, @NotNull MotionEvent motionEvent) {
//
//                        }
//
//                        @Override
//                        public void drag(@NotNull View view, @NotNull MotionEvent motionEvent) {
//                            //拖拽过程不能虚化
//                            /**
//                             *  倒计时结束
//                             */
//                            myCountDownTimer.cancel();
//                            room_rl_header.setBackgroundResource(R.mipmap.llm_setting_icom);
//                        }
//
//                        @Override
//                        public void dragEnd(@NotNull View view) {
//                            /**
//                             *  倒计时结束
//                             */
//                            myCountDownTimer.start();
//                        }
//                    })
//                    .setLayout(R.layout.home_float, new OnInvokeView() {
//                        @Override
//                        public void invoke(View view) {
//                            room_rl_header = view.findViewById(R.id.room_rl_header);
//                            ll_suo = view.findViewById(R.id.ll_suo);
//                            imgR = view.findViewById(R.id.imgR);
//                            imgV = view.findViewById(R.id.imgV);
//                            int current = audio.getStreamVolume(AudioManager.STREAM_MUSIC); //获取当前值
//                            if (current != 0) {
//                                imgV.setBackgroundResource(R.mipmap.llm_volume);
//                            } else {
//                                imgV.setBackgroundResource(R.mipmap.llm_volume_mute);
//                            }
//                            ImageView iv_home = view.findViewById(R.id.iv_home);
//                            iv_home.setOnClickListener(v -> {
//                                myCountDownTimer.cancel();
//                                myCountDownTimer.start();
//
//                                if (isTop) {
////                                    ToastUtil.showToast(SecondAppActivity.this,"当前界面不操作~");
//                                    //如果是当前界面就不操作
//                                    return;
//                                }
////                                myCountDownTimer.start();
//
////                                long time = PrefUtils.getLong("time", 0);
////                                if (System.currentTimeMillis() < time) {
//                                startActivity(new Intent(SecondAppActivity.this, SecondAppActivity.class));
////                                }
//
//                                if (EasyFloat.getFloatView("tag_R") != null) {
//                                    EasyFloat.hide("tag_R");
//                                }
//                                if (EasyFloat.getFloatView("tag_V") != null) {
//                                    EasyFloat.hide("tag_V");
//                                }
//                                if (imgV.getAnimation() != null) {
//                                    imgV.getAnimation().cancel();
//                                }
//                                if (imgR.getAnimation() != null) {
//                                    imgR.getAnimation().cancel();
//                                }
//                                if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                    EasyFloat.dragEnable(true, "tag_showFlow");
//                                }
//                            });
//                            room_rl_header.setOnClickListener(v -> {
//                                if (ll_suo.getVisibility() == VISIBLE) {
////                                    myCountDownTimer.start();
//
////                                    seekBar.setVisibility(View.GONE);
////                                    mCarBrightnessVolume.setVisibility(View.GONE);
//                                    view.getLocationOnScreen(location);//获得 View 相对 屏幕 的绝对坐标
//                                    ll_suo.setVisibility(View.GONE);
////                                    if (EasyFloat.getFloatView("tag_showFlow") != null) {
////                                        EasyFloat.updateFloat("tag_showFlow",
////                                                ScreenUtils.getScreenWidth(SecondAppActivity.this) - UIUtils.dip2px(32),
////                                                UIUtils.dip2px(40)// view距离 屏幕顶边的距离（即y轴方向
////                                        );
////                                    }
//                                } else {
//                                    /**
//                                     *  倒计时结束
//                                     */
//                                    myCountDownTimer.cancel();
//                                    myCountDownTimer.start();
//                                    room_rl_header.setVisibility(View.GONE);
//                                    room_rl_header.setBackgroundResource(R.mipmap.llm_setting_icom);
//                                    ll_suo.setVisibility(VISIBLE);
//                                }
//                                if (EasyFloat.getFloatView("tag_R") != null) {
//                                    EasyFloat.hide("tag_R");
//                                }
//                                if (EasyFloat.getFloatView("tag_V") != null) {
//                                    EasyFloat.hide("tag_V");
//                                }
//                                if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                    EasyFloat.dragEnable(true, "tag_showFlow");
//                                }
//                                if (imgV.getAnimation() != null) {
//                                    imgV.getAnimation().cancel();
//                                }
//                                if (imgR.getAnimation() != null) {
//                                    imgR.getAnimation().cancel();
//                                }
//                            });
//                            ImageView cancle = view.findViewById(R.id.cancle);
//                            cancle.setOnClickListener(v -> {
//                                setCustomDensity(SecondAppActivity.this, GooglePlayApplication.app);
//                                if (ll_suo.getVisibility() == VISIBLE) {
//                                    /**
//                                     * 开始倒计时
//                                     */
//                                    myCountDownTimer.start();
//                                    room_rl_header.setVisibility(VISIBLE);
//                                    view.getLocationOnScreen(location);
////                                    seekBar.setVisibility(View.GONE);
////                                    mCarBrightnessVolume.setVisibility(View.GONE);
//                                    ll_suo.setVisibility(View.GONE);
////                                    if (EasyFloat.getFloatView("tag_showFlow") != null) {
////                                        EasyFloat.updateFloat("tag_showFlow",
////                                                ScreenUtils.getScreenWidth(SecondAppActivity.this) - UIUtils.dip2px(32),
////                                                UIUtils.dip2px(40)// view距离 屏幕顶边的距离（即y轴方向
////                                        );
////                                    }
//                                } else {
//
//                                    ll_suo.setVisibility(VISIBLE);
//                                }
//
//                                if (EasyFloat.getFloatView("tag_R") != null) {
//                                    EasyFloat.hide("tag_R");
//                                }
//                                if (EasyFloat.getFloatView("tag_V") != null) {
//                                    EasyFloat.hide("tag_V");
//                                }
//                                if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                    EasyFloat.dragEnable(true, "tag_showFlow");
//                                }
//                                if (imgV.getAnimation() != null) {
//                                    imgV.getAnimation().cancel();
//                                }
//                                if (imgR.getAnimation() != null) {
//                                    imgR.getAnimation().cancel();
//                                }
//                            });
//
//
//                            imgR.setOnClickListener(v -> {
//                                        setCustomDensity(SecondAppActivity.this, GooglePlayApplication.app);
//                                        view.getLocationOnScreen(location);//获得 View 相对 屏幕 的绝对坐标
//                                        myCountDownTimer.cancel();
//                                        myCountDownTimer.start();
//                                        if (EasyFloat.getFloatView("tag_R") == null) {
//                                            EasyFloat.with(getApplication())
//                                                    .setTag("tag_R")
//                                                    .setShowPattern(ShowPattern.ALL_TIME)
//                                                    .setSidePattern(SidePattern.RIGHT)
//                                                    .setLocation(ScreenUtils.getScreenWidth(SecondAppActivity.this) - UIUtils.dip2px(180), location[1] + UIUtils.dip2px(70))
//                                                    .setAnimator(null)
//                                                    .setDragEnable(false)
//                                                    .setImmersionStatusBar(true)
//
//                                                    .setMatchParent(false, false)
////                                                    .setAnimator(new DefaultAnimator())
//                                                    .setDisplayHeight(new DefaultDisplayHeight())
//                                                    .setFilter(
//
//                                                    )
//                                                    .setLayout(R.layout.float_r, new OnInvokeView() {
//                                                        @SuppressLint("NewApi")
//                                                        @Override
//                                                        public void invoke(View mMenuView) {
////                                                            myCountDownTimer.cancel();
//                                                            if (EasyFloat.getFloatView("tag_V") != null) {
//                                                                EasyFloat.hide("tag_V");
//                                                            }
//                                                            if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                                                EasyFloat.dragEnable(false, "tag_showFlow");
//                                                            }
//                                                            if (imgV.getAnimation() != null) {
//                                                                imgV.getAnimation().cancel();
//                                                            }
//
//                                                            dongHuaScaleAnimation(imgR);
//                                                            SeekBar seekBar = mMenuView.findViewById(R.id.SeekBar);
////                                                    TextView tv_Brightness = mMenuView.findViewById(R.id.tv_Brightness);
//                                                            //将系统最大屏幕亮度值设为seekbar的最大进度值
//                                                            seekBar.setMax(BrightnessUtil.getMaxBrightness(SecondAppActivity.this));
//                                                            // 设置最小值
//                                                            seekBar.setMin(10);
//                                                            //将系统当前屏幕亮度值设为seekbar当前进度值
//                                                            int brightness = BrightnessUtil.getBrightness(SecondAppActivity.this);
//                                                            seekBar.setProgress(brightness);
////                                                    tv_Brightness.setText("" + brightness);
//                                                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                                                                @Override
//                                                                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//                                                                    ModifySettingsScreenBrightness(SecondAppActivity.this, progress);
////                                                            tv_Brightness.setText("" + progress);
////                                                            if (progress!=0){
////                                                                imgR.setBackgroundResource(R.mipmap.llm_brightness);
////                                                            }else{
////                                                                imgR.setBackgroundResource(R.mipmap.llm_volume);
////                                                            }
//                                                                    /**
//                                                                     *  倒计时结束
//                                                                     */
//                                                                    myCountDownTimer.cancel();
//                                                                }
//
//                                                                @Override
//                                                                public void onStartTrackingTouch(SeekBar seekBar) {
//
//                                                                }
//
//                                                                @Override
//                                                                public void onStopTrackingTouch(SeekBar seekBar) {
//                                                                    /**
//                                                                     *  倒计时开始
//                                                                     */
//                                                                    myCountDownTimer.start();
//                                                                }
//                                                            });
//
//                                                        }
//                                                    }).show();
//                                        } else {
//                                            if (EasyFloat.getFloatView("tag_V") != null) {
//                                                EasyFloat.hide("tag_V");
//                                            }
//                                            boolean tag_v = EasyFloat.isShow("tag_R");
//                                            if (tag_v) {
////                                                myCountDownTimer.start();
//                                                //结束动画
//                                                if (imgR.getAnimation() != null) {
//                                                    imgR.getAnimation().cancel();
//                                                }
//
//                                                if (EasyFloat.getFloatView("tag_R") != null) {
//                                                    EasyFloat.hide("tag_R");
//                                                }
//                                                if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                                    EasyFloat.dragEnable(true, "tag_showFlow");
//                                                }
//                                            } else {
////                                                myCountDownTimer.cancel();
//                                                //开始动画
//                                                if (imgV.getAnimation() != null) {
//                                                    imgV.getAnimation().cancel();
//                                                }
//                                                dongHuaScaleAnimation(imgR);
//                                                if (EasyFloat.getFloatView("tag_R") != null) {
//                                                    EasyFloat.updateFloat("tag_R",
//                                                            ScreenUtils.getScreenWidth(SecondAppActivity.this) - UIUtils.dip2px(180),
//                                                            location[1] + UIUtils.dip2px(70)// view距离 屏幕顶边的距离（即y轴方向
//                                                    );
//                                                }
//
//
//                                                if (EasyFloat.getFloatView("tag_R") != null) {
//                                                    EasyFloat.show("tag_R");
//                                                }
//                                                if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                                    EasyFloat.dragEnable(false, "tag_showFlow");
//                                                }
//                                            }
//
//                                        }
//
//                                    }
//                            );
//                            imgV.setOnClickListener(v -> {
//                                setCustomDensity(SecondAppActivity.this, GooglePlayApplication.app);
//                                view.getLocationOnScreen(location);//获得 View 相对 屏幕 的绝对坐标
//                                myCountDownTimer.cancel();
//                                myCountDownTimer.start();
//                                if (EasyFloat.getFloatView("tag_V") == null) {
//                                    EasyFloat.with(getApplication())
//                                            .setTag("tag_V")
//                                            .setShowPattern(ShowPattern.ALL_TIME)
//                                            .setSidePattern(SidePattern.RIGHT)
//                                            .setLocation(ScreenUtils.getScreenWidth(SecondAppActivity.this) - UIUtils.dip2px(180), location[1] + UIUtils.dip2px(105))
//                                            .setAnimator(null)
//                                            .setImmersionStatusBar(true)
//
//                                            .setDragEnable(false)
//                                            .setMatchParent(false, false)
////                                            .setAnimator(new DefaultAnimator())
//                                            .setDisplayHeight(new DefaultDisplayHeight())
//                                            .setFilter(
//
//                                            )
//                                            .setLayout(R.layout.float_r, new OnInvokeView() {
//                                                @Override
//                                                public void invoke(View mMenuView) {
////                                                    myCountDownTimer.cancel();
//                                                    if (EasyFloat.getFloatView("tag_R") != null) {
//                                                        EasyFloat.hide("tag_R");
//                                                    }
//                                                    if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                                        EasyFloat.dragEnable(false, "tag_showFlow");
//                                                    }
//                                                    if (imgR.getAnimation() != null) {
//                                                        imgR.getAnimation().cancel();
//                                                    }
//
//                                                    dongHuaScaleAnimation(imgV);
//
//                                                    mCarBrightnessVolume = mMenuView.findViewById(R.id.SeekBar);
//                                                    int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//                                                    //将系统最大屏幕音量值设为seekbar的最大进度值
//                                                    mCarBrightnessVolume.setMax(maxVolume);
//                                                    int current = audio.getStreamVolume(AudioManager.STREAM_MUSIC); //获取当前值
//                                                    //将系统当前屏幕音量值设为seekbar当前进度值
//                                                    mCarBrightnessVolume.setProgress(current);
//                                                    if (current != 0) {
//                                                        imgV.setBackgroundResource(R.mipmap.llm_volume);
//                                                    } else {
//                                                        imgV.setBackgroundResource(R.mipmap.llm_volume_mute);
//                                                    }
////                                                    tv_Brightness.setText("" + current);
//                                                    mCarBrightnessVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                                                        @Override
//                                                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//                                                            audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
////                                                            tv_Brightness.setText("" + progress);
//                                                            if (progress != 0) {
//                                                                imgV.setBackgroundResource(R.mipmap.llm_volume);
//                                                            } else {
//                                                                imgV.setBackgroundResource(R.mipmap.llm_volume_mute);
//                                                            }
//
//                                                            /**
//                                                             *  倒计时结束
//                                                             */
//                                                            myCountDownTimer.cancel();
//                                                            /**
//                                                             *  倒计时开始
//                                                             */
//                                                            myCountDownTimer.start();
//                                                        }
//
//                                                        @Override
//                                                        public void onStartTrackingTouch(SeekBar seekBar) {
//                                                        }
//
//                                                        @Override
//                                                        public void onStopTrackingTouch(SeekBar seekBar) {
//                                                            /**
//                                                             *  倒计时开始
//                                                             */
////                                                            myCountDownTimer.start();
//                                                        }
//                                                    });
//                                                    //注册音量发生变化时接收的广播
//                                                    myRegisterReceiver();
//                                                }
//                                            }).show();
//
//
//                                } else {
//                                    if (EasyFloat.getFloatView("tag_R") != null) {
//                                        EasyFloat.hide("tag_R");
//                                    }
//                                    boolean tag_v = EasyFloat.isShow("tag_V");
//                                    if (tag_v) {
////                                        myCountDownTimer.start();
//                                        if (imgV.getAnimation() != null) {
//                                            imgV.getAnimation().cancel();
//                                        }
//                                        if (EasyFloat.getFloatView("tag_V") != null) {
//                                            EasyFloat.hide("tag_V");
//                                        }
//                                        if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                            EasyFloat.dragEnable(true, "tag_showFlow");
//                                        }
//                                    } else {
////                                        myCountDownTimer.cancel();
//                                        if (imgR.getAnimation() != null) {
//                                            imgR.getAnimation().cancel();
//                                        }
//                                        dongHuaScaleAnimation(imgV);
//                                        if (EasyFloat.getFloatView("tag_V") != null) {
//                                            EasyFloat.updateFloat("tag_V",
//                                                    ScreenUtils.getScreenWidth(SecondAppActivity.this) - UIUtils.dip2px(180),
//                                                    location[1] + UIUtils.dip2px(105)// view距离 屏幕顶边的距离（即y轴方向
//                                            );
//                                        }
//
//                                        if (EasyFloat.getFloatView("tag_V") != null) {
//                                            EasyFloat.show("tag_V");
//                                        }
//                                        if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                                            EasyFloat.dragEnable(false, "tag_showFlow");
//                                        }
//                                    }
//
//                                }
//
//
//                            });
//
////                           RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
////                                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
////                            rotateAnimation.setDuration(3000);
////                            rotateAnimation.setFillAfter(true);
////                            rotateAnimation.setRepeatMode(Animation.RESTART);
////                            //让旋转动画一直转，不停顿的重点
////                            rotateAnimation.setInterpolator(new LinearInterpolator());
////                            rotateAnimation.setRepeatCount(-1);
////                            room_rl_header.startAnimation(rotateAnimation);
////                            cancle.startAnimation(rotateAnimation);
//
//                        }
//                    }).show();
//
//
//            myCountDownTimer = new CountDownTimer(10000, 1000) {
//                @Override
//                public void onTick(long millisUntilFinished) {
//
//                }
//
//                @Override
//                public void onFinish() {
//                    room_rl_header.setVisibility(VISIBLE);
//                    room_rl_header.setBackgroundResource(R.mipmap.llm_setting_icom_tm);
//                    ll_suo.setVisibility(View.GONE);
//
//                    if (EasyFloat.getFloatView("tag_R") != null) {
//                        EasyFloat.hide("tag_R");
//                    }
//                    if (EasyFloat.getFloatView("tag_V") != null) {
//                        EasyFloat.hide("tag_V");
//                    }
//                    if (EasyFloat.getFloatView("tag_showFlow") != null) {
//                        EasyFloat.dragEnable(true, "tag_showFlow");
//                    }
//                    if (imgV.getAnimation() != null) {
//                        imgV.getAnimation().cancel();
//                    }
//                    if (imgR.getAnimation() != null) {
//                        imgR.getAnimation().cancel();
//                    }
//
//                }
//
//            };
//            /**
//             * 开始倒计时
//             */
//            myCountDownTimer.start();
//
//        }
//    }
//
//}
