package com.flavienlaurent.vdh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class DragView extends View {
    private String TAG = DragView.class.getSimpleName();
    private int lastX;
    private int lastY;





    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent event) {
        //获取到手指处的横坐标和纵坐标
        int x = (int) event.getX();
        int y = (int) event.getY();
        Log.d(TAG, "onTouchEvent: x==> " + x + "|y:" + y + "|getleft:" + getLeft() + "|getTop:" + getTop() + "|getWidth:" + getWidth());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                //计算移动的距离
                int offX = x - lastX;
                int offY = y - lastY;
                //调用layout方法来重新放置它的位置
                /*layout(getLeft()+offX, getTop()+offY,
                        getRight()+offX  , getBottom()+offY);*/
                if (Math.abs(offX)>getWidth()/2||Math.abs(offY)>getHeight()/2){
                    break;
                }
                offsetLeftAndRight(offX);
                offsetTopAndBottom(offY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "onTouchEvent: ===ACTION_POINTER_DOWN");

                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onTouchEvent: ===ACTION_POINTER_UP");

                break;
        }
        return true;
    }


}
