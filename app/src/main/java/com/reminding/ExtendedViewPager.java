package com.reminding;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

//핀치 투줌 과 충돌 막기 위한 커스텀 뷰페이저
public class ExtendedViewPager extends ViewPager {

    public ExtendedViewPager(Context context) { super(context); }

    public ExtendedViewPager(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try { return super.onTouchEvent(ev); }
        catch (IllegalArgumentException ex) { ex.printStackTrace(); }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try { return super.onInterceptTouchEvent(ev); }
        catch (IllegalArgumentException ex) { ex.printStackTrace(); }
        return false;
    }

}
