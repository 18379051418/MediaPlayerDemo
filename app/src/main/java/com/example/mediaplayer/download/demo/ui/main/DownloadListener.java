package com.example.mediaplayer.download.demo.ui.main;

/**
 * @author xiezeqing
 * @date 7/17/2019
 * @email xiezeqing@hikcreate.com
 */
public interface DownloadListener {
    void onStart();
    void onProgress(int p);
    void onFinish(String path);
    void onError(String msg);
}
