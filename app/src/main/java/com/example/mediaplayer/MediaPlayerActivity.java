package com.example.mediaplayer;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MediaPlayerActivity extends AppCompatActivity implements View.OnClickListener,
MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener, MyVideoView.OnVideoStartListener{

    //https://media.w3.org/2010/05/sintel/trailer.mp4
    //"android.resource://com.example.mediaplayer/" + R.raw.trailer;
    //"http://192.168.43.188:8080/download/test2.mp4";
    public static final String TEST_URL ="http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private MyVideoView vv;
    private final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private LinearLayout layoutTitle;
    private ImageView mBackButton;
    private TextView mTitle;
    private ProgressBar mProgressBar;
    private HikMediaController mHikMediaController;

    private final Handler mHandler = new Handler();

    //进度条控制
    private final Runnable mDisplayProgressBar = new Runnable() {
        @Override
        public void run() {
            if(!vv.isPlaying()){
                mProgressBar.setVisibility(View.VISIBLE);
            }else{
                mProgressBar.setVisibility(View.INVISIBLE);
                mHandler.removeCallbacks(this);
            }
            mHandler.postDelayed(this,500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        mBackButton = findViewById(R.id.iv_back);
        vv = findViewById(R.id.video_view);
        mTitle = findViewById(R.id.tv_title_media);
        layoutTitle = findViewById(R.id.ll_title);
        mProgressBar = findViewById(R.id.pb_media);

        init();

        //请求权限
        if(getPackageManager().
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getPackageName()) == PackageManager.PERMISSION_DENIED
            ||getPackageManager().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,getPackageName())
                ==PackageManager.PERMISSION_DENIED){
            requestPermission();
        }else{
            playVideo();
        }
    }

    private void init(){
        mHikMediaController = new HikMediaController(this);
        mHikMediaController.setTitleView(layoutTitle);
        vv.setMediaController(mHikMediaController);
        vv.setOnCompletionListener(this);
        vv.setOnPreparedListener(this);
        vv.setOnVideoStartListener(this);

        mBackButton.setOnClickListener(this);
    }

    private void playVideo() {
        vv.setVideoPath(TEST_URL);
        vv.requestFocus();
        vv.start();

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission(){
        requestPermissions(permissions,0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0){
            for(int i : grantResults){
                if(i == PackageManager.PERMISSION_DENIED)return;
            }
            playVideo();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        vv.seekTo(0);
        mHandler.removeCallbacks(mDisplayProgressBar);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mDisplayProgressBar);
        mHikMediaController.hide();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        TestUtil.logd("left:" + vv.getLeft() + ",right:" + vv.getRight());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onVideoStart() {
        mHandler.post(mDisplayProgressBar);
        mHikMediaController.show(1000*10);
    }
}
