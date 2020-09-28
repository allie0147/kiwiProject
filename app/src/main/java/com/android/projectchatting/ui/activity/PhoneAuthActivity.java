package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.custom.CustomSnackbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {
    private static final String TAG = "PHONE_AUTH_ACTIVITY";

    private TextInputLayout inputLayout;
    private EditText phoneNum;
    private Button button;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String phone;
    private CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        dialog = new CustomDialog(PhoneAuthActivity.this);
        inputLayout = findViewById(R.id.phone_auth_input_number);
        phoneNum = inputLayout.getEditText();
        progressBar = findViewById(R.id.phone_auth_progress);
        button = findViewById(R.id.phone_auth_button);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        button.setOnClickListener(v -> {
            phone = phoneNum.getText().toString();
            phone = "+82" + phone.substring(1);
            Log.d(TAG, "onCreate: " + phone);
            if (phone.isEmpty()) {
                inputLayout.setError("핸드폰 번호를 입력하세요.");
            } else {
                inputLayout.setError(null);
                progressBar.setVisibility(View.VISIBLE);
                String finalPhone = phone;
                PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, PhoneAuthActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                progressBar.setVisibility(View.INVISIBLE);
                                signInWithAuthCredential(phoneAuthCredential, finalPhone);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                dialog.makeSimpleDialog("인증 실패! 재시도 하세요", "확인");
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

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                Log.d(TAG, "onCodeSent: " + verificationId);
                                Log.d(TAG, "onCodeSent: " + token);
                                progressBar.setVisibility(View.INVISIBLE);
                                Intent otpIntent = new Intent(PhoneAuthActivity.this, OtpActivity.class);
                                otpIntent.putExtra("AuthCredentials", verificationId);
                                otpIntent.putExtra("phone", finalPhone);
                                startActivity(otpIntent);
                                finish();
                            }
                        });
            }
        });
    }

    private void signInWithAuthCredential(PhoneAuthCredential credential, String phone) {
        mCurrentUser.updatePhoneNumber(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put(mCurrentUser.getUid() + "/phone", phone);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                databaseReference.updateChildren(userUpdate);
                dialog.makeSimpleDialog("인증 성공!", "둘러보기");
                dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                    @Override
                    public void onPositiveClicked() {
                        if (dialog.isShowing()) dialog.dismiss();
                        sendUserHome();
                    }

                    @Override
                    public void onNegativeClicked() {

                    }
                });
                dialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.makeSimpleDialog("인증 실패! 재시도 하세요", "확인");
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
        inputLayout.setError(null);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void sendUserHome() {
        Intent intent = new Intent(PhoneAuthActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}