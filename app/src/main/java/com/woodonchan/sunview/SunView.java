package com.woodonchan.sunview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wudongchen on 16/5/24.
 * SunView
 */
public class SunView extends View {

    private Paint mDottedLinePaint;

    private Paint mBottomLinePaint;

    private Paint mSunRaysPaint;

    private Paint mSunPaint;

    private Paint mCoverPaint;

    private Paint mReplenishPaint;

    private Paint mShadowPaint;

    private float mWidth;

    private float mHeight;

    private float mRadius;

    private float mSunPercentage;

    private float mSunRadius;

    public SunView(Context context) {
        super(context);
        init();
    }

    public SunView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SunView);
        mSunPercentage = array.getInteger(R.styleable.SunView_sunPercentage, 0);
        mSunRadius = array.getInteger(R.styleable.SunView_sunRadius, 40);
        array.recycle();

        init();
    }


    public void setSunPercentage(int sunPercentage) {
        mSunPercentage = sunPercentage;
    }

    public void setSunRadius(float sunRadius) {
        mSunRadius = sunRadius;
    }

    private void init() {
        mDottedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDottedLinePaint.setARGB(255, 254, 226, 174);
        mDottedLinePaint.setStrokeWidth(5);
        mDottedLinePaint.setStyle(Paint.Style.STROKE);
        PathEffect effects = new DashPathEffect(new float[]{15, 15}, 0);
        mDottedLinePaint.setPathEffect(effects);

        mBottomLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBottomLinePaint.setARGB(255, 254, 210, 125);
        mBottomLinePaint.setStrokeWidth(5);
        mBottomLinePaint.setStyle(Paint.Style.STROKE);

        mSunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSunPaint.setARGB(255, 254, 209, 120);
        mSunPaint.setStyle(Paint.Style.FILL);

        mSunRaysPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSunRaysPaint.setARGB(255, 254, 209, 120);
        mSunRaysPaint.setStyle(Paint.Style.STROKE);
        mSunRaysPaint.setStrokeWidth(12);
        PathEffect sunRaysEffects = new DashPathEffect(new float[]{5, 12}, 0);
        mSunRaysPaint.setPathEffect(sunRaysEffects);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.rgb(255, 238, 205));
        mShadowPaint.setStyle(Paint.Style.FILL);

        mCoverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCoverPaint.setARGB(255, 255, 255, 255);
        mCoverPaint.setStyle(Paint.Style.FILL);

        mReplenishPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReplenishPaint.setARGB(255, 255, 238, 205);
        mReplenishPaint.setStyle(Paint.Style.FILL);
    }

    private float mCurrentPercentage;

    private void drawSunView(Canvas canvas) {

        mCurrentPercentage += 0.5;

        float x = mWidth / 2;
        float y = mHeight / 2;

        //矩形轮廓
        RectF dottedLineRectF = new RectF(x - mRadius, y - mRadius, x + mRadius, y + mRadius);
        float xx = x / 4;
        dottedLineRectF.inset(xx, xx);

        //计算当前角度太阳的坐标
        Path path = new Path();
        path.addArc(dottedLineRectF, -0, -180);
        PathMeasure measure = new PathMeasure(path, false);
        float sunDegrees = (float) (180 - mCurrentPercentage * 1.8);
        float[] sunXY = new float[2];
        measure.getPosTan((sunDegrees) * measure.getLength() / 180, sunXY, null);

        //太阳扇形阴影
        float startAngle = -sunDegrees;
        float sweepAngle = -180 - startAngle;
        canvas.drawArc(dottedLineRectF, startAngle, sweepAngle, true, mShadowPaint);

        //太阳扇形阴影多余部分遮盖
        if (sunDegrees >= 90) {
            RectF rectF = new RectF(sunXY[0], sunXY[1], x, y);
            canvas.drawRect(rectF, mCoverPaint);
        } else {
            Path trianglePath = new Path();
            trianglePath.moveTo(x, y);
            trianglePath.lineTo(sunXY[0], sunXY[1]);
            trianglePath.lineTo(sunXY[0], y);
            canvas.drawPath(trianglePath, mReplenishPaint);
        }

        //太阳轨迹的虚线
        canvas.drawArc(dottedLineRectF, -0, -180, true, mDottedLinePaint);

        //太阳
        if (mSunRadius < 40) mSunRadius = 40;
        canvas.drawCircle(sunXY[0], sunXY[1], mSunRadius, mSunRaysPaint);
        canvas.drawCircle(sunXY[0], sunXY[1], mSunRadius - 12, mSunPaint);

        //地平线
        canvas.drawLine(0, y, mWidth, y, mBottomLinePaint);

        //日出或日落半个太阳的遮罩层
        canvas.drawRect(0, y + 1, mWidth, y + 1 + mSunRadius, mCoverPaint);

        if (mCurrentPercentage <= ((mSunPercentage > 100) ? 100 : mSunPercentage)) {
            invalidate();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();

        mHeight = getHeight();

        mRadius = Math.min(mWidth, mHeight) / 2;

        canvas.drawRGB(255, 255, 255);

        drawSunView(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    result = Math.max(result, size);
                } else {
                    result = Math.max(result, size);
                }
            }
        }
        return result;
    }
}
