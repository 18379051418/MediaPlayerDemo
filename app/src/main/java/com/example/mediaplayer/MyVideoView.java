package com.example.mediaplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * @author xiezeqing
 * @date 7/11/2019
 * @email xiezeqing@hikcreate.com
 */
public class MyVideoView extends VideoView {
    private Context context;
    private HikMediaController mHikMediaContorler;
    private OnVideoStartListener mOnVideoStartListener;

    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    public void setMediaController(MediaController controller) {
        if(controller instanceof HikMediaController){
            mHikMediaContorler = (HikMediaController) controller;
        }
        super.setMediaController(controller);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && mHikMediaContorler != null) {
            if (mHikMediaContorler.isShowing()) {
                mHikMediaContorler.hide();
            } else {
                mHikMediaContorler.defaultShow();
            }
        }
        return false;
    }

    @Override
    public void start() {
        super.start();
        if(mOnVideoStartListener != null)
            mOnVideoStartListener.onVideoStart();
    }

    public void setOnVideoStartListener(OnVideoStartListener mOnVideoStartListener) {
        this.mOnVideoStartListener = mOnVideoStartListener;
    }

    public interface OnVideoStartListener {
        void onVideoStart();
    }


}
