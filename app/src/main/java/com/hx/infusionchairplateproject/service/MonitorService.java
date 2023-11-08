package com.hx.infusionchairplateproject.service;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hx.infusionchairplateproject.R;
import com.hx.infusionchairplateproject.tools.GeneralUtil;
import com.hx.infusionchairplateproject.tools.SPTool;
import com.hx.infusionchairplateproject.ui.LockScreenActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 本地前台服务检测
 * 本地与服务器的双重判断
 */
public class MonitorService extends Service {

    private boolean isInit;
    private final int FOREGROUND_ID = 11;

    // 锁屏时间计时器
    private final Timer unlockTimetimer = new Timer();
    // USB C340 时间计时器
    private final Timer USBTimeTimer = new Timer();

    private final TimerTask unlockTimerTask = new TimerTask() {

        @Override
        public void run() {
            long unlockTime = SPTool.getLong("unlockTime");
            if (System.currentTimeMillis() < unlockTime) {
                return;
            }

            // 排除锁屏界面
            if (GeneralUtil.Companion.isActivityTop(getApplicationContext(), LockScreenActivity.class)) {
                return;
            }
            // TODO 排除WIFI界面
//            if (GeneralUtil.Companion.isActivityTop(getApplicationContext(),WIFI 界面)) {
//                return;
//            }

            // 锁屏
            Intent intent = new Intent(MonitorService.this, LockScreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!isInit) {
            isInit = true;
            real_startForegroundService();
            unlockTimetimer.schedule(unlockTimerTask, 0, 2000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mManager.cancel(FOREGROUND_ID);
    }

    @SuppressLint("NewApi")
    private void real_startForegroundService() {
        NotificationChannel channel = new NotificationChannel("monitorService_service",
                "communication_service", NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "service");
        Notification notification = builder.setOngoing(true)
                .setSmallIcon(R.drawable.llm_monitor_service_icon)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(FOREGROUND_ID, notification);
    }
}
