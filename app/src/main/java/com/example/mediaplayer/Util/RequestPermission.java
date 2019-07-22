package com.example.mediaplayer.Util;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author xiezeqing
 * @date 7/17/2019
 * @email xiezeqing@hikcreate.com
 */
public class RequestPermission {
    private static final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static boolean checkPermission(AppCompatActivity activity){
        PackageManager packageManager = activity.getPackageManager();
        String packageName = activity.getPackageName();

        for(String permission : permissions){
            if(packageManager.checkPermission(permission,packageName) == PackageManager.PERMISSION_DENIED)
                return false;
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermission(AppCompatActivity activity){
        activity.requestPermissions(permissions,0);
    }
}
