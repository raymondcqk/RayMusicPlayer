package com.raymondqk.raymusicplayer.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by 陈其康 raymondchan on 2016/8/18 0018.
 */
public class VerifyPermissionUtils {
    //存储权限 Storage Permission
    //请求码
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    //要申请的Storage权限
    private static String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
    };


    /**
     * 在API23+以上，光是从Manifest中添加权限是不够的，还要在java中申请
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     * 若未取得权限，会弹出提示去获取权限  -- 授权
     *
     * @param activity
     */
    public static void verifyStoragePermission(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //we don't have permission so prompt the user 未获得权限，提示用户
            ActivityCompat.requestPermissions(activity, PERMISSION_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }

    }

    public static void verifyContactPermission(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.READ_CONTACTS
            }, 1);
        }
    }
}
