package com.android.projectchatting.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.custom.CustomSnackbar;
import com.android.projectchatting.databinding.FragmentSettingsBinding;
import com.android.projectchatting.model.UserModel;
import com.android.projectchatting.ui.activity.EditProfileActivity;
import com.android.projectchatting.ui.activity.LoginActivity;
import com.android.projectchatting.ui.activity.MapsActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SettingsFragment extends Fragment {
    public static final String EXTRA_SETTINGS_KEY = "settings";
    private static final String TAG = "SETTINGS_FRAGMENT";
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private View rootView;
    private FragmentSettingsBinding binding;
    private DatabaseReference userReference;
    private StorageReference userStorage;
    private String userProfileImage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        rootView = binding.getRoot();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());
        userStorage = FirebaseStorage.getInstance().getReference()
                .child("userImages")
                .child(mCurrentUser.getUid());
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // menu for logout
        binding.settingsToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_settings:
                    CustomDialog logoutDialog = new CustomDialog(rootView.getContext(), "로그아웃을 할까요?", "네", "아니오");
                    logoutDialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                        @Override
                        public void onPositiveClicked() {
                            if (logoutDialog.isShowing()) {
                                logoutDialog.dismiss();
                            }
                            userSignOut(mAuth);
                        }

                        @Override
                        public void onNegativeClicked() {
                            logoutDialog.dismiss();
                        }
                    });
                    logoutDialog.show();
                    break;
                default:
                    break;
            }
            return true;
        });
        // map
        binding.settingsMap.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
        });
        // user profile info
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.settingsProfile.authorAddress.setText(snapshot.child("location").getValue() + " " + snapshot.child("dong").getValue());
                binding.settingsProfile.authorName.setText(snapshot.child("nickname").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userStorage.getDownloadUrl().addOnSuccessListener(uri -> {
            userProfileImage = String.valueOf(uri);
            Glide.with(rootView.getContext())
                    .load(uri)
                    .circleCrop()
                    .into(binding.settingsProfile.authorImage);
        }).addOnFailureListener(e -> {
            Log.d(TAG, "onFailure: no user profile image");
            userProfileImage = "";
            binding.settingsProfile.authorImage.setBorderColor(getResources().getColor(android.R.color.transparent));
            Glide.with(rootView.getContext()).load(R.drawable.round_account_circle_24).into(binding.settingsProfile.authorImage);
        });
        binding.settingsProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(rootView.getContext(), EditProfileActivity.class);
            intent.putExtra(EditProfileActivity.USER_URI, userProfileImage);
            startActivity(intent);
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        binding = null;
    }

    private void userSignOut(FirebaseAuth auth) {
        auth.signOut();
        mCurrentUser = null;
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        getActivity().finish();
    }
}