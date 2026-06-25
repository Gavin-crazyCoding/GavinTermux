package com.termux.menu.ui;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

/** 流体波纹动画 Drawable — 呼吸式涟漪效果 */
public class FluidRippleDrawable extends Drawable {

    private Paint mPaint;
    private float mRadius;
    private float mMaxRadius;
    private float mCenterX, mCenterY;
    private int mBaseColor;
    private ValueAnimator mAnimator;

    public FluidRippleDrawable(int baseColor) {
        mBaseColor = baseColor & 0x00FFFFFF; // strip alpha
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        startAnimation();
    }

    private void startAnimation() {
        mAnimator = ValueAnimator.ofFloat(0.3f, 1.0f);
        mAnimator.setDuration(2000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                mRadius = mMaxRadius * (Float) animation.getAnimatedValue();
                invalidateSelf();
            }
        });
        mAnimator.start();
    }

    @Override
    protected void onBoundsChange(android.graphics.Rect bounds) {
        super.onBoundsChange(bounds);
        mCenterX = bounds.exactCenterX();
        mCenterY = bounds.exactCenterY();
        mMaxRadius = Math.min(bounds.width(), bounds.height()) / 2f;
        if (mRadius == 0) mRadius = mMaxRadius * 0.5f;
    }

    @Override
    public void draw(Canvas canvas) {
        // 多层渐变模拟流体波纹
        float r = mRadius;
        int alpha1 = (int) (80 + 80 * (1 - r / mMaxRadius));
        int alpha2 = (int) (40 + 60 * (1 - r / mMaxRadius));
        int alpha3 = (int) (20 + 40 * (r / mMaxRadius));

        // 外层大波纹
        mPaint.setShader(new RadialGradient(mCenterX, mCenterY, r * 1.2f,
            new int[]{
                (alpha1 << 24) | (mBaseColor & 0x00FFFFFF),
                (alpha2 << 24) | (mBaseColor & 0x00FFFFFF),
                0x00000000
            }, new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(mCenterX, mCenterY, r * 1.2f, mPaint);

        // 中层波纹
        mPaint.setShader(new RadialGradient(mCenterX, mCenterY, r * 0.8f,
            new int[]{
                (alpha2 << 24) | (mBaseColor & 0x00FFFFFF),
                (alpha3 << 24) | (mBaseColor & 0x00FFFFFF),
                0x00000000
            }, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(mCenterX, mCenterY, r * 0.8f, mPaint);

        // 中心亮点
        mPaint.setShader(null);
        mPaint.setColor((int)(180 * (r / mMaxRadius)) << 24 | (mBaseColor & 0x00FFFFFF));
        canvas.drawCircle(mCenterX, mCenterY, r * 0.3f, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter colorFilter) {}

    @Override
    public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    public void stop() {
        if (mAnimator != null) mAnimator.cancel();
    }
}
