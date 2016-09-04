package com.raymondqk.raymusicplayer.utils;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by 陈其康 raymondchan on 2016/9/4 0004.
 */
public class MusicUtils {
    /**
     * 毫秒转 02：25 格式的字符串
     *
     * @param mils
     * @return
     */
    public static String getTimeStrByMils(int mils) {
        int seconds = mils / 1000;
        int min = seconds / 60;
        int sec = seconds % 60;
        String min_str;
        String sec_str;
        if (min < 10) {
            min_str = "0" + min;
        } else {
            min_str = min + "";
        }
        if (sec < 10) {
            sec_str = "0" + sec;
        } else {
            sec_str = sec + "";
        }
        return min_str + ":" + sec_str;
    }

    /**
     * 播放按钮点击
     */
    public static void playCurrent(Messenger service) {
        //        if (mMusicService != null) {
        //            int play_state = mMusicService.setPlay_state();
        //            if (play_state == MusicService.STATE_STOP) {
        //                mIb_play.setImageResource(R.drawable.play);
        //            } else if (play_state == MusicService.STATE_PLAYING) {
        //                mIb_play.setImageResource(R.drawable.pause);
        //            }
        //        }
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //多进程方式
        Message msg = Message.obtain();
        msg.what = ConstantUtils.WHAT_PLAY_PLAY_BTN;
        try {
            service.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放上一首 接口方法
     */
    public static void playPreview(Messenger service) {
        Message msg = Message.obtain();
        msg.what = ConstantUtils.WHAT_PLAY_PRE_BTN;
        try {
            service.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放下一首 接口方法
     */
    public static void playNext(Messenger service) {
        Message msg = Message.obtain();
        msg.what = ConstantUtils.WHAT_PLAY_NEXT_BTN;
        try {
            service.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
