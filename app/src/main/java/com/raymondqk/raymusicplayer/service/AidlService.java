package com.raymondqk.raymusicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.raymondqk.raymusicplayer.aidl.IMyAidlInterface;

/**
 * Created by 陈其康 raymondchan on 2016/8/30 0030.
 */
public class AidlService extends Service {

    IMyAidlInterface.Stub mStub = new IMyAidlInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public int doSomething(int a, int b) throws RemoteException {
            return a + b;
        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }
}
