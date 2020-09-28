package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.CustomDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class FindUserActivity extends AppCompatActivity {

    private static final String TAG = "FIND_USER_ACTIVITY";
    private TextInputLayout findEmail, findPwd;
    private EditText findEmailText, findPwdText;
    private CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);
        findEmail = findViewById(R.id.find_user_find_email);
        findEmailText = findEmail.getEditText();
        findPwd = findViewById(R.id.find_user_find_password);
        findPwdText = findPwd.getEditText();
        dialog = new CustomDialog(this);
        // find email button
        findViewById(R.id.find_user_find_email_btn).setOnClickListener(v -> findUser(findEmail, findEmailText, "findEmail"));
        // find password button
        findViewById(R.id.find_user_find_password_btn).setOnClickListener(v -> findUser(findPwd, findPwdText, "findPwd"));
    }

    // user 계정이 있는지 확인
    public void findUser(TextInputLayout layout, EditText findText, String from) {
        String phone = findText.getText().toString();
        if (phone.isEmpty()) {
            layout.setError("핸드폰번호를 입력하세요");
        } else {
            layout.setError(null);
            phone = "+82" + phone.substring(1);
            // db에서 where phone=? select
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
            Log.d(TAG, "onCreate: " + databaseReference);
            Log.d(TAG, "onCreate: " + databaseReference.orderByChild("phone").equalTo(phone).toString());
            databaseReference.orderByChild("phone").equalTo(phone)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                //{ key = users, value = {jo2n4CWMCgTp3c5wpv14ro8JhOp1={phone=+821095226810}} }
//                            Log.d(TAG, "onDataChange: " + snapshot.getValue().toString().substring(1, snapshot.getValue().toString().indexOf('=')));
                                String value = snapshot.getValue().toString();
                                int index = snapshot.getValue().toString().indexOf("phone");
                                String phone = value.substring(index + 6, index + 19);
                                Log.d(TAG, "onDataChange: " + phone);
                                sendMsg(phone, from);
                            } else {
                                dialog.makeButtonsDialog("존재하지 않는 계정이에요", "회원가입 하러가기", "닫기");
                                dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                                    @Override
                                    public void onPositiveClicked() {
                                        if (dialog.isShowing()) dialog.dismiss();
                                        startActivity(new Intent(FindUserActivity.this, SignUpActivity.class));
                                        finish();
                                    }

                                    @Override
                                    public void onNegativeClicked() {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
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

    //OTP Activity로 전달
    public void sendMsg(String phone, String from) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, FindUserActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                        signInWithAuthCredential(phoneAuthCredential, phone);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        dialog.makeSimpleDialog("인증 번호가 달라요! 재시도 하세요", "확인");
                        dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                            @Override
                            public void onPositiveClicked() {
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegativeClicked() {
                            }
                        });
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent: " + verificationId);
                        Log.d(TAG, "onCodeSent: " + token);
                        Intent otpIntent = new Intent(FindUserActivity.this, OtpActivity.class);
                        otpIntent.putExtra("AuthCredentials", verificationId);
                        otpIntent.putExtra("phone", phone);
                        otpIntent.putExtra("from", from);
                        startActivity(otpIntent);
                        finish();
                    }
                });

    }
}
//      mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "email sent");
//                            CustomSnackbar.actionSnackbar(findViewById(R.id.find_user_layout), "이메일이 전송 되었습니다. 확인 해 주세요.", "로그인하러 가기", v -> {
//                                startActivity(new Intent(FindUserActivity.this, LoginActivity.class));
//                                finish();
//                            });
//                        } else {
//                            CustomSnackbar.showError(findViewById(R.id.find_user_layout), "이메일 전송 실패. 재시도 해주세요.", "닫기");
//                        }
//                    }
//                });

//    String email = findPwdText.getText().toString();
//            if (email.isEmpty()) {
//        findPwd.setError("이메일을 입력하세요");
//    } else {
//        findPwd.setError(null);
//        // db에서 where email=? select
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
//        databaseReference.orderByChild("email").equalTo(email).
//                addListenerForSingleValueEvent(new ValueEventListener() {
//                    @RequiresApi(api = Build.VERSION_CODES.N)
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        Log.d(TAG, "onDataChange: " + snapshot.getValue());
//                        String value = snapshot.getValue().toString();
////                                String uid = snapshot.getValue().toString().substring(1, snapshot.getValue().toString().indexOf('='));
////                                Log.d(TAG, "onDataChange: " + snapshot.getValue().toString().indexOf("phone"));
//                        int index = snapshot.getValue().toString().indexOf("phone");
//                        String phone = value.substring(index + 6, index + 19);
//                        Log.d(TAG, "onDataChange: " + phone);
//                        Intent intent = new Intent(FindUserActivity.this, PhoneAuthActivity.class);
//                        intent.putExtra("findPwd", phone);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                        startActivity(intent);
//                        finish();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        CustomSnackbar.actionSnackbar(findViewById(R.id.find_user_layout), "존재하지 않는 계정입니다.", "회원가입 하러가기", v -> {
//                            startActivity(new Intent(FindUserActivity.this, SignUpActivity.class));
//                            finish();
//                        });
//                    }
//                });
//    }
//});

//        String uid = snapshot.getValue().toString().substring(1, snapshot.getValue().toString().indexOf('='));
//        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Log.d(TAG, "onDataChange2: " + snapshot.getValue());
//                Log.d(TAG, "onDataChange: " + snapshot.child("email").getValue());
//                AlertDialog.Builder builder = new AlertDialog.Builder(FindUserActivity.this);
//                builder.setMessage("이메일은\n" + snapshot.child("email").getValue() + "입니다.");
//                builder.setPositiveButton("로그인 하러가기", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        startActivity(new Intent(FindUserActivity.this, LoginActivity.class));
//                        finish();
//                    }
//                });
//                builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        builder.create().cancel();
//                    }
//                });
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                CustomSnackbar.actionSnackbar(findViewById(R.id.find_user_layout), "존재하지 않는 계정입니다.", "회원가입 하러가기", v -> {
//                    startActivity(new Intent(FindUserActivity.this, SignUpActivity.class));
//                    finish();
//                });
//            }
//        });
//    } else {
//        CustomSnackbar.actionSnackbar(findViewById(R.id.find_user_layout), "존재하지 않는 계정입니다.", "회원가입 하러가기", v -> {
//            startActivity(new Intent(FindUserActivity.this, SignUpActivity.class));
//            finish();
//        });
//    }
//}
//
//    @Override
//    public void onCancelled(@NonNull DatabaseError error) {
//        CustomSnackbar.showErrorNoActionText(findViewById(R.id.find_user_layout), "오류 발생! 재시도 해주세요.");
//    }