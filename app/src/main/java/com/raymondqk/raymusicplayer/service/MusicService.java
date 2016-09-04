package com.raymondqk.raymusicplayer.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.raymondqk.raymusicplayer.MusicMode;
import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.activity.MainActivity;
import com.raymondqk.raymusicplayer.utils.ConstantUtils;
import com.raymondqk.raymusicplayer.utils.MusicUtils;
import com.raymondqk.raymusicplayer.widget.MusicWidgetProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 陈其康 raymondchan on 2016/8/4 0004.
 */
public class MusicService extends Service {

    /**
     * 循环模式标志
     */
    public static final int MODE_LOOP_ALL = 0;
    public static final int MODE_LOOP_ONE = 1;
    public static final int MODE_RADOM = 2;

    /**
     * 播放状态标志
     */
    public static final int STATE_PLAYING = 0;
    //    public static final int STATE_PAUSE = 1;
    public static final int STATE_STOP = 2;
    public static final int WHAT_START_PROGRESS = 99;

    private MusicServiceReceiver mMusicServiceReceiver;

    //是否点了喜欢
    private Notification mNotification;
    private Messenger mMsgerMain;
    private Notification.Builder mBuilder;
    private Timer mTimer;
    private Messenger mMsgerListActivity;


    //循环模式变量
    private int play_mode = MODE_LOOP_ALL;
    //播放状态变量
    private int play_state = STATE_STOP;

    //媒体播放类
    private MediaPlayer mMediaPlayer;

    //记录当前播放歌曲索引
    private int currentIndex;

    private List<MusicMode> mMusicList;

    private Messenger mServiceMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case ConstantUtils.WHAT_REGIST_MSGER_MAINACTIVITY:
                    mMsgerMain = msg.replyTo;
                    Message message = Message.obtain();
                    if (mMsgerMain != null) {
                        message.what = ConstantUtils.WHAT_REGIST_MSGER_MAINACTIVITY;
                        try {
                            mMsgerMain.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    initMusicService();
                    break;
                case ConstantUtils.WHAT_REGIST_MSGER_LIST_ACTIVITY:
                    mMsgerListActivity = msg.replyTo;
                    if (mMsgerListActivity != null) {
                        Message msg1 = Message.obtain();
                        Bundle data = new Bundle();
                        data.putParcelableArrayList(ConstantUtils.CURRENT_MUSIC_LIST, (ArrayList<? extends Parcelable>) mMusicList);
                        boolean isPlaying = mMediaPlayer.isPlaying();
                        data.putBoolean(ConstantUtils.IS_PLAYING, isPlaying);
                        msg1.setData(data);
                        msg1.what = ConstantUtils.WHAT_REGIST_MSGER_LIST_ACTIVITY;
                        try {
                            mMsgerListActivity.send(msg1);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case ConstantUtils.WHAT_PLAY_PLAY_BTN:
                    setPlay_state();
                    break;
                case ConstantUtils.WHAT_PLAY_NEXT_BTN:
                    nextMusic();
                    break;
                case ConstantUtils.WHAT_PLAY_PRE_BTN:
                    previewMusic();
                    break;
                case ConstantUtils.WHAT_CHANGE_PROGRESS:
                    int percent = msg.arg1;
                    mMediaPlayer.seekTo(percent * mMediaPlayer.getDuration() / 100);
                    break;
                case ConstantUtils.WHAT_MUSIC_SELECTED:
                    int position = msg.arg1;
                    playOrderMusic(position);
                    break;
                case ConstantUtils.WHAT_UN_REGIST_MSGER_LIST_ACTIVITY:
                    mMsgerListActivity = null;
                    break;
                case WHAT_START_PROGRESS:
                    updateDuration(mMediaPlayer.isPlaying());
                    break;
                case ConstantUtils.WHAT_PLAYED:
                    boolean isPlaying = mMediaPlayer.isPlaying();
                    Message message_play_pause = Message.obtain();
                    Bundle data = new Bundle();
                    data.putBoolean(ConstantUtils.IS_PLAYING, isPlaying);
                    message_play_pause.what = ConstantUtils.WHAT_PLAYED;
                    message_play_pause.setData(data);

                    try {
                        if (mMsgerListActivity != null) {
                            mMsgerListActivity.send(message_play_pause);
                        }
                        if (mMsgerMain != null) {
                            mMsgerMain.send(message_play_pause);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }


    });
    private boolean isFirstPlay = true;
    private boolean isProgressing = false;

    private void onMusicPlayed() {
        Message message = Message.obtain();
        Bundle data = new Bundle();
        Boolean isPlaying = mMediaPlayer.isPlaying();
        message.what = ConstantUtils.WHAT_PLAYED;
        data.putBoolean(ConstantUtils.IS_PLAYING, isPlaying);
        MusicMode musicMode = mMusicList.get(currentIndex % mMusicList.size());
        data.putParcelable(ConstantUtils.CURRENT_MUSIC, musicMode);
        message.setData(data);
        try {
            if (mMsgerMain != null) {
                mMsgerMain.send(message);
            }
            if (mMsgerListActivity != null) {
                mMsgerListActivity.send(message);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateNotification() {
        mBuilder.setContentTitle(mMusicList.get(currentIndex % mMusicList.size()).getTitle());
        mBuilder.setContentText(mMusicList.get(currentIndex % mMusicList.size()).getArtist());
        Bitmap bitmap = mMusicList.get(currentIndex % mMusicList.size()).getAvatar();
        mBuilder.setLargeIcon(bitmap);
        String position = MusicUtils.getTimeStrByMils(mMediaPlayer.getCurrentPosition());
        String duration = MusicUtils.getTimeStrByMils(mMediaPlayer.getDuration());
        mBuilder.setSubText(position + "/" + duration);
        mNotification = mBuilder.build();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, mNotification);//参数1：样式
    }

    private void updateDuration(boolean isPlaying) {
        if (isPlaying) {

            mTimer = new Timer();

            TimerTask timerTask_duration = new TimerTask() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = ConstantUtils.WHAT_PLAY_DURATION;
                    msg.arg1 = mMediaPlayer.getCurrentPosition();
                    msg.arg2 = mMediaPlayer.getDuration();
                    try {

                        if (mMsgerMain != null) {
                            mMsgerMain.send(msg);
                        }
                        if (mMsgerListActivity != null) {
                            mMsgerListActivity.send(msg);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    updateNotification();
                }
            };
            mTimer.schedule(timerTask_duration, 1000, 1000);
        } else {
            mTimer.cancel();
        }
    }

    private void playOrderMusic(int position) {
        currentIndex = position;
        playMusic();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceMessenger.getBinder();
    }

    /**
     * 在服务解绑或stop时调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //记得释放资源
        stopForeground(true);
        mMediaPlayer.stop();
        mMediaPlayer.release();
        unregisterReceiver(mMusicServiceReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Test", "onStartCommand");
        return START_NOT_STICKY;
    }

    /**
     * Service被绑定或启动时执行，进行相关初始化工作
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void initMusicService() {
        //初始化音乐文件列表
        new Thread(new Runnable() {
            @Override
            public void run() {
                initMusicFiles();
                //注册BroadcastReceiver，这是用来接收来自Widget的广播的
                registBroadcastReceiverForWidget();
                simpleNotification();

            }
        }).start();
    }

    /**
     * 开启 前台服务
     */
    private void simpleNotification() {
        mBuilder = new Notification.Builder(this);
        //状态栏提示
        mBuilder.setTicker("RaymondCQK 正在运行");
        //下拉通知栏标题
        mBuilder.setContentTitle("暂无歌曲");
        //下拉通知正文
        mBuilder.setContentText("...");
        //内容摘要（低版本不一定显示）
        mBuilder.setSubText("...");
        //状态栏小图标
        mBuilder.setSmallIcon(R.mipmap.blueball_72px);
        //下拉通知栏图标
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.blueball_72px);
        mBuilder.setLargeIcon(icon);
        //跳转intent
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);//参数2：requestCode 参数4：flag
        mBuilder.setContentIntent(pendingIntent);

        //开启Notification
        mNotification = mBuilder.build();
        //        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //        manager.notify(1,notification);//参数1：样式
        startForeground(1, mNotification);
    }

    /**
     * 注册BroadcastReceiver，这是用来接收来自Widget的广播的
     */
    private void registBroadcastReceiverForWidget() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicWidgetProvider.WIDGET_PLAY);
        intentFilter.addAction(MusicWidgetProvider.WIDGET_NEXT);
        intentFilter.addAction(MusicWidgetProvider.WIDGET_PREVIEW);
        mMusicServiceReceiver = new MusicServiceReceiver();
        registerReceiver(mMusicServiceReceiver, intentFilter);
        Log.i("Test", "registReceiver");
    }

    /**
     * 初始化MediaPlayer
     */
    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        //设置播放结束的监听事件
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //判断当前播放模式是否为单曲循环
                if (play_mode != MODE_LOOP_ONE) {
                    //如果非单曲循环，则进行播放下一首的操作
                    try {
                        Message msg = Message.obtain();
                        msg.what = ConstantUtils.WHAT_PLAY_NEXT_BTN;
                        mServiceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    //若为单曲循环，则直接开始播放，不需要重新setDataSource
                    mMediaPlayer.start();
                }
            }
        });

    }

    /**
     * 准备音乐文件
     * 通过ContentProvider读取媒体库音乐数据
     */
    private void initMusicFiles() {
        scanMusicWithContentProvider();
        //        mMusicList = scanMusicWithRaw();

    }

    private List<MusicMode> scanMusicWithRaw() {
        mMusicList = new ArrayList<>();
        //android 获取raw 绝对路径 -- raw资源转uri
        for (int i = 0; i < 10; i++) {     // 通过循环重复加载uri到list里面，模拟有多首歌曲的情况
            //将raw资源转化为uri
            Uri uri = Uri.parse("android.resource://com.raymondqk.raymusicplayer/" + R.raw.missyou);
            //加入到musicList
            //把头像资源加入到头像的list里面 与music同步加入，根据index就可以将music和头像对应起来，这是目前的暂缓之策
            // 日后应当根据music的title找到对应的头像图片
            // TODO: 2016/8/4 0004 因为MediaPlayer似乎无法读取文件里面的歌曲信息，如标题和艺术家，所以目前这样处理着
            MusicMode musicMode = new MusicMode();
            musicMode.setArtist("Joyce");
            musicMode.setTitle("好想你");
            musicMode.setAvatar(BitmapFactory.
                    decodeResource(getResources(), R.mipmap.embarrassed_128px));
            musicMode.setUri(uri);
            mMusicList.add(musicMode);
            //这是第二首
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stillalive);
            musicMode = new MusicMode();
            musicMode.setArtist("Big Bang");
            musicMode.setTitle("Still Alive");
            musicMode.setAvatar(BitmapFactory.
                    decodeResource(getResources(), R.drawable.avatar_g));
            musicMode.setUri(uri);
            mMusicList.add(musicMode);
        }
        for (MusicMode m : mMusicList) {
            Log.i("scanmusic", "扫描音乐库 完毕: " + m.toString());
        }
        return mMusicList;
    }

    private List<MusicMode> scanMusicWithContentProvider() {
        Log.i("scanmusic", "正在扫描音乐库");
        mMusicList = new ArrayList<>();
        Cursor cursor = getContentResolver().
                query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null)
            return null;
        if (cursor.moveToFirst()) {
            do {
                MusicMode mode = new MusicMode();
                mode.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                mode.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                String duration = MusicUtils.getTimeStrByMils((int) cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                mode.setDuration(duration);
                mode.setUri(Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))));
                // TODO: 2016/9/4 0004 读取专辑图
                mode.setAvatar(BitmapFactory.decodeResource(getResources(), R.mipmap.musicplayer_512px));
                mMusicList.add(mode);
                Log.i("scanmusic", mode.getTitle());
            } while (cursor.moveToNext());
            cursor.close();
        }
        Log.i("scanmusic", "扫描音乐库 完毕");
        for (MusicMode m : mMusicList) {
            Log.i("scanmusic", "扫描音乐库 完毕: " + m.toString());
        }
        return mMusicList;
    }

    /**
     * 设置当前播放模式：单曲、循环
     *
     * @param play_mode 循环模式 : 可选 MODE_LOOP_ALL/MODE_LOOP_ONE
     */
    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    /**
     * 设置当前播放状态：播放中、暂停,同时进行播放操作，是播放还是暂停，暂时未添加停止项
     * 外部只需要通知播放暂停按键被按下了即可，剩下操作留给service来判断
     */
    public int setPlay_state() {

        /*
        当前是播放状态，则进行暂停操作
         */
        if (isFirstPlay) {
            initMediaPlayer();
            playMusic();
            isFirstPlay = false;
        } else {
            if (play_state == STATE_PLAYING) {
                play_state = STATE_STOP;
                mMediaPlayer.pause();
            } else if (play_state == STATE_STOP) {
                play_state = STATE_PLAYING;
                mMediaPlayer.start();
            }
            Message msg = Message.obtain();
            msg.what = ConstantUtils.WHAT_PLAYED;
            Bundle data = new Bundle();
            data.putBoolean(ConstantUtils.IS_PLAYING, mMediaPlayer.isPlaying());
            try {

                mServiceMessenger.send(msg);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return play_state;
    }


    /**
     * 获取当前循环模式
     *
     * @return
     */
    public int getPlay_mode() {
        return play_mode;
    }

    /**
     * 获取当前播放器状态：暂停、播放
     *
     * @return
     */
    public int getPlay_state() {
        return play_state;
    }


    /**
     * 播放音乐
     */
    public void playMusic() {
        Log.i("Test", "play");
        //重置MediaPlayer，确保能顺利载入datasource以及播放，这里的策略是每次要播放新歌曲时，即第一次播放，切歌时，都进行一次reset
        mMediaPlayer.reset();
        try {//prepare()会抛出异常
            //设置数据源
            mMediaPlayer.setDataSource(mMusicList.get(currentIndex % mMusicList.size()).getUri().toString());
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int duration_raw = mMediaPlayer.getDuration();
                    String duration = getCurrent_duration(duration_raw);
                    mMusicList.get(currentIndex % mMusicList.size()).setDuration(duration);
                    //播放音乐
                    mMediaPlayer.start();
                    updateNotification();
                    onMusicPlayed();

                    Message msg = Message.obtain();
                    msg.what = WHAT_START_PROGRESS;
                    try {
                        mServiceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                }
            });
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Test", "无法播放音乐");
            mMediaPlayer.reset();
        }


    }


    /**
     * 上一首
     */
    public void nextMusic() {

        //歌曲索引加一，指向下一首歌曲
        currentIndex++;
        //设置完索引，就让播放函数载入当前索引对应的音乐文件
        playMusic();


    }

    /**
     * 下一首
     */
    public void previewMusic() {
        if (currentIndex > 0) {
            //歌曲索引减一，指向上一首歌曲
            currentIndex--;
        } else {
            //上面的代码会导致索引出现负值，作如下处理
            //当前索引为0，即第一首，负值为队尾的索引值，即最后一首
            currentIndex = mMusicList.size() - 1;
        }
        //设置完索引，就让播放函数载入当前索引对应的音乐文件
        playMusic();
    }

    /**
     * 暂停当前播放
     */
    public void stopMediaPlayer() {
        mMediaPlayer.pause();
    }

    /**
     * 继续当前播放
     */
    public void continueMediaPlayer() {
        mMediaPlayer.start();
    }

    /**
     * 获得当前音乐长度的字符串 格式 02:25
     *
     * @return
     */
    public String getCurrent_duration(int duration_raw) {
        return MusicUtils.getTimeStrByMils(duration_raw);

    }


    /**
     * 获得当前播放进度的字符串 格式 02:25
     *
     * @return
     */
    public String getCurrent_pisition() {
        return MusicUtils.getTimeStrByMils(mMediaPlayer.getCurrentPosition());
    }


    /**
     * 获得当前进度的百分比
     *
     * @return
     */
    public float getProgressPercent() {

        return (float) mMediaPlayer.getCurrentPosition() / (float) mMediaPlayer.getDuration();
    }

    /**
     * 创建一个Binder类，用于绑定服务时传给Activity
     */
    public class MusicServiceBinder extends Binder {
        //返回当前服务的实例引用
        public MusicService getServiceInstance() {
            return MusicService.this;
        }
    }

    /**
     * 用与接收来自Widget的Broadcast
     */
    class MusicServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                if (TextUtils.equals(intent.getAction(), MusicWidgetProvider.WIDGET_PLAY)) {
                    setPlay_state();
                    Log.i("TEST", "service-onReceive-PLAY");
                } else if (TextUtils.equals(intent.getAction(), MusicWidgetProvider.WIDGET_NEXT)) {
                    nextMusic();
                    Log.i("TEST", "service-onReceive-next");
                } else if (TextUtils.equals(intent.getAction(), MusicWidgetProvider.WIDGET_PREVIEW)) {
                    previewMusic();
                    Log.i("TEST", "service-onReceive-PRE");
                }
            }

        }
    }
}

