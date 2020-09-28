package com.android.projectchatting.custom;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class BackKeyPressedHandler {
    private static final String TAG = "BACK_KEY";
    private long backKeyClickTime = 0;
    private Activity activity;
    private Toast toast;

    public BackKeyPressedHandler(Activity activity) {
        this.activity = activity;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyClickTime + 2000) {
            backKeyClickTime = System.currentTimeMillis();
            showToast();
            return;
        } else {
            activity.finish();
            toast.cancel();
        }
    }

    public void showToast() {
        toast = Toast.makeText(activity, "\'뒤로 가기\' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}