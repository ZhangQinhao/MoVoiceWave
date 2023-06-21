package com.monke.movoicewavelib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class MoVisualizerView extends View {
    public MoVisualizerView(Context context) {
        this(context, null);
    }

    public MoVisualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MoVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private int uiType = 0; //UI样式
    private int maxProgress;

    private Paint paint;
    private Paint paint2;

    private int[] colors = new int[]{Color.parseColor("#CC82C2EE"), Color.parseColor("#CCF89DC1"), Color.parseColor("#CC82C2EE")};  //必须大于2  第一个和第最后初始必须一样，用来作为渐变动画
    private float[] colorPos = new float[]{0f, 0f, 1.0f};   //第一个和第二个必须0f

    private int[] colors2 = new int[]{Color.parseColor("#99D87EE4"), Color.parseColor("#99F89DC1"), Color.parseColor("#99D87EE4")};  //必须大于2  第一个和第最后初始必须一样，用来作为渐变动画

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MoVisualizerView);
            uiType = typedArray.getInt(R.styleable.MoVisualizerView_movisualizerview_uiType, 0);
            typedArray.recycle();
        }
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);

        paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setDither(true);
        initUiType();
        maxProgress = 128 + defaultMin;
    }

    /**
     * 修改UI样式
     */
    public void setUiType(int type) {
        if (type != 0 && type != 1 && type != 2 && type != 3 && type != 4 && type != 5 && type != 6 && type != 7) {
            type = 0;
        }
        if (this.uiType != type) {
            this.uiType = type;
            initUiType();
            requestLayout();
        }
    }

    private int uiLineWidth;  //每根线宽度
    private float uiLineSpace; //间隔
    private int defaultMin;

    private void initUiType() {
        paint.setPathEffect(null);
        paint2.setPathEffect(null);
        switch (uiType) {
            case 0: {
                initUiType0();
            }
            break;
            case 1: {
                initUiType1();
            }
            break;
            case 2: {
                initUiType2();
            }
            break;
            case 3: {
                initUiType3();
            }
            break;
            case 4: {
                initUiType4();
            }
            break;
            case 5: {
                initUiType5();
            }
            break;
            case 6: {
                initUiType6();
            }
            break;
            case 7: {
                initUiType7();
            }
            break;
            default: {
                //后续添加不同样式
            }
            break;
        }
    }

    /**
     * 仅仅调用一次
     */
    private int density = 1;  //从数据源中取值密度
    private int animStep = 1;//如果有动画  动画步长次数
    private int[] steps;  //用来管理每一条数据的步长

    public void setInitData(int density, int animStep) {
        this.density = density;
        if (this.density <= 0) {
            this.density = 1;
        }
        this.animStep = animStep;
        if (this.animStep <= 0) {
            this.animStep = 1;
        }
    }

    /**
     * 取值傅里叶曲线
     */
    private byte[] originDatas; //原始数据
    private float dataScale;
    private float[] datas;
    private float[] dataTemp;
    private boolean lastAnim = false;

    public void updateData(@NonNull byte[] d, float dataScale) {
        originDatas = d;
        if (dataScale < 1) {
            dataScale = 1;
        }
        this.dataScale = dataScale;
        lastAnim = false;
        calData();
        invalidate();
    }

    /**
     * 取值傅里叶曲线
     */
    public void updateDataWithAnim(@NonNull byte[] d, float dataScale) {
        originDatas = d;
        if (dataScale < 1) {
            dataScale = 1;
        }
        this.dataScale = dataScale;
        lastAnim = true;
        calData();
        invalidate();
    }

    private int dataLength = 0; //真实绘制数据组长度

    private void calData() {
        if (originDatas == null) {
            if (datas != null) {
                int tempLength = getDataLengthByWidth();
                if (dataLength != tempLength) {
                    int minLength = Math.min(tempLength, dataLength);
                    dataLength = tempLength;
                    float[] temp = new float[dataLength];
                    System.arraycopy(datas, 0, temp, 0, minLength);
                    datas = temp;
                    temp = new float[dataLength];
                    System.arraycopy(dataTemp, 0, temp, 0, minLength);
                    dataTemp = temp;
                    int[] temp2 = new int[dataLength];
                    System.arraycopy(steps, 0, temp2, 0, minLength);
                    steps = temp2;
                }
            }
            return;
        }
        if (realWidth <= 0) {
            datas = null;
            dataTemp = null;
            steps = null;
            dataLength = 0;
        } else {
            dataLength = getDataLengthByWidth();
            if (datas == null || datas.length != dataLength) {
                datas = new float[dataLength];
                dataTemp = new float[dataLength];
                steps = new int[dataLength];
            }
            for (int i = 0; i < dataLength; i++) {
                if (i * density < originDatas.length) {
                    int t = (int) (originDatas[i * density] * dataScale);
                    if (t < -128) {
                        t = -128;
                    } else if (t > 127) {
                        t = 127;
                    }
                    datas[i] = t;
                } else {
                    datas[i] = 0;
                }
                if (lastAnim) {
                    float t1 = datas[i];
                    float t2 = dataTemp[i];
                    if (t1 == t2) {
                        steps[i] = 1;
                    } else if (t1 > t2) {
                        steps[i] = (int) Math.max(1, (t1 - t2) / animStep);
                    } else {
                        steps[i] = (int) Math.min(-1, (t1 - t2) / animStep);
                    }
                } else {
                    dataTemp[i] = datas[i];
                }
            }
            originDatas = null;

            if (colorAnimator == null && datas != null && datas.length > 0) {
                for (int i = 0; i < datas.length; i++) {
                    if (datas[i] != 0) {
                        startColorAnim();
                        break;
                    }
                }
            }
        }
    }

    private int getDataLengthByWidth() {
        if (uiType == 3) {
            return (int) ((realWidth - paint.getStrokeWidth()) / uiLineWidth);
        } else {
            return (int) ((realWidth + uiLineSpace) / (uiLineWidth + uiLineSpace));
        }
    }

    int realHeight = 0;
    int realWidth = 0;

    private float everyPointHeight;
    private int baseHeight;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            realHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();
        } else {
            realHeight = 300;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(realHeight + getPaddingBottom() + getPaddingTop(), MeasureSpec.AT_MOST);
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            realWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        } else {
            realWidth = 300;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(realWidth + getPaddingLeft() + getPaddingRight(), MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        paint.setShader(new LinearGradient(getPaddingLeft(), getPaddingTop(), getMeasuredWidth() - getPaddingRight(), getMeasuredHeight() - getPaddingBottom(), colors, colorPos, Shader.TileMode.REPEAT));
        paint2.setShader(new LinearGradient(getPaddingLeft(), getPaddingTop(), getMeasuredWidth() - getPaddingRight(), getMeasuredHeight() - getPaddingBottom(), colors2, colorPos, Shader.TileMode.REPEAT));
        if (uiType == 7) {
            everyPointHeight = realHeight * 0.5f / maxProgress;
            baseHeight = realHeight / 2 + getPaddingTop();
        } else {
            everyPointHeight = realHeight * 1.0f / maxProgress;
            baseHeight = getMeasuredHeight() - getPaddingBottom();
        }
        calData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (datas == null || dataTemp == null || steps == null || dataLength == 0 || realWidth <= 0 || realHeight <= 0) {
            return;
        }
        boolean needContinue = false;
        curColorData();
        if (uiType == 0) {
            float startX = getPaddingLeft();
            float endX = getMeasuredWidth() - getPaddingRight();
            drawType0(canvas, startX, endX);
        } else if (uiType == 1) {
            float startX = getPaddingLeft();
            float endX = getMeasuredWidth() - getPaddingRight();
            drawType1(canvas, startX, endX);
        } else if (uiType == 2) {
            float startX = getPaddingLeft();
            float endX = getMeasuredWidth() - getPaddingRight();
            drawType2(canvas, startX, endX);
        } else if (uiType == 3) {
            float startX = getPaddingLeft() + paint.getStrokeWidth() / 2;
            float endX = getMeasuredWidth() - getPaddingRight();
            drawType3(canvas, startX, endX);
        } else if (uiType == 4) {
            float startX = getPaddingLeft();
            float endX = getMeasuredWidth() - getPaddingRight();
            drawType4(canvas, startX, endX);
        } else if (uiType == 5) {
            float startX = getPaddingLeft();
            float endX = getMeasuredWidth() - getPaddingRight();
            drawType5(canvas, startX, endX);
        } else if (uiType == 6) {
            float startX = getPaddingLeft();
            float endX = getMeasuredWidth() - getPaddingRight();
            drawType6(canvas, startX, endX);
        } else if (uiType == 7) {
            float startX = getPaddingLeft();
            float endX = getMeasuredWidth() - getPaddingRight();
            drawType7(canvas, startX, endX);
        }


        //判断是否有过度动画
        for (int i = 0; i < dataLength; i++) {
            if (dataTemp[i] != datas[i]) {
                needContinue = true;
                if (dataTemp[i] > datas[i]) {
                    dataTemp[i] += steps[i];
                    if (dataTemp[i] < datas[i]) {
                        dataTemp[i] = datas[i];
                    }
                } else {
                    dataTemp[i] += steps[i];
                    if (dataTemp[i] > datas[i]) {
                        dataTemp[i] = datas[i];
                    }
                }
            }
        }
        if (needContinue) {
            postInvalidateOnAnimation();
        }
    }

    ///////////////////////////////样式0绘制/////////////////////////////////////////////////////////
    private int uiType0PointRadius;

    private void initUiType0() {
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        uiLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics());
        uiLineSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getResources().getDisplayMetrics());
        defaultMin = 6;
        uiType0PointRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
    }

    private RectF rectType0Line = new RectF();
    private RectF rectType0Oval = new RectF();

    private void drawType0(Canvas canvas, float startX, float endX) {
        if (dataTemp == null || dataTemp.length == 0) {
            return;
        }
        for (int i = 0; i < dataTemp.length; i++) {
            if (startX >= endX) {
                break;
            }
            float centerY = baseHeight - (Math.abs(dataTemp[i]) + defaultMin) * everyPointHeight;
            rectType0Line.left = startX;
            rectType0Line.top = centerY;
            rectType0Line.right = startX + uiLineWidth;
            rectType0Line.bottom = baseHeight;
            canvas.drawRect(rectType0Line, paint);
            float centerX = startX + uiLineWidth / 2.0f;
            rectType0Oval.left = centerX - uiType0PointRadius;
            rectType0Oval.top = centerY - uiType0PointRadius;
            rectType0Oval.right = centerX + uiType0PointRadius;
            rectType0Oval.bottom = centerY + uiType0PointRadius;
            canvas.drawOval(rectType0Oval, paint);
            startX += uiLineWidth + uiLineSpace;
        }
    }

    //////////////////////////////样式1绘制//////////////////////////////////////////////////////////

    private void initUiType1() {
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        uiLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
        uiLineSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
        defaultMin = 6;
    }

    private RectF rectType1 = new RectF();

    private void drawType1(Canvas canvas, float startX, float endX) {
        if (dataTemp == null || dataTemp.length == 0) {
            return;
        }
        for (int i = 0; i < dataTemp.length; i++) {
            if (startX >= endX) {
                break;
            }
            rectType1.left = startX;
            rectType1.top = baseHeight - (Math.abs(dataTemp[i]) + defaultMin) * everyPointHeight;
            rectType1.right = startX + uiLineWidth;
            rectType1.bottom = baseHeight;
            canvas.drawRect(rectType1, paint);
            startX += uiLineWidth + uiLineSpace;
        }
    }

    /////////////////////////////样式2绘制///////////////////////////////////////////////////////////
    private int uiTypePointProgress;
    private float uiTypePointSpaceProgress;

    private void initUiType2() {
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        uiLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, getResources().getDisplayMetrics());
        uiLineSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
        defaultMin = 4;
        uiTypePointProgress = defaultMin;
        uiTypePointSpaceProgress = 1f;
    }

    private RectF rectType2 = new RectF();

    private void drawType2(Canvas canvas, float startX, float endX) {
        if (dataTemp == null || dataTemp.length == 0) {
            return;
        }
        float tempA = uiTypePointProgress * everyPointHeight;
        float tempB = uiTypePointSpaceProgress * everyPointHeight;
        for (int i = 0; i < dataTemp.length; i++) {
            if (startX >= endX) {
                break;
            }
            float item = Math.abs(dataTemp[i]) + defaultMin;
            float t = 0;
            float startY = baseHeight;
            while (t < item) {
                rectType2.left = startX;
                rectType2.top = startY - tempA;
                rectType2.right = startX + uiLineWidth;
                rectType2.bottom = startY;
                canvas.drawRect(rectType2, paint);
                t += uiTypePointProgress + uiTypePointSpaceProgress;
                startY -= (tempA + tempB);
            }

            startX += uiLineWidth + uiLineSpace;
        }
    }

    ///////////////////////////样式3绘制/////////////////////////////////////////////////////////////
    private void initUiType3() {
        paint.setStrokeWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics()));
        paint.setStyle(Paint.Style.FILL);
        uiLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
        uiLineSpace = 0; //用不到
        defaultMin = 0;
    }

    private float type3PreX;
    private float type3PreY;

    private void drawType3(Canvas canvas, float startX, float endX) {
        if (dataTemp == null || dataTemp.length == 0) {
            return;
        }
        for (int i = 0; i < dataTemp.length; i++) {
            if (startX >= endX) {
                break;
            }
            if (i == 0) {
                type3PreY = baseHeight - (Math.abs(dataTemp[i]) + defaultMin) * everyPointHeight;
                canvas.drawLine(startX, type3PreY, startX, baseHeight, paint);
                type3PreX = startX + uiLineWidth;
                canvas.drawLine(type3PreX, type3PreY, type3PreX, baseHeight, paint);
                canvas.drawLine(startX, type3PreY, type3PreX, type3PreY, paint);
            } else {
                float tY = baseHeight - (Math.abs(dataTemp[i]) + defaultMin) * everyPointHeight;
                if (tY < type3PreY) {
                    canvas.drawLine(type3PreX, tY, type3PreX, type3PreY, paint);
                }
                type3PreX = startX + uiLineWidth;
                type3PreY = tY;
                canvas.drawLine(type3PreX, type3PreY, type3PreX, baseHeight, paint);
                canvas.drawLine(startX, type3PreY, type3PreX, type3PreY, paint);
            }
            startX += uiLineWidth;
        }
    }

    ///////////////////////////样式4绘制/////////////////////////////////////////////////////////////
    private int uiType4PointRadius;

    private void initUiType4() {
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        uiLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics());
        uiLineSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getResources().getDisplayMetrics());
        defaultMin = 6;
        uiType4PointRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
    }

    private RectF rectType4Oval = new RectF();

    private void drawType4(Canvas canvas, float startX, float endX) {
        if (dataTemp == null || dataTemp.length == 0) {
            return;
        }
        for (int i = 0; i < dataTemp.length; i++) {
            if (startX >= endX) {
                break;
            }
            float centerY = baseHeight - (Math.abs(dataTemp[i]) + defaultMin) * everyPointHeight;
            float centerX = startX + uiLineWidth / 2.0f;
            rectType4Oval.left = centerX - uiType4PointRadius;
            rectType4Oval.top = centerY - uiType4PointRadius;
            rectType4Oval.right = centerX + uiType4PointRadius;
            rectType4Oval.bottom = centerY + uiType4PointRadius;
            canvas.drawOval(rectType4Oval, paint);
            startX += uiLineWidth + uiLineSpace;
        }
    }

    ///////////////////////////////样式5绘制/////////////////////////////////////////////////////////
    private void initUiType5() {
        paint.setStyle(Paint.Style.FILL);
        uiLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics());
        paint.setStrokeWidth(uiLineWidth);
        uiLineSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getResources().getDisplayMetrics());
        defaultMin = 0;
    }

    private RectF rectType5Line = new RectF();

    private float type5PreX = 0;
    private float type5PreY = 0;

    private void drawType5(Canvas canvas, float startX, float endX) {
        if (dataTemp == null || dataTemp.length == 0) {
            return;
        }
        for (int i = 0; i < dataTemp.length; i++) {
            if (startX >= endX) {
                break;
            }
            float centerY = baseHeight - (Math.abs(dataTemp[i]) + defaultMin) * everyPointHeight;
            rectType5Line.left = startX;
            rectType5Line.top = centerY;
            rectType5Line.right = startX + uiLineWidth;
            rectType5Line.bottom = baseHeight;
            canvas.drawRect(rectType5Line, paint);
            float centerX = startX + uiLineWidth / 2.0f;
            if (i > 0) {
                canvas.drawLine(type5PreX, type5PreY, centerX, centerY, paint);
            }
            type5PreX = centerX;
            type5PreY = centerY;
            startX += uiLineWidth + uiLineSpace;
        }
    }

    ///////////////////////////////样式6绘制/////////////////////////////////////////////////////////
    private void initUiType6() {
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        paint.setPathEffect(new CornerPathEffect(40));
        paint2.setStrokeWidth(0);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setPathEffect(new CornerPathEffect(40));
        uiLineWidth = 1;
        uiLineSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics());
        defaultMin = 3;
    }

    private Path type6PathFont = new Path();
    private Path type6PathBg = new Path();

    private void drawType6(Canvas canvas, float startX, float endX) {
        if (dataTemp == null || dataTemp.length == 0) {
            return;
        }
        type6PathFont.reset();
        type6PathBg.reset();
        type6PathFont.setLastPoint(startX, baseHeight);
        type6PathBg.setLastPoint(startX, baseHeight);
        for (int i = 0; i < dataTemp.length; i++) {
            if (startX >= endX) {
                break;
            }
            float centerY = baseHeight - (Math.abs(dataTemp[i]) + defaultMin) * everyPointHeight;
            if (i > 0 && i < dataLength - 1) {
                if (dataTemp[i] > 0) {
                    type6PathFont.lineTo(startX, centerY);
                    type6PathBg.lineTo(startX, baseHeight - defaultMin * everyPointHeight);
                } else if (dataTemp[i] < 0) {
                    type6PathFont.lineTo(startX, baseHeight - defaultMin * everyPointHeight);
                    type6PathBg.lineTo(startX, centerY);
                } else {
                    type6PathFont.lineTo(startX, centerY);
                    type6PathBg.lineTo(startX, centerY);
                }
            }
            startX += uiLineWidth + uiLineSpace;
        }
        type6PathFont.lineTo(startX, baseHeight);
        type6PathFont.close();
        type6PathBg.lineTo(startX, baseHeight);
        type6PathBg.close();

        canvas.drawPath(type6PathFont, paint);
        canvas.drawPath(type6PathBg, paint2);
    }

    ///////////////////////////////样式7绘制/////////////////////////////////////////////////////////
    private void initUiType7() {
        paint.setStrokeWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics()));
        paint.setStyle(Paint.Style.FILL);
        uiLineWidth = 1;
        uiLineSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        defaultMin = 0;
    }

    private float type7PreX = 0;
    private float type7PreY = 0;

    private void drawType7(Canvas canvas, float startX, float endX) {
        if (dataTemp == null || dataTemp.length == 0) {
            return;
        }
        for (int i = 0; i < dataTemp.length; i++) {
            if (startX >= endX) {
                break;
            }
            float centerY = baseHeight - (dataTemp[i] + defaultMin) * everyPointHeight;
            float centerX = startX + uiLineWidth / 2.0f;
            if (i > 0) {
                canvas.drawLine(type7PreX, type7PreY, centerX, centerY, paint);
            }
            type7PreX = centerX;
            type7PreY = centerY;
            startX += uiLineWidth + uiLineSpace;
        }
    }

    ////////////////////////////////渐变动画/////////////////////////////////////////////////////////
    private ValueAnimator colorAnimator;
    private float curColorAnimatorValue = 0;
    private float colorAnimatorMove = 0;

    private void startColorAnim() {
        stopColorAnim();
        curColorAnimatorValue = 0f;
        colorAnimatorMove = 0f;
        colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(2500);  //渐变色滚动时间
        colorAnimator.setInterpolator(new LinearInterpolator());
        colorAnimator.setRepeatCount(-1);
        colorAnimator.setRepeatMode(ValueAnimator.RESTART);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (colors != null && colors.length > 2) {
                    float t = (float) animation.getAnimatedValue();
                    if (t > 0) {
                        colorAnimatorMove += (t - curColorAnimatorValue);
                        curColorAnimatorValue = t;
                    }
                }
            }
        });
        colorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                curColorAnimatorValue = 0;
            }
        });
        colorAnimator.start();
    }

    private void curColorData() {
        if (colorPos != null && colorPos.length > 2 && colorAnimatorMove >= 0.001f) {
            for (int i = colorPos.length - 1; i > 0; i--) {
                colorPos[i] += colorAnimatorMove;
            }

            while (colorPos[colorPos.length - 2] >= 1.0f) {
                colorPos[0] = colorPos[colorPos.length - 2] - (int) colorPos[colorPos.length - 2];
                for (int i = colorPos.length - 1; i > 0; i--) {
                    colorPos[i] = colorPos[i - 1];
                    colors[i] = colors[i - 1];
                    colors2[i] = colors2[i - 1];
                }
                colorPos[0] = 0;
                colors[0] = colors[colors.length - 1];
                colors2[0] = colors2[colors.length - 1];
            }

            colorAnimatorMove = 0;
            paint.setShader(new LinearGradient(getPaddingLeft(), getMeasuredHeight() - getPaddingBottom(), getMeasuredWidth() - getPaddingRight(), getPaddingTop(), colors, colorPos, Shader.TileMode.REPEAT));
            if (uiType == 6) {
                paint2.setShader(new LinearGradient(getPaddingLeft(), getMeasuredHeight() - getPaddingBottom(), getMeasuredWidth() - getPaddingRight(), getPaddingTop(), colors2, colorPos, Shader.TileMode.REPEAT));
            }
        }
    }

    private void stopColorAnim() {
        if (colorAnimator != null) {
            colorAnimator.cancel();
            colorAnimator = null;
        }
        curColorAnimatorValue = 0f;
        colorAnimatorMove = 0f;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        release();
    }

    /**
     * 数据清除
     */
    public void release() {
        stopColorAnim();
        lastAnim = false;
        dataTemp = null;
        datas = null;
        steps = null;
        dataLength = 0;
        originDatas = null;
        invalidate();
    }
}
