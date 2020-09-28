package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.projectchatting.R;
import com.android.projectchatting.model.UserModel;
import com.android.projectchatting.custom.CustomSnackbar;
import com.android.projectchatting.custom.CustomTextWatcher;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SIGN_UP_ACTIVITY";
    private static final int CHOOSE_PIC_FROM_ALBUM = 101;
    private ImageView profile;
    private Uri imgUri;
    private EditText email;
    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        profile = findViewById(R.id.signup_profile);
        profile.setOnClickListener(v -> getPermission());
        TextInputLayout emailLayout = findViewById(R.id.signup_layout_email);
        TextInputLayout pwdLayout = findViewById(R.id.signup_layout_pwd);
        TextInputLayout nameLayout = findViewById(R.id.signup_layout_name);
        email = emailLayout.getEditText();
        CustomTextWatcher.emailTextWatcher(emailLayout);
        password = pwdLayout.getEditText();
        CustomTextWatcher.pwdTextWatcher(pwdLayout);
        username = nameLayout.getEditText();
        CustomTextWatcher.nameTextWatcher(nameLayout);
        //다음 버튼 클릭시 -> 로그인으로
        findViewById(R.id.sign_up_finish).setOnClickListener(v -> {
            String emailRegex = "^[a-zA-Z0-9]+[@][a-zA-Z0-9]+[\\\\.][a-zA-Z0-9]+$";
            String pwdRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,20}$";
            String nameRegex = "^[a-zA-zㄱ-ㅎ가-힣0-9]{2,11}$";
            boolean matchEmail = Pattern.matches(emailRegex, email.getText().toString());
            boolean matchPwd = Pattern.matches(pwdRegex, password.getText().toString());
            boolean matchName = Pattern.matches(nameRegex, username.getText().toString());
            Log.d(TAG, "name: " + matchName);
            Log.d(TAG, "name: " + username.getText().toString());
            if (!matchEmail || !matchName || !matchPwd) { // 정규식에 맞지 않을 경우
                return;
            } else {
                UserModel user = setUser(email.getText().toString(), username.getText().toString());
                Log.d(TAG, "onCreate: " + user.toString());
                signupUser(user);
            }
        });
    }

    private void signupUser(UserModel user) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(user.getEmail(), password.getText().toString())
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // email, nickname 저장
                        Log.d(TAG, "onComplete: " + FirebaseDatabase.getInstance().getReference().child("users"));
                        String uid = task.getResult().getUser().getUid();
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(user);
                        //profile img가 있을 때,
                        if (imgUri != null) {
                            // image file upload to firebase storage
                            FirebaseStorage.getInstance().getReference()
                                    .child("userImages")
                                    .child(uid)
                                    .putFile(Uri.parse(imgUri.toString()))
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // user profile 저장
                                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(username.getText().toString())
                                                .setPhotoUri(Uri.parse(imgUri.toString()))
                                                .build();
                                        updateUser(userProfileChangeRequest, auth);
                                    }).addOnFailureListener(exception -> {
                                // Handle unsuccessful uploads
                                CustomSnackbar.showError(findViewById(R.id.sign_up_layout), "프로필 이미지 업로드 실패!", "다시하기");
                            });
                        } else {
                            //profile img가 없을 때,
                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username.getText().toString()).build();
                            updateUser(userProfileChangeRequest, auth);
                        }
                    }
                });
    }

    private void updateUser(UserProfileChangeRequest userProfileChangeRequest, FirebaseAuth auth) {
        auth.getCurrentUser().updateProfile(userProfileChangeRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendVerificationEmail();
            } else {
                CustomSnackbar.showError(findViewById(R.id.sign_up_layout), "회원가입에 실패했습니다. 재시도 해주세요.", "확인");
            }
        });
    }

    // 회원가입 완료시, 이메일 발송
    private void sendVerificationEmail() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        Log.d(TAG, "sendVerificationEmail: " + user);
        auth.useAppLanguage();
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 2초 후, 로그인 액티비티로 이동
                Log.d(TAG, "sendVerificationEmail: " + auth.getCurrentUser());
                CustomSnackbar.showInfo(findViewById(R.id.sign_up_layout), "소중한 고객님의 정보를 위해 이메일 인증을 해주세요!", "확인");
                auth.signOut();
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                }, 2000);
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "invalid email");
            CustomSnackbar.showError(findViewById(R.id.sign_up_layout), "유효하지 않은 이메일입니다. 재시도 하세요.", "확인");
        });
    }

    private UserModel setUser(String email, String nickname) {
        UserModel user = new UserModel();
        user.setEmail(email);
        user.setNickname(nickname);
        return user;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PIC_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(data.getDataString()).into(profile);
        }
    }

    // permission
    public void getPermission() {
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


