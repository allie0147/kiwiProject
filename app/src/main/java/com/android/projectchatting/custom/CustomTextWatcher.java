package com.android.projectchatting.custom;

import android.text.Editable;

import com.android.projectchatting.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class CustomTextWatcher {
    private static final String emailRegex = "^[a-zA-Z0-9]+[@][a-zA-Z0-9]+[\\\\.][a-zA-Z0-9]+$";
    private static final String pwdRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,20}$";
    private static final String nameRegex = "^[a-zA-zㄱ-ㅎ가-힣0-9]{2,11}$";

    public static void emailTextWatcher(TextInputLayout emailLayout) {
        emailLayout.getEditText().addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (Pattern.matches(emailRegex, s)) {
                    emailLayout.setError(null);
                    emailLayout.setErrorIconDrawable(null);
                } else {
                    emailLayout.setErrorIconDrawable(R.drawable.round_error_24);
                    emailLayout.setError("올바른 이메일주소가 아닙니다");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public static void pwdTextWatcher(TextInputLayout layout) {
        layout.getEditText().addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.length() == 0 || s.length() < 7 || !Pattern.matches(pwdRegex, s) || s.equals(" ")) {
                    layout.setErrorIconDrawable(R.drawable.round_error_24);
                    layout.setError("영문 대문자, 소문자, 숫자, 특수문자 포함 8자리 이상 20자리 이하의 비밀번호를 입력하세요");
                } else {
                    layout.setErrorIconDrawable(null);
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public static void nameTextWatcher(TextInputLayout layout) {
        layout.getEditText().addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.length() == 0 || s.length() == 1 || !Pattern.matches(nameRegex, s) || s.equals(" ")) {
                    layout.setErrorIconDrawable(R.drawable.round_error_24);
                    layout.setError("문자, 숫자를 포함 2자리 이상 11자리 이하의 닉네임을 입력하세요");
                } else {
                    layout.setErrorIconDrawable(null);
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
