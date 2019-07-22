package com.example.mediaplayer.media;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.mediaplayer.TestUtil;

/**
 * @author xiezeqing
 * @date 7/22/2019
 * @email xiezeqing@hikcreate.com
 */
public class HikSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;

    public HikSurfaceView(Context context) {
        super(context);
        init();
    }

    public HikSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HikSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mSurfaceHolder = getHolder();
    }

    private void draw(){
        try{
            //获取canvas
            mCanvas = mSurfaceHolder.lockCanvas();
            //执行绘制操作
        }catch (Exception e){
            TestUtil.loge("SurfaceView绘制出现问题",e);
        }finally {
            if(mCanvas != null){
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

/*---------------------------------------surface holder call back---------------------------------*/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
/*---------------------------------------runnable-------------------------------------------------*/
    @Override
    public void run() {

    }
}
