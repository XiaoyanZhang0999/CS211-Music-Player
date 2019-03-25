package com.ldw.music.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoScrollViewPager extends ViewPager {

    private boolean isScroll = true;

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);   // return true;不行
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // return false;
        // return true;
        //return super.onInterceptTouchEvent(ev);
        if (isScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //return false;
        //return true;
        //super.onTouchEvent(ev); /

        if (isScroll) {
            return super.onTouchEvent(ev);
        } else {
            return true;
        }
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }
}
