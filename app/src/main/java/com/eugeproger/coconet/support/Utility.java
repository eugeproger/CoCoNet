package com.eugeproger.coconet.support;

import android.content.Context;
import android.widget.Toast;

public class Utility {

    public static void showShortToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showLengthToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void showErrorToast(Context context, String errorMessage) {
        Toast.makeText(context, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
}
