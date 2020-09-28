package com.android.projectchatting.custom;


import android.view.View;

import com.android.projectchatting.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class CustomSnackbar {

    public static void actionSnackbar(View view, String text, String actionText, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(view.getResources().getColor(R.color.light_gray));
        snackbar.setTextColor(view.getResources().getColor(R.color.white));
        snackbar.setActionTextColor(view.getResources().getColor(R.color.green));
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.setAction(actionText, onClickListener);
        snackbar.show();
    }

    public static void showError(View view, String text, String actionText) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(view.getResources().getColor(R.color.white));
        snackbar.setTextColor(view.getResources().getColor(R.color.black));
        snackbar.setActionTextColor(view.getResources().getColor(R.color.red));
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.setAction(actionText, v -> snackbar.dismiss());
        snackbar.show();
    }

    public static void showInfo(View view, String text, String actionText) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(view.getResources().getColor(R.color.light_gray));
        snackbar.setTextColor(view.getResources().getColor(R.color.white));
        snackbar.setActionTextColor(view.getResources().getColor(R.color.yellow));
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.setAction(actionText, v -> snackbar.dismiss());
        snackbar.show();
    }

    public static void showErrorNoActionText(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(view.getResources().getColor(R.color.white));
        snackbar.setTextColor(view.getResources().getColor(R.color.black));
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.show();
    }

    public static void showInfoNoActionText(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(view.getResources().getColor(R.color.light_gray));
        snackbar.setTextColor(view.getResources().getColor(R.color.white));
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.show();
    }

}
