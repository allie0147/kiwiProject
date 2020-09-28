package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.custom.CustomTextWatcher;
import com.android.projectchatting.custom.OnSingleClickListener;
import com.android.projectchatting.databinding.ActivityEditProfileBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    public static final String USER_URI = "user_profile_image";
    private static final String TAG = "EDIT_PROFILE_ACTIVITY";
    private static final int CHOOSE_PIC_FROM_ALBUM = 101;
    private ActivityEditProfileBinding binding;
    private FirebaseUser mUser;
    private String userProfileImage;
    private DatabaseReference mUserDatabase;
    private StorageReference mUserStorage;
    private ArrayList<String> locations;
    private Uri profileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        userProfileImage = getIntent().getStringExtra(USER_URI);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid());
        mUserStorage = FirebaseStorage.getInstance().getReference().child("userImages");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userProfileImage.equals("")) {
            Glide.with(this).load(R.drawable.round_account_circle_24).into(binding.editProfileImage);
        } else {
            Glide.with(this).load(Uri.parse(userProfileImage)).into(binding.editProfileImage);
        }
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.editProfileNickname.setText(snapshot.child("nickname").getValue().toString());
                locations = new ArrayList<>();
                locations.add(snapshot.child("locationId").getValue().toString());
                locations.add(snapshot.child("location").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        CustomTextWatcher.nameTextWatcher(binding.editProfileNicknameInput);
        binding.editProfileClose.setOnClickListener(v -> onBackPressed());
        // submit
        binding.editProfileSubmit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Map<String, Object> user = new HashMap<>();
                user.put("nickname", binding.editProfileNickname.getText().toString());
                mUserDatabase.updateChildren(user);
                mUserStorage.child(mUser.getUid()).putFile(profileUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            CustomDialog dialog = new CustomDialog(EditProfileActivity.this,
                                    "정보가 수정되었어요!",
                                    "확인");
                            dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                                @Override
                                public void onPositiveClicked() {
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                    onBackPressed();
                                }

                                @Override
                                public void onNegativeClicked() {

                                }
                            });
                        });
            }
        });
        // 이미지 가져오기
        binding.editProfileImage.setOnClickListener(v -> getPermission());
        binding.editProfileImageMini.setOnClickListener(v -> getPermission());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_SETTINGS_KEY, locations);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PIC_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(data.getDataString()).into(binding.editProfileImage);
            profileUri = data.getData();
        }
    }

    void getPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Log.d(TAG, "onPermissionGranted: ");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, CHOOSE_PIC_FROM_ALBUM); // 101
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Log.d(TAG, "onPermissionDenied: " + deniedPermissions.toString());
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진을 업로드 하기 위해서는 갤러리 접근 권한이 필요해요.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }
}