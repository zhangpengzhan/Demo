package com.example.a64460.demo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

public class DemoService extends Service {
    private final String TAG =  DemoService.class.getSimpleName();
    private CameraReceiver cameraReceiver;

    public DemoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: =======service");

        if (cameraReceiver==null) {
            IntentFilter intentFilter = new IntentFilter ();
            try {
                intentFilter.addDataType("video/*");
                intentFilter.addDataType("image/*");

            } catch (IntentFilter.MalformedMimeTypeException e) {
                e.printStackTrace();
            }
            intentFilter.addAction ( "android.hardware.action.NEW_PICTURE" );
            //intentFilter.addAction ( "com.android.camera.NEW_PICTURE" );
            intentFilter.addAction ( "com.moon.camera.NEW_PICTURE" );
            cameraReceiver = new CameraReceiver ();
            getBaseContext().registerReceiver ( cameraReceiver, intentFilter );
            Log.d ( TAG, "init: ========>");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        getBaseContext().unregisterReceiver(cameraReceiver);
        super.onDestroy();
    }

    class CameraReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.moon.camera.NEW_PICTURE")){
                Log.d(TAG, "onReceive: ======test");
                return;
            }
            Cursor cursor = context.getContentResolver().query(intent.getData(),
                    null, null, null, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex("_data"));
            Log.d ( TAG, "onReceive: ===>" + path);
        }
    }
}
