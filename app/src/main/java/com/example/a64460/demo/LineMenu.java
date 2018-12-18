package com.example.a64460.demo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.net.ConnectException;

public class LineMenu extends ViewGroup {
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
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ic_launcher_background);
        addView(imageView);
        ImageView imageView1 = new ImageView(context);
        imageView1.setImageResource(R.drawable.ic_launcher_background);
        addView(imageView1);
        ImageView imageView2 = new ImageView(context);
        imageView2.setImageResource(R.drawable.ic_launcher_background);
        addView(imageView2);
        ImageView imageView3 = new ImageView(context);
        imageView3.setImageResource(R.drawable.ic_launcher_background);
        addView(imageView3);
        ImageView imageView4 = new ImageView(context);
        imageView4.setImageResource(R.drawable.ic_launcher_background);
        addView(imageView4);
        ImageView imageView5 = new ImageView(context);
        imageView5.setImageResource(R.drawable.ic_launcher_background);
        addView(imageView5);

    }

    public LineMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private void initView(){
        for (ImageView imageView:imageViews){
            addView(imageView);
        }
        valueAnimator = ValueAnimator.ofFloat(0,100);
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
        point[0] = orientation?(childSize/2):(width/2);
        point[1] = orientation?(height/2):(childSpace/2);
        for (int i = 0 ; i < childSize; i++){
            int childPointX = orientation?(point[0]+childSpace*i):point[0];
            int childPointY = orientation?point[1]:(point[1]+childSpace*i);
            imageViews[i].layout(childPointX-30,childPointY-30,childPointX+30,childPointY+30);
        }
    }

    ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Float value = (Float) animation.getAnimatedValue();
            widthOffer = (int) (orientation?getMeasuredWidth()*value/100:getMeasuredWidth());
            heightOffer = (int) (orientation?getMeasuredHeight():getMeasuredHeight()*value/100);
        }
    };

    public void showWithAnim(){
        valueAnimator.start();
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
}
