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
    private int mStartAngle = 60;
    private int mMoveAngle = 0;
    private Path mPaths[] = new Path[mDatas.length+1];
    private Path mPathsItemView[] = new Path[mDatas.length];
    private Path mPathsTESTItemView[][] = new Path[mDatas.length][mDatas[0].length];
    private Paint mPaints[] = new Paint[mPaths.length];
    private Region mRregions[] = new Region[mPaths.length];
    private int mColors[] = new int[]{0xffffff34,0xffddff44,0xffff44ff};
    private int mLockFoolIndex = -1;

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

   Handler mUIHandler = new Handler(Looper.getMainLooper()){
       @Override
       public void dispatchMessage(Message msg) {
           super.dispatchMessage(msg);
           switch (msg.what){
               case 0:
                   removeMessages(0);
                   mMoveAngle+=4;
                   Log.d(TAG, "dispatchMessage: ===========>"+mMoveAngle);
                   requestLayout();
                   sendEmptyMessageDelayed(0,15);
                   break;
           }
       }
   };

    private void initView() {

        for (int i = 0;i < mPaths.length; i++){
            Paint paint = new Paint();
            //去锯齿
            paint.setAntiAlias(true);
            paint.setColor(mColors[i%mColors.length]);
            paint.setStrokeWidth(5);
            mPaints[i] = paint;
            mPaths[i] = new Path();
        }
        for (int i =0;i<mDatas.length;i++){
            for (int j = 0 ; j < mDatas[i].length;j++){
                mDatas[i][j] = new ImageView(getContext());
                mDatas[i][j].setImageResource(R.mipmap.ic_launcher);
                mDatas[i][j].setBackgroundColor(R.color.colorAccent);
                addView(mDatas[i][j]);
            }
        }
        mUIHandler.sendEmptyMessageDelayed(0,2000);
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

        for (int i =0;i<mDatas.length;i++){
            for (int j = 0 ; j < mDatas[i].length;j++){
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
        int mCirclePath = (int) (mCircleR/mPaths.length);
        Log.d(TAG, "onLayout: =======mCircleR:" + mCircleR);
        mCircleCenterPoint[0] = (r - l) / 2;
        mCircleCenterPoint[1] = (b - t) / 2;
        Log.d(TAG, "onLayout: =============mCircleCenterPoint=" + mCircleCenterPoint[0] + "|" + mCircleCenterPoint[1]);

        for (int i =0;i<mPaths.length;i++){
            float pCircleR = mCircleR-mCirclePath*i;
            float vCircleR = pCircleR - mCirclePath/2;
            Log.d(TAG, "onLayout: ==========vCircleR="+vCircleR+"|"+pCircleR);
            mPaths[i].addCircle(mCircleCenterPoint[0], mCircleCenterPoint[1], pCircleR , Path.Direction.CCW);
            if (vCircleR  > mCirclePath){
                mPathsItemView[i]=new Path();
                mPathsItemView[i].addCircle(mCircleCenterPoint[0], mCircleCenterPoint[1],vCircleR,Path.Direction.CCW);
                PathMeasure pathMeasure = new PathMeasure(mPathsItemView[i],false);
                ImageView[] imageViews = mDatas[i];
                float moveOffer = (float) (vCircleR*mMoveAngle*Math.PI/180.0);
                float startPoint = pathMeasure.getLength()/imageViews.length;
                for (int j = 0;j < imageViews.length;j++){
                    float[] pos= new float[2];
                    float[] tan = new float[2];
                    pathMeasure.getPosTan((j*startPoint+moveOffer)%pathMeasure.getLength(),pos,tan);
                    float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
                    ImageView imageView = imageViews[j];
                    imageView.layout((int)pos[0]-15,(int)pos[1]-15,(int)pos[0]+15,(int)pos[1]+15);
                    imageView.setPivotX(imageView.getWidth()/2);
                    imageView.setPivotY(imageView.getHeight()/2);//支点在图片中心
                    imageView.setRotation(degrees+180.0f);
                    mPathsTESTItemView[i][j] = new Path();
                    mPathsTESTItemView[i][j].addCircle(pos[0],pos[1],15,Path.Direction.CCW);


                    Log.d(TAG, "onLayout: ==========imageView::"+moveOffer+"|"+imageView.getBottom());
                    Log.d(TAG, "onLayout: ========getPosTan="+j*startPoint+"||"+(int)pos[0]+"|"+(int)pos[1]+"|"+tan[0]+"|"+tan[1]+"||"+degrees);
                }
                Log.d(TAG, "onLayout: =========mPathsItemView=>"+i);
                Log.d(TAG, "onLayout: ==============pathMeasure=>"+pathMeasure.getLength());
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < mRregions.length; i++) {
                    if (mRregions[i].contains((int) event.getX(), (int) event.getY())) {
                        mLockFoolIndex = i;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: move========>"+mLockFoolIndex);
                mMoveAngle++;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        Log.d(TAG, "dispatchDraw: =============>" + mCircleCenterPoint[0] + "|" + mCircleCenterPoint[1]);
        for (int i = 0; i < mPaths.length; i++) {
            canvas.drawPath(mPaths[i], mPaints[i]);
        }
        for (int i = 0; i < mPathsItemView.length; i++) {
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPathsItemView[i], p);
        }

        for (int i = 0; i<mPathsTESTItemView.length;i++){
            for (int j = 0 ; j < mPathsTESTItemView[i].length;j++){
                Paint p = new Paint();
                p.setColor(Color.DKGRAY);
                p.setStyle(Paint.Style.FILL);
                //canvas.drawPath(mPathsTESTItemView[i][j], p);
            }
        }
        super.dispatchDraw(canvas);
    }

}
