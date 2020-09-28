package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.custom.CustomTextWatcher;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

public class OtpActivity extends AppCompatActivity {
    private static final String TAG = "OTP_ACTIVITY";
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private TextInputLayout inputLayout;
    private EditText authNum;
    private ProgressBar progressBar;
    private String mAuthVerificationId;
    private String phone;
    private Button button;
    private CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        dialog = new CustomDialog(OtpActivity.this);
        mAuth = FirebaseAuth.getInstance();
        inputLayout = findViewById(R.id.otp_verification_code);
        progressBar = findViewById(R.id.otp_progress);
        authNum = inputLayout.getEditText();
        // 확인 버튼
        button = findViewById(R.id.otp_submit);
        Intent intent = getIntent();
        // find user
        if (intent.hasExtra("from")) {
            if (intent.getStringExtra("from").equals("findEmail")) {
                getCode();
                button.setOnClickListener(v -> showUserInfo("email"));
            } else if (intent.getStringExtra("from").equals("findPwd")) {
                getCode();
                button.setOnClickListener(v -> showUserInfo("pwd"));
            }
            // 일반 인증일 경우,
        } else {
            getCode();
            button.setOnClickListener(v -> authUser());
        }
    }

    // find user
    private void showUserInfo(String flag) {
        //flag==email||pwd
        String otp = authNum.getText().toString();
        if (otp.isEmpty()) {
            inputLayout.setError("인증번호를 입력하세요.");
        } else {
            inputLayout.setError(null);
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "findUser_otp: " + otp);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
            Log.d(TAG, "findUser_cred: " + credential);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
            databaseReference.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        String value = snapshot.getValue().toString();
                        String uid = value.substring(1, snapshot.getValue().toString().indexOf('='));
                        Log.d(TAG, "onDataChange: " + value);
                        //이메일 인증일 경우
                        if (flag.equals("email")) {
                            progressBar.setVisibility(View.INVISIBLE);
                            snapshot.getRef().child(uid).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                //이메일 찾기!!!!!!
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.d(TAG, "onDataChange: " + snapshot.getValue());
                                    // {key: email, value: 이메일}
                                    dialog.makeSimpleDialog("키위마켓에 가입하신 이메일은\n" + snapshot.getValue() + "입니다", "로그인 하러가기");
                                    dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                                        @Override
                                        public void onPositiveClicked() {
                                            if (dialog.isShowing()) {
                                                dialog.dismiss();
                                            }
                                            startActivity(new Intent(OtpActivity.this, LoginActivity.class));
                                            finish();
                                        }

                                        @Override
                                        public void onNegativeClicked() {

                                        }
                                    });
                                    dialog.show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    dialog.makeSimpleDialog("오류 발생! 재시도 하세요", "확인");
                                    dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                                        @Override
                                        public void onPositiveClicked() {
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onNegativeClicked() {

                                        }
                                    });
                                    dialog.show();
                                }
                            });
                            //비밀번호 재설정인 경우: 보류
                        } else {
                            //패스워드 재설정 fragment로 intent 넘길때, uid를 같이 전달
//                            Bundle bundle = new Bundle();
//                            bundle.putString("uid", uid);
//                            PasswordFragment passwordFragment = new PasswordFragment();
//                            passwordFragment.setArguments(bundle);
//                            getSupportFragmentManager().beginTransaction().replace(R.id.container, passwordFragment).commit();
                            convertText();
                            CustomTextWatcher.pwdTextWatcher(inputLayout);
                            button.setText("변경하기");
                            progressBar.setVisibility(View.INVISIBLE);
                            button.setOnClickListener(v -> {
                                progressBar.setVisibility(View.VISIBLE);
                                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
                                Log.d(TAG, "findUser_cred: " + credential);
//                                if (mAuth.signInWithCredential(credential).isSuccessful()) {
//                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                                    Log.d(TAG, "user: " + user); //null
//                                }
//                                user.updatePassword(authNum.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        progressBar.setVisibility(View.INVISIBLE);
//                                        Log.d(TAG, "onComplete: update pwd" + task);
//                                        AlertDialog.Builder builder = new AlertDialog.Builder(OtpActivity.this);
//                                        builder.setMessage("비밀번호 변경 성공!");
//                                        builder.setPositiveButton("로그인 하러가기", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialogInterface, int i) {
//                                                startActivity(new Intent(OtpActivity.this, LoginActivity.class));
//                                                finish();
//                                            }
//                                        });
//                                        AlertDialog dialog = builder.create();
//                                        dialog.show();
//                                    }
//                                });
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dialog.makeSimpleDialog("오류 발생! 재시도 하세요", "확인");
                    dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                        @Override
                        public void onPositiveClicked() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegativeClicked() {

                        }
                    });
                    dialog.show();
                }
            });
        }
    }

    // 일반 인증일 경우,
    private void authUser() {
        String otp = authNum.getText().toString();
        if (otp.isEmpty()) {
            inputLayout.setError("인증번호를 입력하세요.");
        } else {
            inputLayout.setError(null);
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "otp: " + otp);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
            Log.d(TAG, "cred: " + credential);
            mCurrentUser = mAuth.getCurrentUser();
            mCurrentUser.updatePhoneNumber(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put(mCurrentUser.getUid() + "/phone", phone);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                    databaseReference.updateChildren(userUpdate);
                    progressBar.setVisibility(View.INVISIBLE);
                    dialog.makeSimpleDialog("인증 성공!", "동네인증 하러가기");
                    dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                        @Override
                        public void onPositiveClicked() {
                            if (dialog.isShowing()) dialog.dismiss();
                            sendUser();
                        }

                        @Override
                        public void onNegativeClicked() {

                        }
                    });
                    dialog.show();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    dialog.makeSimpleDialog("인증 실패! 재인증 하세요", "확인");
                    dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                        @Override
                        public void onPositiveClicked() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegativeClicked() {

                        }
                    });
                    dialog.show();
                }
            });
        }
    }

    private void sendUser() {
        Intent intent = new Intent(OtpActivity.this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void getCode() {
        mAuthVerificationId = getIntent().getStringExtra("AuthCredentials");
        phone = getIntent().getStringExtra("phone");
        Log.d(TAG, "onCodeSent: " + mAuthVerificationId);
    }

    private void convertText() {
        TextView title = findViewById(R.id.otp_title);
        title.setText("변경할 비밀번호를 입력하세요");
        authNum.setText("");
        inputLayout.setHint("비밀번호 입력");
        inputLayout.setCounterEnabled(true);
        inputLayout.setCounterMaxLength(20);
        authNum.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputLayout.setCounterTextAppearance(R.integer.fui_min_password_length);
    }


//    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(OtpActivity.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            mCurrentUser.updatePhoneNumber(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    CustomSnackbar.showInfo(findViewById(R.id.otp_layout), "인증 성공!", "확인");
//                                    new Handler().postDelayed(() -> sendUserHome(), 1000);
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    CustomSnackbar.showError(findViewById(R.id.otp_layout), "인증 실패. 재인증하세요.", "확인");
//                                }
//                            });
//                        }
//                    }
//                });
//
//    }
}