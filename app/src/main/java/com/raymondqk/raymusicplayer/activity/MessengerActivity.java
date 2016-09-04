package com.raymondqk.raymusicplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.service.MessengerService;

/**
 * Created by 陈其康 raymondchan on 2016/8/29 0029.
 */
public class MessengerActivity extends AppCompatActivity {

    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessengerService.MSG_WHAT_FROM_SERVICE:
                    Log.i(MessengerService.TAG, "接收到来自Service的Message：" + msg.getData().getString(MessengerService.KEY_FROM_SERVICE));
                    break;
                case MessengerService.MSG_WHAT_TIMER:
                    mTv.setText(msg.arg1+"");
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    });

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            if (mServiceMessenger != null) {
                getSupportActionBar().setTitle("已成功连接Messenger服务");
                Log.i(MessengerService.TAG, "已成功连接Messenger服务");
                Message msg = Message.obtain();
                msg.what = MessengerService.MSG_WHAT_FROM_CLIENT;
                //携带上本Activity的Messenger，Service获取到后，才能发消息给本Activity
                msg.replyTo = mMessenger;
                Bundle data = new Bundle();
                data.putString(MessengerService.KEY_MSG_FROM_CLIENT, "来自MessengerActivity的注册请求");
                msg.setData(data);
                try {
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
        }
    };
    private Messenger mServiceMessenger;
    private TextView mTv;
    private Button mBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        mTv = (TextView) findViewById(R.id.tv_aidl);
        mBtn = (Button) findViewById(R.id.btn_aidl_star_timer);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain();
                msg.what = MessengerService.MSG_WHAT_START_TIMER;
                try {
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        bindService(new Intent(this, MessengerService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
    }
}
