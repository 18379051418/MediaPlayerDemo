package com.example.mediaplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;

/**
 * @author xiezeqing
 * @date 7/12/2019
 * @email xiezeqing@hikcreate.com
 */
public class HikMediaController extends MediaController {
    private LinearLayout titleView;
    private boolean isShowingTitle;
    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public HikMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HikMediaController(Context context) {
        super(context);
    }

    public void setTitleView(View view){
        if(view instanceof LinearLayout) {
            this.titleView = (LinearLayout) view;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                show(0); // show until hide is called
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void show(int timeout) {
        TestUtil.logd("show");
        super.show(timeout);
        if(!isShowingTitle && titleView != null){
            titleView.setVisibility(VISIBLE);
            isShowingTitle = true;
        }
    }

    @Override
    public void hide() {
        TestUtil.logd("hide");
        super.hide();
        if(isShowingTitle && titleView != null){
            titleView.setVisibility(GONE);
            isShowingTitle = false;
        }
        removeCallbacks(mFadeOut);
    }

    public void defaultShow(){
        showCorrect(1000*10);//10秒
    }

    public void showCorrect(int timeout){
        show(0);
        postDelayed(mFadeOut,timeout);
    }
}
