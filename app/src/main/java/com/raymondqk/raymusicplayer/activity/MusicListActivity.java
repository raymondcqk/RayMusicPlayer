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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.raymondqk.raymusicplayer.MusicMode;
import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.adapter.MusicListAdapter;
import com.raymondqk.raymusicplayer.service.MusicService;
import com.raymondqk.raymusicplayer.utils.ConstantUtils;
import com.raymondqk.raymusicplayer.utils.MusicUtils;

import java.util.List;


/**
 * Created by 陈其康 raymondchan on 2016/8/3 0003.
 */
public class MusicListActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView mListView;
    private MusicListAdapter mAdapter;

    private ImageButton mIb_play;
    private ImageButton mIb_next;
    private ImageButton mIb_preview;
    private ImageButton mIb_play_mode;
    private ImageButton mIb_favor;
    private SeekBar mProgressBar;
    private List<MusicMode> mMusicList;

    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantUtils.WHAT_REGIST_MSGER_LIST_ACTIVITY:
                    Bundle data = msg.getData();
                    data.setClassLoader(MusicMode.class.getClassLoader());
                    mMusicList = data.getParcelableArrayList(ConstantUtils.CURRENT_MUSIC_LIST);
                    //                    mMusicList = (List<MusicMode>) data.get(ConstantUtils.CURRENT_MUSIC_LIST);
                    if (mMusicList != null) {
                        mAdapter = new MusicListAdapter(MusicListActivity.this, mMusicList);
                        mListView.setAdapter(mAdapter);
                    }
                    boolean isPlaying = data.getBoolean(ConstantUtils.IS_PLAYING);
                    if (isPlaying) {
                        mIb_play.setImageResource(R.drawable.pause);
                    } else {
                        mIb_play.setImageResource(R.drawable.play);
                    }
                    break;
                case ConstantUtils.WHAT_PLAY_PLAY_BTN:

                    break;
                case ConstantUtils.WHAT_PLAY_NEXT_BTN:

                    break;
                case ConstantUtils.WHAT_PLAY_PRE_BTN:

                    break;
                case ConstantUtils.WHAT_PLAY_DURATION:
                    int position = msg.arg1;
                    int duration = msg.arg2;
                    mProgressBar.setProgress(position * 100 / duration);
                    break;
                case ConstantUtils.WHAT_PLAYED:
                    setPlaying(msg);
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

        private void setPlaying(Message msg) {
            Bundle data = msg.getData();
            //就算没用到对应的对象，也要设置class loader，否则会出错。
            data.setClassLoader(MusicMode.class.getClassLoader());
            boolean isPlaying = data.getBoolean(ConstantUtils.IS_PLAYING);
            MusicMode musicMode = data.getParcelable(ConstantUtils.CURRENT_MUSIC);
            if (isPlaying) {
                mIb_play.setImageResource(R.drawable.pause);
            } else {
                mIb_play.setImageResource(R.drawable.play);
            }

        }

    });
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            if (mService != null) {
                //                Toast.makeText(MusicListActivity.this, "服务连接成功", Toast.LENGTH_SHORT).show();
                Message msg = Message.obtain();
                msg.replyTo = mMessenger;
                msg.what = ConstantUtils.WHAT_REGIST_MSGER_LIST_ACTIVITY;
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    private Messenger mService;
    private float mPercent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_musiclist);
        initView();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        Message msg = Message.obtain();
        msg.what = ConstantUtils.WHAT_UN_REGIST_MSGER_LIST_ACTIVITY;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onDestroy();


    }

    private void initView() {
        //去掉默认导航栏

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_musiclist);
        //设置导航图标 左上角
        toolbar.setNavigationIcon(R.drawable.nav_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();


            }
        });

        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.menu_list);
        toolbar.inflateMenu(R.menu.list_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_search:
                        Toast.makeText(MusicListActivity.this, "search", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menu_item_setting:
                        Toast.makeText(MusicListActivity.this, "setting", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        mListView = (ListView) findViewById(R.id.lv_music);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMusicList.get(position);
                Message msg = Message.obtain();
                msg.what = ConstantUtils.WHAT_MUSIC_SELECTED;
                msg.arg1 = position;
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        mIb_play = (ImageButton) findViewById(R.id.ib_play);
        mIb_next = (ImageButton) findViewById(R.id.ib_next);
        mIb_preview = (ImageButton) findViewById(R.id.ib_preview);
        mIb_play_mode = (ImageButton) findViewById(R.id.ib_play_mode);
        mIb_favor = (ImageButton) findViewById(R.id.ib_favor);

        mIb_favor.setOnClickListener(this);
        mIb_next.setOnClickListener(this);
        mIb_preview.setOnClickListener(this);
        mIb_play.setOnClickListener(this);
        mIb_play_mode.setOnClickListener(this);

        mProgressBar = (SeekBar) findViewById(R.id.progressbar);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);
        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPercent = (float) progress * 100 / (float) mProgressBar.getMax();
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
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        unbindService(mServiceConnection);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_play_mode:

                break;
            case R.id.ib_play:
                MusicUtils.playCurrent(mService);
                break;
            case R.id.ib_next:
                MusicUtils.playNext(mService);
                break;
            case R.id.ib_preview:
                MusicUtils.playPreview(mService);
                break;
            case R.id.ib_favor:

                break;

        }
    }


}
