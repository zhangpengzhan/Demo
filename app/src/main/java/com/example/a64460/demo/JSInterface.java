package com.example.a64460.demo;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JSInterface {


    private Context context;

    public JSInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void toastShow() {
        Toast.makeText(context, "show===>", Toast.LENGTH_SHORT).show();
    }

}
