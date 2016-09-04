package com.raymondqk.raymusicplayer.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.raymondqk.raymusicplayer.service.MessengerService;

import java.util.List;

/**
 * Created by 陈其康 raymondchan on 2016/8/29 0029.
 */
public class ApplicationTest extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //以下是判断只有主进程初始化时，才进程 全局初始化

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        //获得进程列表
        List<ActivityManager.RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
        //获得当前pid
        int pid = android.os.Process.myPid();
        Log.i(MessengerService.TAG, "PID：" +pid);
        //判断当前pid属于哪个进程
        for (ActivityManager.RunningAppProcessInfo process : processList) {
            if (pid == process.pid){
                //找到了所属进程，判断进程名是否为 主进程名（默认为包名）
                Log.i(MessengerService.TAG, "进程名：" + process.processName);
                if (process.processName.equals(getPackageName())) {
                    Log.i(MessengerService.TAG, "主进程：全局初始化，只执行一次");
                }
            }
        }
    }
}
