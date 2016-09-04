package com.raymondqk.raymusicplayer.activity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.utils.MyFileUtil;

/**
 * Created by 陈其康 raymondchan on 2016/8/9 0009.
 */
public class TestFileActivity extends AppCompatActivity {

    public static final String FILENAME = "data.txt";

    private TextView mFileDir;
    private TextView mCache_dir;
    private EditText mEditText;
    private TextView mTv_file_content;
    private TextView mExternalDir;
    private TextView mTv_external;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        //        File file = new File()
        mFileDir = (TextView) findViewById(R.id.id_file_internal_dir);
        mCache_dir = (TextView) findViewById(R.id.id_file_internal_cache_dir);
        mExternalDir = (TextView) findViewById(R.id.id_file_external);
        mTv_external = (TextView) findViewById(R.id.tv_file_external_content);

        //测试
        MyFileUtil.writeExternalFile("cba", "ccc.txt");
        MyFileUtil.writeExternalAppFiles(this, "ray", "chan.txt", "hello");
        Log.i(MyFileUtil.TEST,MyFileUtil.readExternalAppFiles(this, "ray", "chan.txt"));

        //内部路径
        mFileDir.setText(getFilesDir().getAbsolutePath());
        mCache_dir.setText(getCacheDir().getAbsolutePath());
        //外部路径
        mExternalDir.setText(Environment.getExternalStorageState() + ":" + Environment.getExternalStorageDirectory().getAbsolutePath());
        mEditText = (EditText) findViewById(R.id.edt_file);
        mTv_file_content = (TextView) findViewById(R.id.tv_file_result);

        if (!TextUtils.equals(MyFileUtil.readInternalFile(this, FILENAME), "")) {
            mTv_file_content.setText(MyFileUtil.readInternalFile(this, FILENAME));
        }


        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                        String data = mEditText.getText().toString();
                        if (TextUtils.equals(data, "clr")) {
                            MyFileUtil.writeInternalFile(TestFileActivity.this, FILENAME, Context.MODE_PRIVATE, "");
                            MyFileUtil.writeExternalFile(FILENAME, "", false);
                        } else {
                            MyFileUtil.writeInternalFile(TestFileActivity.this, FILENAME, Context.MODE_APPEND, data);
                            MyFileUtil.writeExternalFile(FILENAME, data, true);
                        }

                        mTv_file_content.setText(MyFileUtil.readInternalFile(TestFileActivity.this, FILENAME));
                        mTv_external.setText(MyFileUtil.readExternalFile(FILENAME));

                        if (TextUtils.equals(MyFileUtil.readInternalFile(TestFileActivity.this, FILENAME), "")) {
                            //获取Application Name
                            PackageManager manager = getPackageManager();
                            ApplicationInfo info = null;
                            try {
                                info = manager.getApplicationInfo(getPackageName(), 0);

                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            getSupportActionBar().setTitle(manager.getApplicationLabel(info));
                        } else {
                            getSupportActionBar().setTitle(MyFileUtil.readInternalFile(TestFileActivity.this, FILENAME));
                        }

                        mEditText.setText("");
                }
                return true;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyFileUtil.deleteExternalFile("cba", "ccc.txt");
    }


}

