package com.termux.menu.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/** 触摸水波纹 — FrameLayout底层，不拦截触摸，双环荡漾 */
public class TouchRippleView extends View {

    private static final int MAX = 6;
    private float[] mX = new float[MAX], mY = new float[MAX];
    private long[] mT = new long[MAX];
    private int mIdx = 0;
    private int mColor;
    private Paint mPaint;

    public TouchRippleView(Context context, int color) {
        super(context);
        mColor = color;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setClickable(false);
        setFocusable(false);
    }

    private float[] mLastX = new float[MAX];
    private float[] mLastY = new float[MAX];

    /** 统一触摸入口 — 由父View的OnTouchListener调用 */
    public void handleTouch(MotionEvent e) {
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            int i = e.getActionIndex();
            mLastX[i] = e.getX(i);
            mLastY[i] = e.getY(i);
            spawn(e.getX(i), e.getY(i), 0.6f);
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < e.getPointerCount(); i++) {
                float dx = e.getX(i) - mLastX[i];
                float dy = e.getY(i) - mLastY[i];
                float dist = (float) Math.sqrt(dx*dx + dy*dy);
                if (dist > 2) {
                    int steps = (int)(dist / 4);
                    for (int s = 1; s <= steps; s++) {
                        float f = (float)s / steps;
                        spawn(mLastX[i] + dx * f, mLastY[i] + dy * f, 0.3f);
                    }
                    mLastX[i] = e.getX(i);
                    mLastY[i] = e.getY(i);
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            for (int i = 0; i < e.getPointerCount(); i++) {
                spawn(e.getX(i), e.getY(i), 0.5f);
            }
        }
    }

    private float[] mScale = new float[MAX];

    private void spawn(float x, float y, float scale) {
        mX[mIdx] = x; mY[mIdx] = y;
        mScale[mIdx] = scale;
        mT[mIdx] = System.currentTimeMillis();
        mIdx = (mIdx + 1) % MAX;
        postInvalidate();
    }

    protected void onDraw(Canvas c) {
        long now = System.currentTimeMillis();
        float maxR = Math.max(getWidth(), getHeight()) * 0.5f;
        boolean alive = false;

        for (int i = 0; i < MAX; i++) {
            if (mT[i] == 0) continue;
            float t = (now - mT[i]) / 1000f;
            float s = mScale[i];
            float duration = 0.8f + s * 0.7f;
            if (t > duration) { mT[i] = 0; continue; }
            alive = true;
            float r = maxR * s * (t / duration);

            // 外环 — 淡金扩散
            int a1 = (int)((1f - t/duration) * 120);
            mPaint.setShader(new RadialGradient(mX[i], mY[i], r,
                new int[]{0x00000000,
                    (a1 << 24) | (mColor & 0xFFFFFF),
                    0x00000000},
                new float[]{0.5f, 0.8f, 1f}, Shader.TileMode.CLAMP));
            c.drawCircle(mX[i], mY[i], r, mPaint);

            // 内环 — 亮白闪烁
            float t2 = Math.min(t, duration * 0.3f);
            int a2 = t2 < duration * 0.2f ? (int)(t2/(duration*0.2f) * 200) : (int)((1f - (t2-duration*0.2f)/(duration*0.1f)) * 200);
            if (a2 > 0) {
                mPaint.setShader(new RadialGradient(mX[i], mY[i], r * 0.25f,
                    new int[]{(a2 << 24) | 0xFFFFFF, 0x00000000},
                    new float[]{0f, 1f}, Shader.TileMode.CLAMP));
                c.drawCircle(mX[i], mY[i], r * 0.25f, mPaint);
            }
        }

        if (alive) postInvalidateDelayed(32);
    }
}
