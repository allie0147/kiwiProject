package com.android.projectchatting.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;


import com.android.projectchatting.R;

public class CustomDialog extends Dialog implements View.OnClickListener {
    CustomDialogListener dialogListener;
    TextView msgView, titleView;
    TextView positiveBtn;
    TextView negativeBtn;

    // title, message, button2
    public CustomDialog(Context context) {
        super(context);
        setContentView(R.layout.custom_dialog);
        setCancelable(false);
        msgView = findViewById(R.id.dialog_message);
        titleView = findViewById(R.id.dialog_title);
        positiveBtn = findViewById(R.id.dialog_btn);
        positiveBtn.setOnClickListener(this);
        negativeBtn = findViewById(R.id.dialog_dismiss_btn);
        negativeBtn.setOnClickListener(this);
    }

    // message, button1
    public CustomDialog(Context context, String message, String positiveText) {
        super(context);
        setContentView(R.layout.custom_dialog);
        setCancelable(false);
        msgView = findViewById(R.id.dialog_message);
        msgView.setText(message);
        positiveBtn = findViewById(R.id.dialog_btn);
        positiveBtn.setText(positiveText);
        positiveBtn.setOnClickListener(this);
    }

    // message, button2
    public CustomDialog(Context context, String message, String positiveText, String negativeText) {
        super(context);
        setContentView(R.layout.custom_dialog);
        setCancelable(false);
        msgView = findViewById(R.id.dialog_message);
        msgView.setText(message);
        positiveBtn = findViewById(R.id.dialog_btn);
        positiveBtn.setText(positiveText);
        negativeBtn = findViewById(R.id.dialog_dismiss_btn);
        negativeBtn.setVisibility(View.VISIBLE);
        negativeBtn.setText(negativeText);
        positiveBtn.setOnClickListener(this);
        negativeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_btn:
                dialogListener.onPositiveClicked();
                break;
            case R.id.dialog_dismiss_btn:
                dialogListener.onNegativeClicked();
                break;
        }
    }

    public interface CustomDialogListener {
        void onPositiveClicked();

        void onNegativeClicked();
    }

    public void setDialogListener(CustomDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    // message, button
    public void makeSimpleDialog(String message, String text) {
        if (titleView.getVisibility() == View.VISIBLE) {
            titleView.setVisibility(View.GONE);
        }
        if (negativeBtn.getVisibility() == View.VISIBLE) {
            negativeBtn.setVisibility(View.GONE);
        }
        msgView.setText(message);
        positiveBtn.setText(text);
    }

    //message, two buttons
    public void makeButtonsDialog(String message, String positiveText, String negativeText) {
        if (titleView.getVisibility() == View.VISIBLE) {
            titleView.setVisibility(View.GONE);
        }
        msgView.setText(message);
        positiveBtn.setText(positiveText);
        negativeBtn.setVisibility(View.VISIBLE);
        negativeBtn.setText(negativeText);
    }

    //message, title, button
    public void makeTitleDialog(String title, String message, String positiveText) {
        if (negativeBtn.getVisibility() == View.VISIBLE) {
            negativeBtn.setVisibility(View.GONE);
        }
        titleView.setVisibility(View.VISIBLE);
        titleView.setText(title);
        msgView.setText(message);
        positiveBtn.setText(positiveText);
    }

    //message, title, two buttons
    public void makeFullDialog(String title, String message, String positiveText, String negativeText) {
        titleView.setVisibility(View.VISIBLE);
        negativeBtn.setVisibility(View.VISIBLE);
        titleView.setText(title);
        msgView.setText(message);
        positiveBtn.setText(positiveText);
        negativeBtn.setText(negativeText);
    }

}