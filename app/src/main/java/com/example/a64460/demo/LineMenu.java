package com.example.a64460.demo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;

import java.net.ConnectException;

public class LineMenu extends ViewGroup {
    private final String TAG = "LineMenu";
    private ImageView[] imageViews;
    private ValueAnimator valueAnimator;
    private int widthOffer,heightOffer;
    boolean orientation;
    public LineMenu(Context context,ImageView[] imageViews){
        super(context);
        this.imageViews = imageViews;
    }

    public LineMenu(Context context) {
        super(context);
        initView();
    }

    public LineMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        imageViews = new ImageView[6];
        ImageView imageView = new ImageView(context);
        imageViews[0] = imageView;
        imageView.setImageResource(R.mipmap.ic_launcher_round);
        ImageView imageView1 = new ImageView(context);
        imageViews[1] = imageView1;
        imageView1.setImageResource(R.mipmap.ic_launcher_round);
        ImageView imageView2 = new ImageView(context);
        imageViews[2] = imageView2;
        imageView2.setImageResource(R.mipmap.ic_launcher_round);
        ImageView imageView3 = new ImageView(context);
        imageViews[3] = imageView3;
        imageView3.setImageResource(R.mipmap.ic_launcher_round);
        ImageView imageView4 = new ImageView(context);
        imageViews[4] = imageView4;
        imageView4.setImageResource(R.mipmap.ic_launcher_round);
        ImageView imageView5 = new ImageView(context);
        imageViews[5] = imageView5;
        imageView5.setImageResource(R.mipmap.ic_launcher_round);
        initView();

    }

    public LineMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private void initView(){
        for (ImageView imageView:imageViews){
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            addView(imageView);
        }
        setBackgroundColor(Color.DKGRAY);
        valueAnimator = ValueAnimator.ofFloat(0,100);
        valueAnimator.setInterpolator(new AnticipateOvershootInterpolator());
        valueAnimator.addUpdateListener(animatorUpdateListener);
    }

    public void setImageViews(ImageView[] imageViews) {
        removeAllViews();
        this.imageViews = imageViews;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = (r - l);
        int height = (b - t);
        int childSize = imageViews.length;
        int childSpace;
        int[] point = new int[2];
        orientation = (width > height);
        childSpace = orientation?widthOffer/childSize:heightOffer/childSize;
        int halfChildSpace = childSpace/2-2;
        point[0] = orientation?(childSpace/2):(width/2);
        point[1] = orientation?(height/2):(childSpace/2);
        for (int i = 0 ; i < childSize; i++){
            int childPointX = orientation?(point[0]+childSpace*i):point[0];
            int childPointY = orientation?point[1]:(point[1]+childSpace*i);
            imageViews[i].layout(childPointX-halfChildSpace,childPointY-halfChildSpace,childPointX+halfChildSpace,childPointY+halfChildSpace);
        }
    }

    ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Float value = (Float) animation.getAnimatedValue();
            Log.d(TAG, "onAnimationUpdate: =====>"+value);
            widthOffer = (int) (orientation?getMeasuredWidth()*value/100:getMeasuredWidth());
            heightOffer = (int) (orientation?getMeasuredHeight():getMeasuredHeight()*value/100);
            requestLayout();
        }
    };

    public void showWithAnim(){
        valueAnimator.start();
        Log.d(TAG, "showWithAnim: ========>start");
    }

    public static class Build{
        private Context context;
        private ImageView[] imageViews;
        private int mOrientation;
        public Build(Context context){
            this.context = context;
        }

        public Build setImageViews(ImageView[] imageViews){
            this.imageViews = imageViews;
            return this;
        }

        public LineMenu builder(){
            return new LineMenu(context,imageViews);
        }
    }

    public interface LineMenuOnClick{
        void onClick(View view);
    }
}
