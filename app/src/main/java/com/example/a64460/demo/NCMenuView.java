package com.example.a64460.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

@SuppressLint("ResourceAsColor")
public class NCMenuView extends ViewGroup {
    public static final String TAG = NCMenuView.class.getSimpleName();
    private ImageView[][] mDatas = new ImageView[2][9];
    private float[] mCircleCenterPoint = new float[2];
    private float mCircleR;
    private int mStartAngle[] = new int[mDatas.length];
    private int mMoveAngle = 0;
    private Path mPaths[] = new Path[mDatas.length + 1];
    private Path mPathsItemView[] = new Path[mDatas.length];
    private Path mPathsTESTItemView[][] = new Path[mDatas.length][mDatas[0].length];
    private Paint mPaints[] = new Paint[mPaths.length];
    private Region mRregions[] = new Region[mPaths.length];
    private int mColors[] = new int[]{0xffffff34, 0xffddff44, 0xffff44ff};
    private int mLockFoolIndex = -1;
    private float mLastX, mLastY;
    private long mDownTime;
    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private static final int FLINGABLE_VALUE = 300;

    /**
     * 如果移动角度达到该值，则屏蔽点击
     */
    private static final int NOCLICK_VALUE = 3;

    /**
     * 当每秒移动角度达到该值时，认为是快速移动
     */
    private int mFlingableValue = FLINGABLE_VALUE;
    private boolean isFling;
    private AutoFlingRunnable mFlingRunnable;

    public NCMenuView(Context context) {
        super(context);
        initView();
    }

    public NCMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public NCMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();

    }

    Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case 0:
                    removeMessages(0);
                    mMoveAngle += 4;
                    Log.d(TAG, "dispatchMessage: ===========>" + mMoveAngle);
                    requestLayout();
                    sendEmptyMessageDelayed(0, 150);
                    break;
            }
        }
    };

    private void initView() {

        for (int i = 0; i < mPaths.length; i++) {
            Paint paint = new Paint();
            //去锯齿
            paint.setAntiAlias(true);
            paint.setColor(mColors[i % mColors.length]);
            paint.setStrokeWidth(5);
            mPaints[i] = paint;
            mPaths[i] = new Path();
        }
        for (int i = 0; i < mDatas.length; i++) {
            mStartAngle[i] = i*10;
            for (int j = 0; j < mDatas[i].length; j++) {
                mDatas[i][j] = new ImageView(getContext());
                mDatas[i][j].setImageResource(R.mipmap.ic_launcher);
                mDatas[i][j].setBackgroundColor(R.color.colorAccent);
                addView(mDatas[i][j]);
            }
        }
        //mUIHandler.sendEmptyMessageDelayed(0,2000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int resWidth, resHeight;
        /**
         * 如果宽或者高的测量模式非精确值
         */
        if (widthMode != MeasureSpec.EXACTLY
                || heightMode != MeasureSpec.EXACTLY) {
            // 主要设置为背景图的高度
            resWidth = getSuggestedMinimumWidth();

            resHeight = getSuggestedMinimumHeight();
        } else {
            // 如果都设置为精确值，则直接取小值；
            resWidth = resHeight = Math.min(width, height);
        }

        for (int i = 0; i < mDatas.length; i++) {
            for (int j = 0; j < mDatas[i].length; j++) {
                // mDatas[i][j].measure(30,30);
            }
        }
        // 获得半径
        int mRadius = Math.max(getMeasuredWidth(), getMeasuredHeight());
        Log.d(TAG, "onMeasure: ========>mR::" + mRadius + "|" + resWidth + "|" + resHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout===========1: " + changed + "|" + l + "|" + t + "|" + r + "|" + b);
        int width = (r - l);
        int height = (b - t);
        mCircleR = Math.min(width, height) / 2;
        int mCirclePath = (int) (mCircleR / mPaths.length);
        Log.d(TAG, "onLayout: =======mCircleR:" + mCircleR);
        mCircleCenterPoint[0] = (r - l) / 2;
        mCircleCenterPoint[1] = (b - t) / 2;
        Log.d(TAG, "onLayout: =============mCircleCenterPoint=" + mCircleCenterPoint[0] + "|" + mCircleCenterPoint[1]);

        for (int i = 0; i < mPaths.length; i++) {
            float pCircleR = mCircleR - mCirclePath * i;
            float vCircleR = pCircleR - mCirclePath / 2;
            Log.d(TAG, "onLayout: ==========vCircleR=" + vCircleR + "|" + pCircleR);
            mPaths[i].addCircle(mCircleCenterPoint[0], mCircleCenterPoint[1], pCircleR, Path.Direction.CW);
            if (vCircleR > mCirclePath) {
                ImageView[] imageViews = mDatas[i];
                float startAngle = 360 / imageViews.length;
                for (int j = 0; j < imageViews.length; j++) {
                    int[] pos = new int[2];
                    double[] tan = new double[2];
                    float mAngle = (i == mLockFoolIndex || mLockFoolIndex == -1) ?startAngle * j - mMoveAngle-mStartAngle[i]:startAngle * j;
                    // tmp cosa 即menu item中心点的横坐标
                    pos[0] = (int) Math.round(vCircleR * Math.sin(Math.toRadians(mAngle)) + mCircleCenterPoint[0]);
                    pos[1] = (int) Math.round(vCircleR * Math.cos(Math.toRadians(mAngle)) + mCircleCenterPoint[1]);
                    tan[0] = vCircleR * Math.sin(Math.toRadians(mAngle)-Math.PI/2 ) ;
                    tan[1] = vCircleR * Math.cos(Math.toRadians(mAngle) -Math.PI/2) ;
                    float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
                    if (i == 0) {
                        float[] linePos = new float[2];
                        linePos[0] = Math.round(mCircleR * Math.sin(Math.toRadians(mAngle + startAngle / 2)) + mCircleCenterPoint[0]);
                        linePos[1] = Math.round(mCircleR * Math.cos(Math.toRadians(mAngle + startAngle / 2)) + mCircleCenterPoint[1]);
                        float[] linePos1 = new float[2];
                        linePos1[0] = Math.round(2*mCircleR/3 * Math.sin(Math.toRadians(mAngle + startAngle / 2)) + mCircleCenterPoint[0]);
                        linePos1[1] = Math.round(2*mCircleR/3 * Math.cos(Math.toRadians(mAngle + startAngle / 2)) + mCircleCenterPoint[1]);
                        Path path = new Path();
                        path.moveTo(linePos1[0], linePos1[1]);
                        path.lineTo(linePos[0], linePos[1]);
                        Log.d(TAG, "onLayout: ==========linePos=" + linePos[0] + "||" + linePos[1]);
                        mPathsTESTItemView[i][j] = path;
                    }
                    Log.d(TAG, "onLayout: ==========degrees=" + degrees);
                    ImageView imageView = imageViews[j];
                    imageView.layout(pos[0] - 15, pos[1] - 15, pos[0] + 15, pos[1] + 15);
                    imageView.setPivotX(imageView.getWidth() / 2);
                    imageView.setPivotY(imageView.getHeight() / 2);//支点在图片中心
                    imageView.setRotation(degrees);
                    Log.d(TAG, "onLayout: ==========imageView::" +  "|" + imageView.getBottom());
                }
                Log.d(TAG, "onLayout: =========mPathsItemView=>" + i);
            }
            RectF rectF = new RectF();

            mPaths[i].computeBounds(rectF, true);
            Region region = new Region();
            region.setPath(mPaths[i], new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
            mRregions[i] = region;
        }


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mMoveAngle = 0;

                for (int i = 0; i < mRregions.length; i++) {
                    if (mRregions[i].contains((int) event.getX(), (int) event.getY())) {
                        mLockFoolIndex = i;
                        if (mLockFoolIndex >= mDatas.length){
                            mLockFoolIndex = -1;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: move========>" + mLockFoolIndex);
            if (mLockFoolIndex != -1) {
                /**
                 * 获得开始的角度
                 */
                float start = getAngle(mLastX, mLastY);
                /**
                 * 获得当前的角度
                 */
                float end = getAngle(x, y);

                // Log.e("TAG", "start = " + start + " , end =" + end);
                // 如果是一、四象限，则直接end-start，角度值都是正值
                if (getQuadrant(x, y) == 1 || getQuadrant(x, y) == 4) {
                    mStartAngle[mLockFoolIndex] += end - start;
                    mMoveAngle += end - start;
                } else
                // 二、三象限，色角度值是付值
                {
                    mStartAngle[mLockFoolIndex] += start - end;
                    mMoveAngle += start - end;
                }
                // 重新布局
                requestLayout();

                mLastX = x;
                mLastY = y;
            }
                break;
            case MotionEvent.ACTION_UP:

                // 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
                if (Math.abs(mMoveAngle) > NOCLICK_VALUE) {
                    return true;
                }

                break;
        }
        return true;
    }

    /**
     * 自动滚动的任务
     *
     * @author zhy
     */
    private class AutoFlingRunnable implements Runnable {

        private float angelPerSecond;

        public AutoFlingRunnable(float velocity) {
            this.angelPerSecond = velocity;
        }

        public void run() {
            // 如果小于20,则停止
            if ((int) Math.abs(angelPerSecond) < 20) {
                isFling = false;
                return;
            }
            isFling = true;
            // 不断改变mStartAngle，让其滚动，/30为了避免滚动太快
            mStartAngle[mLockFoolIndex] += (angelPerSecond / 30);
            // 逐渐减小这个值
            angelPerSecond /= 1.0666F;
            postDelayed(this, 30);
            // 重新布局
            requestLayout();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        Log.d(TAG, "dispatchDraw: =============>" + mCircleCenterPoint[0] + "|" + mCircleCenterPoint[1]);
        for (int i = 0; i < mPaths.length; i++) {
            //canvas.drawPath(mPaths[i], mPaints[i]);
        }
       /* for (int i = 0; i < mPathsItemView.length; i++) {
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPathsItemView[i], p);
        }*/



        for (int i = 0; i<mPathsTESTItemView.length;i++){
            for (int j = 0 ; j < mPathsTESTItemView[i].length;j++){
                if (mPathsTESTItemView[i][j] == null){
                    continue;
                }
                Paint p = new Paint();
                //去锯齿
                p.setAntiAlias(true);
                p.setColor(Color.DKGRAY);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(5);
                canvas.drawPath(mPathsTESTItemView[i][j], p);
            }
        }
        super.dispatchDraw(canvas);
    }

    /**
     * 根据当前位置计算象限
     *
     * @param x
     * @param y
     * @return
     */
    private int getQuadrant(float x, float y) {
        int tmpX = (int) (x - mCircleCenterPoint[0]);
        int tmpY = (int) (y - mCircleCenterPoint[1]);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }

    }

    /**
     * 根据触摸的位置，计算角度
     *
     * @param xTouch
     * @param yTouch
     * @return
     */
    private float getAngle(float xTouch, float yTouch) {
        double x = xTouch - mCircleCenterPoint[0];
        double y = yTouch - mCircleCenterPoint[1];
        return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
    }
}
