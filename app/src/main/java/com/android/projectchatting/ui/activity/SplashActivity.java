package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.CustomDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    private LinearLayout linearLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseAuth auth;
    private static final String TAG = "SplashActivity_debug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        linearLayout = findViewById(R.id.splash_layout);
        auth = FirebaseAuth.getInstance();
        // firebase 원격 지원
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1200)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, task + "task succeeded");
                    } else {
                        Log.d(TAG, task + "task failed");
                    }
                    displayWelcomeMessage();
                });

    }

    private void displayWelcomeMessage() {
        Log.e("background_color", mFirebaseRemoteConfig.getString("splash_background"));
        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
        boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splash_message = mFirebaseRemoteConfig.getString("splash_message");
        linearLayout.setBackgroundColor(Color.parseColor(splash_background));
        if (caps) {
            CustomDialog dialog = new CustomDialog(this, splash_message, "확인");
            dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                @Override
                public void onPositiveClicked() {
                    finish();
                }

                @Override
                public void onNegativeClicked() {

                }
            });
            dialog.show();
        } else {
            FirebaseUser user = auth.getCurrentUser();
//            Log.d(TAG, "displayWelcomeMessage: " + user);
            if (user == null) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            } else {
                FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.child("location").getValue() == null) {
                                    Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(loginIntent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                    ArrayList<String> locationList = new ArrayList<>();
                                    locationList.add(String.valueOf(snapshot.child("locationId").getValue()));
                                    locationList.add(String.valueOf(snapshot.child("location").getValue()));
                                    intent.putStringArrayListExtra(MainActivity.LOCATION_KEY, locationList);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
            }
        }
    }
}