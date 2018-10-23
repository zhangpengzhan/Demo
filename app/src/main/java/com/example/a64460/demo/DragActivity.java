package com.example.a64460.demo;

import android.app.Activity;
import android.os.Bundle;

import com.flavienlaurent.vdh.DragLayout;

/**
 * Created by Flavien Laurent (flavienlaurent.com) on 23/08/13.
 */
public class DragActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag);

        DragLayout dragLayout = (DragLayout) findViewById(R.id.dragLayout);


    }
}
