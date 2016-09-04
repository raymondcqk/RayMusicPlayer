package com.raymondqk.raymusicplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.utils.MySharedPreference;

/**
 * Created by 陈其康 raymondchan on 2016/8/8 0008.
 */
public class GuideActivity extends Activity {

    private MySharedPreference mPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        mPreference = new MySharedPreference(GuideActivity.this);
        Button btn = (Button) findViewById(R.id.btn_guide);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mPreference.setData(LaunchActivity.IS_FIRST_LAUNCH,false);
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
