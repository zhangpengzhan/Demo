package net.majorkernelpanic.example1;

import net.majorkernelpanic.spydroid.Utilities;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;
import android.net.Uri;

import java.text.BreakIterator;
import java.util.Locale;

/**
 * A straightforward example of how to use the RTSP server included in libstreaming.
 */
public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    private SurfaceView mSurfaceView;

    boolean recording;
    //private Camera myCamera;
    //private MediaRecorder mediaRecorder;
    SurfaceHolder surfaceHolder;
    Button myButton;

    final static int REQUEST_VIDEO_CAPTURED = 1;
    Uri uriVideo = null;
    VideoView videoviewPlay;

    //Creating servlet variables
    MyHttpServer server;
    private RtspServer mRtspServer;
    private TextView mLine1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mLine1 = findViewById(R.id.line1);
        Button myButton = (Button) findViewById(R.id.myButton);
        myButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub


                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, REQUEST_VIDEO_CAPTURED);
            }
        });


        Button btnPrefs = (Button) findViewById(R.id.btnPrefs);
        btnPrefs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(MainActivity.this, PrefsActivity.class);
                startActivity(intent);
            }
        });


        mSurfaceView = (SurfaceView) findViewById(R.id.surface);


        // Sets the port of the RTSP server to 1234
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(RtspServer.KEY_PORT, String.valueOf(1234));
        editor.commit();


        // Configures the SessionBuilder
        SessionBuilder.getInstance()
                .setSurfaceView(mSurfaceView)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                //.setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioEncoder(SessionBuilder.AUDIO_AMRNB)
                .setVideoEncoder(SessionBuilder.VIDEO_H264);

        // Starts the RTSP server
        bindService(new Intent(this, RtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection mRtspServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ==========");
            mRtspServer = (RtspServer) ((RtspServer.LocalBinder) service).getService();
            mRtspServer.addCallbackListener(mRtspCallbackListener);
            mRtspServer.start();
            displayIpAddress();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ========");
        }

    };
    private RtspServer.CallbackListener mRtspCallbackListener = new RtspServer.CallbackListener() {

        @Override
        public void onError(RtspServer server, Exception e, int error) {
            // We alert the user that the port is already used by another app.
            if (error == RtspServer.ERROR_BIND_FAILED) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.port_used)
                        .setMessage(getString(R.string.bind_failed, "RTSP"))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                startActivityForResult(new Intent(MainActivity.this, OptionsActivity.class), 0);
                            }
                        })
                        .show();
            }
        }

        @Override
        public void onMessage(RtspServer server, int message) {
            Log.d(TAG, "onMessage: =========="+message);
            if (message == RtspServer.MESSAGE_STREAMING_STARTED) {

            } else if (message == RtspServer.MESSAGE_STREAMING_STOPPED) {

            }
        }

    };

    private void displayIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ipaddress = null;
        if (info != null && info.getNetworkId() > -1) {
            int i = info.getIpAddress();
            String ip = String.format(Locale.ENGLISH, "%d.%d.%d.%d", i & 0xff, i >> 8 & 0xff, i >> 16 & 0xff, i >> 24 & 0xff);
            mLine1.setText("rtsp://");
            mLine1.append(ip);
            mLine1.append(":" + mRtspServer.getPort());
        } else if ((ipaddress = Utilities.getLocalIpAddress(true)) != null) {
            mLine1.setText("rtsp://");
            mLine1.append(ipaddress);
            mLine1.append(":" + mRtspServer.getPort());
        } else {
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.stop();
    }
}
