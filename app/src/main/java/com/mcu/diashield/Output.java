package com.mcu.diashield;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Output {
    public static void message(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        Log.d("tag",message);
    }
}