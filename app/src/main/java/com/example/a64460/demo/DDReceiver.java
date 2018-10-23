package com.example.a64460.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DDReceiver extends BroadcastReceiver {
    private final String TAG = DDReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(TAG, "onReceive: ======="+intent.getAction());
    }
}
