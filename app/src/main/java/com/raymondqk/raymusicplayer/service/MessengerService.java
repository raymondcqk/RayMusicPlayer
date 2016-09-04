package com.raymondqk.raymusicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 陈其康 raymondchan on 2016/8/29 0029.
 */
public class MessengerService extends Service {

    public static final int MSG_WHAT_TIMER = 3;
    public static final int MSG_WHAT_START_TIMER = 11;
    public static final String TAG = "Messenger";
    public static final String KEY_FROM_SERVICE = "fromService";
    private int mCount = 0;

    public static final int MSG_WHAT_FROM_CLIENT = 1;
    public static final String KEY_MSG_FROM_CLIENT = "fromClient";
    public static final int MSG_WHAT_FROM_SERVICE = 2;
    private List<Messenger> mMessengerList = new ArrayList<Messenger>();
    private Messenger mAidlTestActivityMessenger;
    private Messenger mServiceMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_FROM_CLIENT:
                    Log.i(TAG, msg.getData().getString(KEY_MSG_FROM_CLIENT).toString());
                    mMessengerList.add(msg.replyTo);
                    mAidlTestActivityMessenger = msg.replyTo;
                    if (mAidlTestActivityMessenger != null) {
                        Log.i(TAG, "Activity Messenger 通信 注册成功");
                        //返回给发送方
                        final Message message = Message.obtain();
                        message.what = MSG_WHAT_FROM_SERVICE;
                        Bundle data = new Bundle();
                        data.putString(KEY_FROM_SERVICE, "已成功注册Activity的Messenger到Service");
                        message.setData(data);
                        try {
                            //实质：传来的msg的replyTo携带了发送方的Messenger对象。通过该对象，就可以发送消息给来发送方的handler处理。
                            mMessengerList.get(0).send(message);
                            //若希望对每个发送方都发送同一个消息，就可以遍历List
                            //若每个发送方都有特定需求，则可以为每个发送方进行命名变量，单独管理，方便后续逻辑直接发送消息给发送方。
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    }

                    break;
                case MSG_WHAT_START_TIMER:
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            Message message_timer = Message.obtain();
                            message_timer.what = MSG_WHAT_TIMER;
                            message_timer.arg1 = ++mCount;
                            try {
                                mAidlTestActivityMessenger.send(message_timer);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    timer.schedule(timerTask, 1000, 1000);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceMessenger.getBinder();
    }
}
