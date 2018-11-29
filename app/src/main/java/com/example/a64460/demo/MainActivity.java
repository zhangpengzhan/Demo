package com.example.a64460.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.moon.circleimageview.CircleImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnErrorListener ,SurfaceHolder.Callback {


    private SurfaceView videoView;
    private Button button,button2;
    private MediaController mMediaController;
    private MediaPlayer mPlayer;
    private WebView webView;
    String fileUrl = "file:///android_asset/home-2.mp4";
    String fileUrl2 = "https://apd-videohy.apdcdn.tc.qq.com/vkp.tc.qq.com/A_roHJiG2R4igYVcqYUEbc8sQ1d9emaR3E38E6IsRArs/q00" +
            "16xntkk5.p202.1.mp4?sdtfrom=v1010&guid=969a81a3deb216034bedb03af195de2e&vkey=8C8D55D67151707A547FEBD6546D3A1BD9752F" +
            "A2336AEBC0B8BB872038D8AAC570AB940CBBC9FD48178E607EC493AB3FB809D4905A4B0A634D4BD497DAB3F2AD581100F825A7CA11BA08F57CE8883" +
            "C0813D26EAF4FA2BD4BF7F346B16F4BA74B0976B9FB8B1705084994DB26C90979A01E3189174652C8A9&ocid=450109356";
    String fileUrl1 = "file:///android_asset/index01.html";
    private String TAG = MainActivity.class.getSimpleName();
    private DilatingDotsProgressBar mDilatingDotsProgressBar;
    final int[] ids = {R.mipmap.a1, R.mipmap.a2, R.mipmap.a3, R.mipmap.a4};

    private final String iamgeURl = "http://img.itlun.cn/uploads/allimg/170925/1-1F925224318-lp.jpg";

    private CircleImageView imageView;
    private ImageView imageView2;
    private AnimationDrawable animationDrawable;
    int index = 0;
    private SurfaceHolder surfaceHolder;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        imageView = (CircleImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView2.setImageDrawable(imageView.getDrawable());
        imageView.setAnimAdapter(new CircleImageView.AnimAdapter(ids, 100, false));
        imageView.startAnimDelay(50);
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //imageView.stopAnim();
            }
        }, 3000);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Toast.makeText(getBaseContext(), "=======", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick1: ==============intent");
                /* //创建Intent
                Intent intent = new Intent();
                intent.setAction("com.moon.camera.NEW_PICTURE");
                //发送广播
                sendBroadcast(intent);

                Log.d(TAG, "onClick2: ==============intent");*/
                Intent intent = new Intent(MainActivity.this, MainActivity1.class);
                intent.putExtra("horizontal", true);
                startActivity(intent);
               // mPlayer.start();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                intent.putExtra("horizontal", true);
                startActivity(intent);
            }
        });
        //ImageLoader.getInstance().displayImage(iamgeURl,imageView);
        webView = (WebView) findViewById(R.id.webView);
        webView.requestFocus(View.FOCUS_DOWN);
        //webView.requestFocusFromTouch();
        String data = getFromAssets("index01.html");
        Log.d(TAG, "onCreate: ====>data:" + data);
//        webView.loadData(data,"text/html","utf-8");
        //webView.loadUrl(/*fileUrl1*/"https://www.vipkid.com.cn/login"/*"https://www.baidu.com"*/);
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(new JSInterface(this), "jsi");
        //声明WebSettings子类
        WebSettings webSettings = webView.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);


        //设置自适应屏幕，两者合用（下面这两个方法合用）
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        webSettings.setSupportZoom(false); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式

        videoView = (SurfaceView) findViewById(R.id.videoView);
        //初始化SurfaceHolder类，SurfaceView的控制器
        surfaceHolder = videoView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFixedSize(320, 220);   //显示的分辨率,不设置为视频默认
        checkPermission();


        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                Log.d(TAG, "onEditorAction: ==============>" + actionId + "||" + event.getKeyCode());
                return false;
            }
        });

        //  WindowUtils.showPopupWindow(getBaseContext());
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        super.onProvideKeyboardShortcuts(data, menu, deviceId);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent: ===============>" + event.getKeyCode());
        return false;

    }

    /**
     * 通过XML添加帧动画方法二
     */
    private void setXml2FrameAnim2() {


        // 通过逐帧动画的资源文件获得AnimationDrawable示例
        animationDrawable = (AnimationDrawable) getResources().getDrawable(
                R.drawable.fram_d);

        imageView.setBackground(animationDrawable);
        animationDrawable.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDilatingDotsProgressBar = (DilatingDotsProgressBar) findViewById(R.id.progress);
        mDilatingDotsProgressBar.show(500);

        Intent intent = new Intent(this, DemoService.class);
        startService(intent);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    //从assets 文件夹中获取文件并读取数
    public String getFromAssets(String fileName) {
        String result = "";
        try {
            InputStream in = getResources().getAssets().open(fileName);
            //获取文件的字节数
            int lenght = in.available();
            //创建byte数组
            byte[] buffer = new byte[lenght];
            //将文件中的数据读到byte数组中
            in.read(buffer);
            result = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 200);

        } else {
            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
            Log.e("===>", "checkPermission: 已经授权！");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer = new MediaPlayer();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mPlayer.setDataSource(new myKTVMediaDataSource(myKTVMediaDataSource.path));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /*try {
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setDisplay(surfaceHolder);    //设置显示视频显示在SurfaceView上
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.release();
    }
}

