package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.android.projectchatting.R;
import com.android.projectchatting.model.UserModel;
import com.android.projectchatting.custom.CustomDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LOGIN_ACTIVITY";
    private static final int RC_SIGN_IN = 9001;
    private EditText userId;
    private EditText userPwd;
    //authentication
    private FirebaseAuth mAuth = null;
    // google sign in
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dialog = new CustomDialog(LoginActivity.this);
        userId = findViewById(R.id.login_user_id);
        userPwd = findViewById(R.id.login_user_pwd);
        //login button
        findViewById(R.id.button_login).setOnClickListener(v -> loginEvent());
        // find id or password
        findViewById(R.id.login_find_user).setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, FindUserActivity.class)));
        //sign-up button
        findViewById(R.id.button_sign_up).setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        //google sign-in button
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO);
        // auth google 인증 : 바로 main activity로
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate auth: " + mAuth); //잇음
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // sign-in with google
        signInButton.setOnClickListener(v -> signIn());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.getAccessToken(true).addOnSuccessListener(getTokenResult -> {
            Log.d(TAG, "onSuccess: " + getTokenResult.getToken());
            Log.d(TAG, "onSuccess: " + getTokenResult.getSignInProvider());
        });
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e("loginError", e.toString());
                dialog.makeSimpleDialog("사용자 인증에 실패했습니다. 다시 시도해 주세요", "확인");
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
        }
    }

    //gogole auth
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "uid: " + user.getUid());
                        Log.d(TAG, "email: " + user.getEmail());
                        Log.d(TAG, "email_verification: " + user.isEmailVerified());
                        Log.d(TAG, "phone: " + user.getPhoneNumber());
                        Log.d(TAG, "username: " + user.getDisplayName());
                        Log.d(TAG, "profileURL: " + user.getPhotoUrl());
                        checkGoogleAuth(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        dialog.makeSimpleDialog("사용자 인증에 실패했습니다. 다시 시도해 주세요", "확인");
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
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) { //update ui code here
        Log.d(TAG, "updateUI: ");
        if (user != null) { // 처음 접근 시,
            Log.d(TAG, "uid: " + user.getUid());
            Log.d(TAG, "email: " + user.getEmail());
            Log.d(TAG, "email_verification: " + user.isEmailVerified());
            Log.d(TAG, "phone: " + user.getPhoneNumber());
            Log.d(TAG, "username: " + user.getDisplayName());
            Log.d(TAG, "profileURL: " + user.getPhotoUrl());
            //이메일 체크
            if (!user.isEmailVerified()) {
                dialog.makeSimpleDialog("이메일 인증 후 로그인 하세요", "확인");
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
                //휴대폰 체크: verified로 체크 해야함...
            } else if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty() || user.getPhoneNumber().equals("")) {
                Intent intent = new Intent(getApplicationContext(), PhoneAuthActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                // 위치 데이터 유무 체크
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: " + snapshot.toString());
                        Log.d(TAG, "onDataChange: " + snapshot.hasChild("locationId"));
                        if (snapshot.hasChild("locationId")) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: cannot reach database: " + error);
                    }
                });
            }
        }
    }

    // 기존 또는 최초 로그인
    void checkGoogleAuth(FirebaseUser user) {
        Log.d(TAG, "checkGoogleAuth: ");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        Log.d(TAG, "checkGoogleAuth: " + ref);
        ref.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("email").equals(user.getEmail())) {
                        Log.d(TAG, "onDataChange: updateUI");
                        updateUI(user);
                    } else {
                        loginWithGoogle(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void loginWithGoogle(FirebaseUser user) {
        UserModel userModel = new UserModel();
        userModel.setNickname(user.getDisplayName());
        userModel.setEmail(user.getEmail());
        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).setValue(userModel);
    }

    //login with email auth
    void loginEvent() {
        mAuth.signInWithEmailAndPassword(userId.getText().toString(), userPwd.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUI(mAuth.getCurrentUser());
                    } else {
                        dialog.makeSimpleDialog("사용자 인증에 실패했습니다. 다시 시도해 주세요", "확인");
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