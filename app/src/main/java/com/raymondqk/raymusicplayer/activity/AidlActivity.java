package com.raymondqk.raymusicplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.aidl.IMyAidlInterface;
import com.raymondqk.raymusicplayer.service.AidlService;

/**
 * Created by 陈其康 raymondchan on 2016/8/31 0031.
 */
public class AidlActivity extends AppCompatActivity {

    private TextView mTv_result;
    private EditText mEdt_a;
    private EditText mEdt_b;
    private Button mBtn;

    private IMyAidlInterface mAidlInterface;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获得Service实现的Aidl接口对象
            mAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                //调用接口，执行跨进程方法
                int result =  mAidlInterface.doSomething(2,2);
                getSupportActionBar().setTitle("AIDL接口跨进程调用方法"+result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidl);
        initView();
        bindService(new Intent(this, AidlService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    private void initView() {
        mTv_result = (TextView) findViewById(R.id.tv_aidl);
        mEdt_a = (EditText) findViewById(R.id.edt_aidl_a);
        mEdt_b = (EditText) findViewById(R.id.edt_aidl_b);
        mBtn = (Button) findViewById(R.id.btn_aidl_star_timer);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEdt_a.getText().toString().equals("") && !mEdt_b.getText().toString().equals("")) {

                    try {
                        int result = mAidlInterface.doSomething(Integer.parseInt(mEdt_a.getText().toString()), Integer.parseInt(mEdt_b.getText().toString()));
                        mTv_result.setText(result + "");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
