package com.termux.menu.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/** 自定义FrameLayout — dispatchTouchEvent先让波纹绘制，再正常分发给子View */
public class RippleFrameLayout extends FrameLayout {

    private TouchRippleView mRippleView;

    public RippleFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRippleView(TouchRippleView ripple) {
        mRippleView = ripple;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 先让波纹处理（无论如何都会调用）
        if (mRippleView != null) {
            mRippleView.handleTouch(ev);
        }
        // 再正常分发给子View
        return super.dispatchTouchEvent(ev);
    }
}
