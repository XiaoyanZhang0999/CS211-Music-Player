package com.ldw.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.ldw.music.activity.MainContentActivity;

/**
 * Created by CharlesLui on 27/11/2017.
 */

public class MyLinearlayout extends LinearLayout {

    private MainContentActivity mMainContentActivity;

    public MyLinearlayout(Context context) {
        super(context);
    }

    public MyLinearlayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearlayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (mMainContentActivity != null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                mMainContentActivity.setScrool(false);

            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                mMainContentActivity.setScrool(true);
                mMainContentActivity.setScrool(true);
                mMainContentActivity.setScrool(true);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    public void setMainContentActivity(MainContentActivity mainContentActivity) {
        mMainContentActivity = mainContentActivity;
    }
}
