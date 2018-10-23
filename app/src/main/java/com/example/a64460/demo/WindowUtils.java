package com.example.a64460.demo;


import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

/**
 * 弹窗辅助类
 *
 * @ClassName WindowUtils
 *
 *
 */
public class WindowUtils {

    private static final String LOG_TAG = "WindowUtils";
    private static View mView = null;
    private static WindowManager mWindowManager = null;
    private static Context mContext = null;

    public static Boolean isShown = false;

    /**
     * 显示弹出框
     *
     * @param context
     */
    public static void showPopupWindow(final Context context) {
        if (isShown) {
            Log.i(LOG_TAG, "return cause already shown");
            return;
        }

        isShown = true;
        Log.i(LOG_TAG, "showPopupWindow");

        // 获取应用的Context
        mContext = context.getApplicationContext();
        // 获取WindowManager
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);

        mView = setUpView(context);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        // 类型
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

        // 设置flag

        int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM|LayoutParams.FLAG_NOT_TOUCH_MODAL;
        // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
        // 不设置这个flag的话，home页的划屏会有问题

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.BOTTOM;

        mWindowManager.addView(mView, params);

        Log.i(LOG_TAG, "add view");

    }

    /**
     * 隐藏弹出框
     */
    public static void hidePopupWindow() {
        Log.i(LOG_TAG, "hide " + isShown + ", " + mView);
        if (isShown && null != mView) {
            Log.i(LOG_TAG, "hidePopupWindow");
            mWindowManager.removeView(mView);
            isShown = false;
        }

    }

    private static View setUpView(final Context context) {

        Log.i(LOG_TAG, "setUp view");

        View view = LayoutInflater.from(context).inflate(R.layout.window_manager,
                null);




        // 点击back键可消除
        view.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        WindowUtils.hidePopupWindow();
                        return true;
                    default:
                        return false;
                }
            }
        });

        return view;

    }
}


