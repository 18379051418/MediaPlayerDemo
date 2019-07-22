package com.example.mediaplayer;

import android.util.Log;

/**
 * @author xiezeqing
 * @date 7/12/2019
 * @email xiezeqing@hikcreate.com
 */
public class TestUtil {
    public static void logd(String content){
        Log.d("测试", content);
    }

    public static void loge(String content,Throwable e){
        loge("测试",content,e);
    }

    public static void loge(String title, String content, Throwable e){
        loge(title, content, e);
    }
}
