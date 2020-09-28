package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;

import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.projectchatting.R;
import com.android.projectchatting.model.Category;
import com.android.projectchatting.model.Posts;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.custom.CustomProgressDialog;
import com.android.projectchatting.custom.OnSingleClickListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    private static final int CHOOSE_PIC_FROM_ALBUM = 101;
    private static final String TAG = "POST_ACTIVITY";
    private TextView close, submit, category, photoBtn; // clickable
    private EditText title, price, content;
    private Posts post; //게시글
    private String locationId, location, dong, categoryId, categoryStr; //게시글 속성
    private String nickname;
    private List<Uri> imgUriList; // 이미지 uri
    private ArrayList<String> categoryList; // 카테고리 정보
    private FirebaseUser mUser;
    private FirebaseDatabase database;
    private String errorMsg; // 빈 칸 검사
    private CustomProgressDialog mProgressDialog; // 프로그래스 바
    private CustomDialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        // 프로그래스 바 생성
        mProgressDialog = new CustomProgressDialog(PostActivity.this);
        dialog = new CustomDialog(PostActivity.this);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        imgUriList = new ArrayList<>();
        // 앱 바
        Toolbar toolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(toolbar);
        // 1. 종료 버튼
        close = findViewById(R.id.write_close);
        close.setOnClickListener(v -> showDialog());
        getUser(mUser.getUid()); // locationId, location, dong, nickname 가져오기
        // 2. 글 저장 버튼
        submit = findViewById(R.id.post_submit);
        // 카테고리 버튼
        category = findViewById(R.id.post_category);
        // 사진 등록 버튼
        photoBtn = findViewById(R.id.post_photo);
        // 글 내용
        title = findViewById(R.id.post_title);
        content = findViewById(R.id.post_content);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 글 저장
        submit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                errorMsg = PostActivity.this.setErrorMsg();
                if (!errorMsg.equals("")) {
                    dialog.makeSimpleDialog(errorMsg, "확인");
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
                } else {
                    mProgressDialog.setTitleView("글을 저장하는 중이에요...");
                    mProgressDialog.show();
                    // 개별키 자동생성
                    Log.d(TAG, "updateUI: " + FirebaseDatabase.getInstance().getReference().child("/posts/" + locationId).push().getKey());
                    submitPost();
                }
            }
        });
        // 카테고리 클릭시, dialog selection
        category.setOnClickListener(v -> {
            Category c = new Category();
            categoryList = c.getCategoryList();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setNegativeButton("취소", (dialogInterface, i) -> dialogInterface.cancel());
            builder.setTitle("카테고리를 선택하세요.").setItems(R.array.category, (dialogInterface, i) -> {
                Log.d(TAG, "onClick: " + i + "선택");
                Log.d(TAG, "onClick: " + categoryList.get(i));
                // 카테고리 선택시, 선택 카테고리 표시
                category.setText(categoryList.get(i));
                // post 객체에 카테고리명, id 값
                categoryId = (String.valueOf(i + 1)); // categoryId
                categoryStr = categoryList.get(i); // category
            }).create().show();
        });
        // 사진 등록 5장 최대
        photoBtn.setOnClickListener(v -> {
            if (imgUriList.size() == 0) {
                // 권한 여부
                getPermission();
            } else {
                if (imgUriList.size() > 4) {
                    dialog.makeSimpleDialog("사진은 5개까지만 업로드 가능해요", "확인");
                    dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                        @Override
                        public void onPositiveClicked() {
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegativeClicked() {

                        }
                    });
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, CHOOSE_PIC_FROM_ALBUM); // 101
                }
            }
        });
        price = findViewById(R.id.post_price);
        // 가격 99,999,999까지 가능(자릿수)
        price.addTextChangedListener(new TextWatcher() {
            DecimalFormat myFormatter = new DecimalFormat("###,###");
            String result = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence.toString()) && !charSequence.toString().equals(result)) {
                    result = myFormatter.format(Double.parseDouble(charSequence.toString().replaceAll(",", "")));
                    price.setText(result);
                    price.setSelection(result.length());
                    Log.d(TAG, "onTextChanged: " + result);
                    Log.d(TAG, "onTextChanged: " + result.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    // images upload
    private void uploadImgs(List<Uri> imgUriList, String key) {
        for (int i = 0; i < imgUriList.size(); i++) {
            FirebaseStorage.getInstance().getReference("/postImages/" + locationId + "/" + key + "/" + (i + 1))
                    .putFile(Uri.parse(String.valueOf(imgUriList.get(i))))
                    .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "success")).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
                }
            });
        }
        goMain();
    }

    // user 정보 가져오기
    private void getUser(String uId) {
        DatabaseReference db = database.getReference().child("users").child(uId);
        db.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: " + snapshot.getValue());
                        Map<String, Object> user = (HashMap<String, Object>) snapshot.getValue();
                        Log.d(TAG, "user?: " + user);
                        locationId = (String) user.get("locationId");
                        location = (String) user.get("location");
                        dong = (String) user.get("dong");
                        nickname = (String) user.get("nickname");
                        Log.d(TAG, "locationid: " + user.get("locationId"));
                        Log.d(TAG, "dong: " + user.get("dong"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: " + error);
                    }
                }
        );
    }

    // 이미지 뷰 추가
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PIC_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            ImageView imageView = new ImageView(this);
            // 이미지 뷰 크기 제한
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 300);
            layoutParams.setMargins(5, 0, 5, 0);
            imageView.setLayoutParams(layoutParams);
            imageView.setAdjustViewBounds(true);
            Glide.with(getApplicationContext()).load(getFilePath(data)).override(300, 300).into(imageView);
            // 레이아웃에 이미지 뷰 추가
            LinearLayout layout = findViewById(R.id.post_image_scroll);
            layout.addView(imageView);
            imgUriList.add(data.getData()); //img 원본 stream
            Log.d(TAG, "onActivityResult: " + imgUriList.size());
            photoBtn.setText("사진 등록\n" + imgUriList.size() + "/5");
        }
    }

    // db저장
    private void submitPost() {
        String key = database.getReference().child("/posts/" + locationId).push().getKey();
        // post 객체에 값 셋팅
        Map<String, Object> updates = new HashMap<>();
        post = new Posts(mUser.getUid(), nickname, dong, categoryId, categoryStr,
                title.getText().toString(), content.getText().toString(), price.getText().toString(),
                System.currentTimeMillis());
        Map<String, Object> postsValues = post.toMap();
        Map<String, Object> userPostsValues = post.toMap();
        // posts schema
        updates.put("/posts/" + locationId + "/" + key, postsValues);
        if (userPostsValues.remove("uId") != null) {
            userPostsValues.put("locationId", locationId);
            // users-post schema
            updates.put("/user-posts/" + mUser.getUid() + "/" + key, userPostsValues);
        }
        database.getReference().updateChildren(updates, (error, ref) -> {
            // img file 저장
            if (imgUriList.size() == 1) {
                FirebaseStorage.getInstance().getReference("/postImages/" + locationId + "/" + key + "/1")
                        .putFile(Uri.parse(String.valueOf(imgUriList.get(0))))
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d(TAG, "img file uploaded succeeded");
                            PostActivity.this.goMain();
                        });
            } else {
                PostActivity.this.uploadImgs(imgUriList, key);
            }
        });
    }

    // 파일 경로
    public String getFilePath(Intent data) {
        final Uri selectImageUri = data.getData();
        final String[] filePathColumn = {MediaStore.Images.Media.DATA};
        final Cursor imageCursor = this.getContentResolver().query(selectImageUri, filePathColumn, null, null, null);
        imageCursor.moveToFirst();
        final int columnIndex = imageCursor.getColumnIndex(filePathColumn[0]);
        final String imagePath = imageCursor.getString(columnIndex);
        imageCursor.close();
        return imagePath;
    }

    // 유효성 체크
    private String setErrorMsg() {
        errorMsg = "";
        if (title.getText().length() == 0 || title.getText().equals("")) {
            errorMsg += "- 글 제목은 필수 입력 항목입니다\n";
        }
        if (content.getText().length() == 0 || content.getText().equals("")) {
            errorMsg += "- 내용은 필수 입력 항목입니다\n";
        }
        if (category.getText().equals("카테고리 선택")) {
            errorMsg += "- 카테고리는 필수 입력 항목입니다\n";
        }
        if (imgUriList.size() == 0) {
            errorMsg += "- 물품 사진 한 장은 필수 입력 항목입니다";
        }
        return errorMsg;
    }

    // 메인으로
    private void goMain() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
        ArrayList<String> locationList = new ArrayList<>();
        locationList.add(locationId);
        locationList.add(location);
        Intent intent = new Intent(PostActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.LOCATION_KEY, locationList);
        startActivity(intent);
        finish();
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

    // 백 버튼 클릭시, dialog
    @Override
    public void onBackPressed() {
        showDialog();
    }

    // 돌아가기 or 남아있기
    public void showDialog() {
        dialog.makeButtonsDialog("작성하신 글은 저장되지 않아요.\n글쓰기를 종료 하시겠어요?", "네", "아니요");
        dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
            @Override
            public void onPositiveClicked() {
                if (dialog.isShowing()) dialog.dismiss();
                goMain();
            }

            @Override
            public void onNegativeClicked() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}