package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.BackKeyPressedHandler;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.ui.fragment.ChatListFragment;
import com.android.projectchatting.ui.fragment.HomeFragment;
import com.android.projectchatting.ui.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    public static final String LOCATION_KEY = "location_key";
    public static final String EXTRA_CHAT_LOCATION_KEY = "chatting";
    public static final String EXTRA_SETTINGS_KEY = "settings";
    private static final String TAG = "MAIN_ACTIVITY";
    private BottomNavigationView bottomNavigationView;

    private HomeFragment homeFragment;
    private ChatListFragment chatListFragment;
    private SettingsFragment settingsFragment;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private ArrayList<String> locations;

    private BackKeyPressedHandler handler = new BackKeyPressedHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        homeFragment = new HomeFragment();
        chatListFragment = new ChatListFragment();
        settingsFragment = new SettingsFragment();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else if (getIntent().hasExtra(LOCATION_KEY)) {
            Log.d(TAG, "LOCATION_KEY");
            bottomNavigationView.setOnNavigationItemSelectedListener(this);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (getIntent().hasExtra(EXTRA_CHAT_LOCATION_KEY)) {
            Log.d(TAG, "EXTRA_CHAT_LOCATION_KEY");
            bottomNavigationView.setOnNavigationItemSelectedListener(this);
            bottomNavigationView.setSelectedItemId(R.id.nav_chat);
        } else if (getIntent().hasExtra(EXTRA_SETTINGS_KEY)) {
            Log.d(TAG, "EXTRA_SETTINGS_KEY");
            bottomNavigationView.setOnNavigationItemSelectedListener(this);
            bottomNavigationView.setSelectedItemId(R.id.nav_setting);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.nav_home:
                locations = new ArrayList<>();
                Bundle bundle = new Bundle();
                Log.d(TAG, "onNavigationItemSelected: " + getIntent().hasExtra(LOCATION_KEY));
                if (getIntent().getStringArrayListExtra(LOCATION_KEY) == null) {
                    if (getIntent().getStringArrayListExtra(EXTRA_CHAT_LOCATION_KEY) == null) {
                        locations = getIntent().getStringArrayListExtra(EXTRA_SETTINGS_KEY);
                    } else {
                        locations = getIntent().getStringArrayListExtra(EXTRA_CHAT_LOCATION_KEY);
                    }
                } else locations = getIntent().getStringArrayListExtra(LOCATION_KEY);
                Log.d(TAG, "onNavigationItemSelected: " + locations.toString());
                bundle.putStringArrayList(LOCATION_KEY, locations);
                homeFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                break;
            case R.id.nav_write:
                showDialogWrite();
                break;
            case R.id.nav_chat:
                Bundle chatBundle = new Bundle();
                locations = new ArrayList<>();
                locations = getIntent().getStringArrayListExtra(EXTRA_CHAT_LOCATION_KEY);
                Log.d(TAG, "onNavigationItemSelected: nav_chat");
                chatBundle.putStringArrayList(ChatListFragment.EXTRA_CHAT_LOCATION_KEY, locations);
                chatListFragment.setArguments(chatBundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, chatListFragment).commit();
                break;
            case R.id.nav_setting:
                Bundle settingBundle = new Bundle();
                locations = new ArrayList<>();
                locations = getIntent().getStringArrayListExtra(EXTRA_SETTINGS_KEY);
                Log.d(TAG, "onNavigationItemSelected: nav_setting");
                settingBundle.putStringArrayList(SettingsFragment.EXTRA_SETTINGS_KEY, locations);
                settingsFragment.setArguments(settingBundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).commit();
                break;
        }
        return true;
    }

    // 글 작성 choice
    public void showDialogWrite() {
        CustomDialog dialog = new CustomDialog(MainActivity.this, "중고 판매글을 작성하시겠어요?", "네", "아니오");
        dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
            @Override
            public void onPositiveClicked() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
                postIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(postIntent);
                finish();
            }

            @Override
            public void onNegativeClicked() {
                dialog.dismiss();
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        handler.onBackPressed();
    }
}