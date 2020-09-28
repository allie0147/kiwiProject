package com.android.projectchatting.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.projectchatting.R;

public class CustomProgressDialog extends Dialog {
    private static final String TAG = "CUSTOM_DIALOG";

    public CustomProgressDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.custom_progress_dialog);
    }

    public void setTitleView(String title) {
        TextView titleView = findViewById(R.id.progress_bar_title);
        titleView.setVisibility(View.VISIBLE);
        titleView.setText(title);
    }
}
