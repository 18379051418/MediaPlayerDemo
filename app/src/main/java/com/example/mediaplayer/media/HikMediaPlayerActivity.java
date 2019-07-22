package com.example.mediaplayer.media;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.mediaplayer.R;
import com.example.mediaplayer.TestUtil;
import com.example.mediaplayer.databinding.ActivityHikMediaPlayerBinding;

import java.io.IOException;

/**
 * @author xiezeqing
 * @date 7/12/2019
 * @email xiezeqing@hikcreate.com
 */
public class HikMediaPlayerActivity extends AppCompatActivity implements
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener{
    private HikMediaPlayer mMediaPlayer;
    private String movieUrl;

    private ActivityHikMediaPlayerBinding mBindings;

    private Handler handler = new Handler();

    /**
     * 用于更新发送进度
     */
    private final Runnable mTicker = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(mTicker,200);
            if(mMediaPlayer != null&&mMediaPlayer.isPlaying()) {
                mBindings.seekBar.setProgress(mMediaPlayer.getCurrentPosition());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBindings = DataBindingUtil.setContentView(this,R.layout.activity_hik_media_player);
        mMediaPlayer = new HikMediaPlayer();
        init();

        test();
    }

    private void test(){
        //test
        Button mBeginButton = findViewById(R.id.bt_begin_player);
        mBeginButton.setOnClickListener(v -> {
            Log.d("test", "onClick: test");
            //隐藏预览图
            mBindings.videoImage.setVisibility(View.INVISIBLE);

            //开始播放
            mMediaPlayer.start();
        });
        movieUrl = "android.resource://com.example.mediaplayer/" + R.raw.trailer;
    }

    private void init(){
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setDisplay(mBindings.hsfMedia.getHolder());
        try {
            mMediaPlayer.setDataSource(movieUrl);
        }catch (IOException e){
            TestUtil.loge("视频源错误",e);
            this.finish();
        }
        mBindings.seekBar.setOnSeekBarChangeListener(this);
}


    /**
     * 播放结束时回调
     * {@link MediaPlayer.OnCompletionListener}
     * @param mp mMediaPlayer
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        mBindings.videoImage.setVisibility(View.VISIBLE);
        mBindings.seekBar.setProgress(0);
        //计时器终止
        handler.removeCallbacks(mTicker);
    }

    /**
     * 准备完毕，开始播放
     * {@link MediaPlayer.OnPreparedListener}
     * @param mp mMediaPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mp) {

        //设置总进度
        mBindings.seekBar.setMax(mMediaPlayer.getDuration());

        //定时更新进度
        handler.post(mTicker);
    }

    /**
     * {@link SeekBar.OnSeekBarChangeListener}
     * @param seekBar mSeekBar
     * @param progress 进度条
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    /**
     * {@link SeekBar.OnSeekBarChangeListener}
     * @param seekBar mSeekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //如果在播放中
        if(mMediaPlayer != null && mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.seekTo(seekBar.getProgress());
            else {
                mMediaPlayer.seekTo(seekBar.getProgress());
                mMediaPlayer.start();
            }
        }
    }
}
