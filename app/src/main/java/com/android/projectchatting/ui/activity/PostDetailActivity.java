package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.databinding.ActivityPostDetailBinding;
import com.android.projectchatting.model.Posts;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class PostDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "POST_DETAIL_ACTIVITY";
    public static final String EXTRA_LOCATION_POST_KEY = "location_post_key";
    private ActivityPostDetailBinding binding;
    // database and storage for post
    private DatabaseReference mPostReference;
    private StorageReference mPostImgReference;
    private StorageReference mPostProfileReference;
    // database to check user's post
    private DatabaseReference mUserPostsReference;
    private ValueEventListener mPostListener;
    private ArrayList<String> mLocationPostKey; //0: locationId, 1: locationStr, 2: postKey
    private FirebaseUser user;
    private MyAdapter adapter;
    private ArrayList<StorageReference> imgList; // 이미지 리소스
    private ArrayList<String> imgListStr; // 이미지 str
    //    private int index = 0; // 이미지 포지션
    private Toolbar toolbar;
    private String postUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mLocationPostKey = getIntent().getStringArrayListExtra(EXTRA_LOCATION_POST_KEY);
        if (mLocationPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }
        Log.d(TAG, "LOCATIONS!!: " + mLocationPostKey.toString());
        user = FirebaseAuth.getInstance().getCurrentUser();
        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mLocationPostKey.get(0)).child(mLocationPostKey.get(2));
        mPostImgReference = FirebaseStorage.getInstance().getReference().child("postImages")
                .child(mLocationPostKey.get(0)).child(mLocationPostKey.get(2));
        // to check current user's posts
        mUserPostsReference = FirebaseDatabase.getInstance().getReference()
                .child("user-posts").child(user.getUid()).child(mLocationPostKey.get(2));
        // Initialize toolbar
        toolbar = binding.detailPinToolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }  //채팅하기
        binding.detailPostGoChat.setOnClickListener(this);
        // check current user's post
        mUserPostsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    binding.detailPostGoChat.setVisibility(View.GONE);
                    binding.detailPinToolbar.getMenu().getItem(1).setVisible(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_detail_menu, menu);
        return true;
    }

    // toolbar 아이템 클릭시 이벤트 > 뒤로가기, 공유하기, 삭제하기
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_share:
                break;
            case R.id.nav_delete:
                deletePost();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletePost() {
        Log.d(TAG, "deletePost: ");
        CustomDialog customDialog = new CustomDialog(PostDetailActivity.this);
        customDialog.makeButtonsDialog("게시글을 삭제하시겠어요?", "확인", "취소");
        customDialog.setDialogListener(new CustomDialog.CustomDialogListener() {
            @Override
            public void onPositiveClicked() {
                if (customDialog.isShowing()) {
                    customDialog.dismiss();
                }
                mPostReference.removeValue();
                mUserPostsReference.removeValue();
                mPostImgReference.listAll().addOnSuccessListener(listResult -> {
                    for (StorageReference reference : listResult.getItems()) {
                        reference.delete();
                    }
                });
                customDialog.makeSimpleDialog("게시글이 삭제되었어요!", "확인");
                customDialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                    @Override
                    public void onPositiveClicked() {
                        if (customDialog.isShowing()) {
                            customDialog.dismiss();
                        }
                        onBackPressed();
                    }

                    @Override
                    public void onNegativeClicked() {
                    }
                });
                customDialog.show();
            }

            @Override
            public void onNegativeClicked() {
                customDialog.dismiss();
            }
        });
        customDialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Posts post = dataSnapshot.getValue(Posts.class);
                    Log.d(TAG, "onDataChange: " + post.toString());
                    // [START_EXCLUDE]
                    binding.detailPostAuthor.authorName.setText(post.getNickname());
                    String address = mLocationPostKey.get(1) + " " + post.getDong();
                    binding.detailPostAuthor.authorAddress.setText(address);
                    binding.detailDynamicTitle.setText(post.getTitle());
                    binding.detailPostBody.bodyTitle.setText(post.getTitle());
                    binding.detailPostBody.bodyCategory.setText(post.getCategory());
                    binding.detailPostBody.bodyContent.setText(post.getContent());
                    binding.detailPostBody.bodyWdate.setReferenceTime(post.getwDate());
                    postUid = post.getuId();
                    mPostProfileReference = FirebaseStorage.getInstance().getReference()
                            .child("userImages")
                            .child(post.getuId());
                    // post profile img
                    Log.d(TAG, "mPostProfileReference: " + mPostProfileReference);
                    mPostProfileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "PROFILE URI: " + uri);
                        Glide.with(getApplicationContext())
                                .load(uri)
                                .circleCrop()
                                .into(binding.detailPostAuthor.authorImage);
                    }).addOnFailureListener(e -> {
                        binding.detailPostAuthor.authorImage.setBorderColor(getResources().getColor(android.R.color.transparent));
                        Log.d(TAG, "onFailure: no such profile img");
                        Glide.with(getApplicationContext()).load(R.drawable.round_account_circle_24).into(binding.detailPostAuthor.authorImage);
                    });
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(PostDetailActivity.this, "사진을 불러오는데 실패했어요.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        }; // [END post_value_event_listener]
        mPostReference.addValueEventListener(postListener);
        // get all post images
        mPostImgReference.listAll().addOnSuccessListener(listResult -> {
            imgList = new ArrayList<>();
            imgListStr = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                // gs://project-chatting-822af.appspot.com/postImages/0/-MH9BwdTF2Qvu7IaeJWK/1
                Log.d(TAG, "onSuccess: items ? " + item);
                imgList.add(item);
                imgListStr.add(String.valueOf(item));
            }
            StorageReference[] img = imgList.toArray(new StorageReference[imgList.size()]);
            Log.d(TAG, "onSuccess: " + imgList.toString());
            Log.d(TAG, "onSuccess: " + img[0]);
            adapter = new MyAdapter(getApplicationContext(), img);
            adapter.notifyDataSetChanged();
            binding.detailViewPager.setAdapter(adapter);
            // page change listener
            binding.detailViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    /*index = position;*/
                }


                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            // indicator
            binding.pageIndicatorView.setViewPager(binding.detailViewPager);
        });
        mPostListener = postListener;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }
    }

    //채팅하기
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(PostDetailActivity.this, ChattingActivity.class);
        intent.putExtra(ChattingActivity.EXTRA_USER_ID_KEY, postUid);
        startActivity(intent);
    }

    public class MyAdapter extends PagerAdapter {
        private Context context;
        private StorageReference[] images;

        public MyAdapter(Context context, StorageReference[] images) {
            this.context = context;
            this.images = images;
            Log.d(TAG, "MyAdapter: " + Arrays.toString(this.images));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Log.d(TAG, "instantiateItem: " + position);
            ImageView imageView = new ImageView(context);
            imageView.setAdjustViewBounds(true);
            Glide.with(context).load(images[position]).placeholder(R.drawable.kiwi_30).fitCenter().centerCrop().into(imageView);
            container.addView(imageView, 0);
            // click > full screen fragment
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(PostDetailActivity.this, PostImagesActivity.class);
                intent.putExtra(PostImagesActivity.EXTRA_POST_IMAGES_KEY, imgListStr);
                startActivity(intent);
            });
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((ImageView) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PostDetailActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.LOCATION_KEY, mLocationPostKey);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}