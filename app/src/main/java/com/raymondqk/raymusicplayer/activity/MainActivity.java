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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.raymondqk.raymusicplayer.MusicMode;
import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.customview.AvatarCircle;
import com.raymondqk.raymusicplayer.service.MusicService;
import com.raymondqk.raymusicplayer.utils.ConstantUtils;
import com.raymondqk.raymusicplayer.utils.MusicUtils;

/**
 * Created by 陈其康 raymondchan on 2016/8/3 0003.
 * 当前进度：完成主界面布局
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AvatarCircle mAvatarCircle;
    private boolean isClickAvatar;
    private ImageButton mIb_play_mode;
    private ImageButton mIb_play;
    private ImageButton mIb_preview;
    private ImageButton mIb_next;
    private TextView mTv_duration;

    private Intent mMusicSeviceIntent;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /* 单进程下Service与Activity通信 */
            /*MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            mMusicService = binder.getServiceInstance();
            if (mMusicService != null) {
                Toast.makeText(MainActivity.this, "音乐服务绑定成功", Toast.LENGTH_SHORT).show();
                mMusicService.setCompletionCallback(mOnCompletionCallback);
                mMusicService.setPlayCallback(mPlayPreparedCallback);
            } else {
                Toast.makeText(MainActivity.this, "音乐服务绑定失败", Toast.LENGTH_SHORT).show();
            }*/

            mServiceMsger = new Messenger(service);
            if (mServiceMsger != null) {
                Toast.makeText(MainActivity.this, "远程服务连接成功", Toast.LENGTH_SHORT).show();
                //向Service进程注册Activity的Messenger，用户互发Msg
                Message msg = Message.obtain();
                msg.replyTo = mMessenger;
                msg.what = ConstantUtils.WHAT_REGIST_MSGER_MAINACTIVITY;
                try {
                    mServiceMsger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(MainActivity.this, "远程服务连接失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //            mMusicService = null;
            mServiceMsger = null;

        }
    };

    private Handler mHandler = new Handler();
    private TextView mTv_position;
    private SeekBar mProgress;
    private TextView mTv_title;
    private TextView mTv_artist;
    private float mPercent;

    private Messenger mServiceMsger;
    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantUtils.WHAT_REGIST_MSGER_MAINACTIVITY:
                    Log.i(ConstantUtils.TAG_SERVICE, "Messenger信使相互注册成功，可以开始互发消息");
                    break;
                case ConstantUtils.WHAT_PLAY_PLAY_BTN:

                    break;
                case ConstantUtils.WHAT_PLAY_NEXT_BTN:

                    break;
                case ConstantUtils.WHAT_PLAY_PRE_BTN:

                    break;
                case ConstantUtils.WHAT_PLAYED:
                    setPlaying(msg);
                case ConstantUtils.WHAT_PLAY_DURATION:
                    int position = msg.arg1;
                    int duration = msg.arg2;
                    mTv_position.setText(MusicUtils.getTimeStrByMils(position));
                    // TODO: 2016/9/4 0004 !!!!!
                    if (duration==0){
                        break;
                    }
                    mProgress.setProgress(position * 100 / duration);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    });


    private void setPlaying(Message msg) {
        Bundle data = msg.getData();
        data.setClassLoader(MusicMode.class.getClassLoader());
        boolean isPlaying = data.getBoolean(ConstantUtils.IS_PLAYING);
        MusicMode musicMode = data.getParcelable(ConstantUtils.CURRENT_MUSIC);
        if (isPlaying) {
            mIb_play.setImageResource(R.drawable.pause);
        } else {
            mIb_play.setImageResource(R.drawable.play);
        }
        if (musicMode != null) {
            mTv_title.setText(musicMode.getTitle());
            mTv_artist.setText(musicMode.getArtist());
            mTv_duration.setText(musicMode.getDuration());
            if (musicMode.getAvatar() != null) {
                mAvatarCircle.setImageBitmap(musicMode.getAvatar());
            }

        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        serviceInit();
    }

    private void initView() {
        getSupportActionBar().setSubtitle("播放界面");
        getSupportActionBar().setIcon(R.drawable.preview_selector);
        mAvatarCircle = (AvatarCircle) findViewById(R.id.avatar_main);
        mAvatarCircle.setOnClickListener(this);
        mIb_play_mode = (ImageButton) findViewById(R.id.ib_play_mode);
        mIb_play = (ImageButton) findViewById(R.id.ib_play);
        mIb_preview = (ImageButton) findViewById(R.id.ib_preview);
        mIb_next = (ImageButton) findViewById(R.id.ib_next);

        mIb_next.setOnClickListener(this);
        mIb_play.setOnClickListener(this);
        mIb_play_mode.setOnClickListener(this);
        mIb_preview.setOnClickListener(this);

        mTv_duration = (TextView) findViewById(R.id.tv_main_time);
        mTv_duration.setText("00:00");
        mTv_position = (TextView) findViewById(R.id.tv_pass_time);
        mTv_position.setText("00:00");

        mTv_title = (TextView) findViewById(R.id.tv_main_title);
        mTv_artist = (TextView) findViewById(R.id.tv_main_artist);

        mProgress = (SeekBar) findViewById(R.id.progressbar);
        mProgress.setProgress(0);
        mProgress.setMax(100);
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) { //必须这个判断，是否为用户拉动导致的进度变更，否则会造成播放卡顿现象
                    mPercent = (float) progress * 100 / (float) mProgress.getMax();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //当用户释放SeekBar之后，在通知service更新进度，解决拖动过程中卡顿问题
                Message msg = Message.obtain();
                msg.what = ConstantUtils.WHAT_CHANGE_PROGRESS;
                msg.arg1 = (int) mPercent;
                try {
                    mServiceMsger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        stopService(mMusicSeviceIntent);
        Log.i("Test", "MainActivity onDestroy");
    }

    private void serviceInit() {
        mMusicSeviceIntent = new Intent();
        mMusicSeviceIntent.setClass(MainActivity.this, MusicService.class);
        bindService(mMusicSeviceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_list:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MusicListActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_main:

                break;
            case R.id.ib_play_mode:
                break;
            case R.id.ib_play:
                MusicUtils.playCurrent(mServiceMsger);
                break;
            case R.id.ib_next:
                MusicUtils.playNext(mServiceMsger);
                break;
            case R.id.ib_preview:
                MusicUtils.playPreview(mServiceMsger);
                break;
        }
    }


}


