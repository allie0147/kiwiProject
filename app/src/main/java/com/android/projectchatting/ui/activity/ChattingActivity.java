package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.android.projectchatting.databinding.ActivityChattingBinding;
import com.android.projectchatting.model.Chat;
import com.android.projectchatting.viewAdapter.ChatAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChattingActivity extends AppCompatActivity {
    private static final String TAG = "CHATTING_ACTIVITY";
    public static final String EXTRA_USER_ID_KEY = "user_id";
    private static final String MESSAGES_CHILD = "user-chats";
    private static final String CHAT_LIST = "chat-lists";
    private ActivityChattingBinding binding;

    private DatabaseReference messagesRef;
    private DatabaseReference postUserRef;
    private DatabaseReference listRef;
    private FirebaseRecyclerOptions options;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ChatAdapter mAdapter;
    private String postUid;
    private String location;
    private String locationId;
    private LinearLayoutManager mLinearLayoutManager;
    private boolean isFirstChat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChattingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        // 채팅하기 눌렀을 시, 채팅 리스트 눌렀을 시, 상대방의 uid
        postUid = getIntent().getStringExtra(EXTRA_USER_ID_KEY);
        if (postUid != null) {
            FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD).child(mUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                messagesRef = FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD).child(mUser.getUid()).child(postUid);
                            } else {
                                messagesRef = FirebaseDatabase.getInstance().getReference().child(MESSAGES_CHILD).child(postUid).child(mUser.getUid());
                            }
                            options = new FirebaseRecyclerOptions.Builder<Chat>()
                                    .setQuery(messagesRef, Chat.class)
                                    .build();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            listRef = FirebaseDatabase.getInstance().getReference().child(CHAT_LIST);
            postUserRef = FirebaseDatabase.getInstance().getReference().child("users");
        }
        Log.d(TAG, "onCreate: " + postUid);
        // edittext
        binding.chattingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    binding.chattingSend.setEnabled(true);
                } else {
                    binding.chattingSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.chattingSend.setOnClickListener(v -> {
            String msg = binding.chattingEditText.getText().toString();
            if (!msg.equals("")) sendMessage(mUser.getUid(), postUid, msg);
        });
        // image
        binding.chattingAddImage.setOnClickListener(v -> {
        });
        // tool bar
        binding.chattingToolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private void sendMessage(String sender, String receiver, String message) {
        HashMap<String, Object> sendingMessage = new HashMap<>();
        sendingMessage.put("sender", sender);
        sendingMessage.put("receiver", receiver);
        sendingMessage.put("message", message);
        sendingMessage.put("wDate", System.currentTimeMillis());
        if (isFirstChat) {
            Map<String, Object> chatListMap = new HashMap<>();
            chatListMap.put("nickname", binding.chattingUsername.getText().toString());
            chatListMap.put("location", location);
            Map<String, Object> chatListReverse = new HashMap<>();
            chatListReverse.put("nickname", mUser.getDisplayName());
            chatListReverse.put("location", location);
            listRef.child(mUser.getUid()).child(postUid).setValue(chatListMap).addOnSuccessListener(aVoid -> isFirstChat = false);
            listRef.child(postUid).child(mUser.getUid()).setValue(chatListReverse);
        }
        messagesRef.push().setValue(sendingMessage).addOnSuccessListener(aVoid2 -> binding.chattingEditText.setText(""));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // set adapter
        new Handler().postDelayed(() -> {
            mAdapter = new ChatAdapter(options, ChattingActivity.this);
            mLinearLayoutManager = new LinearLayoutManager(this);
            binding.chattingRecycler.setHasFixedSize(true);
            binding.chattingRecycler.setLayoutManager(mLinearLayoutManager);
            binding.chattingRecycler.setAdapter(mAdapter);
            // 메세지 보냈을 시, 새 메세지로 스크롤 이동
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    int messageCount = mAdapter.getItemCount();
                    int lastPosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastPosition == -1 ||
                            (positionStart >= (messageCount - 1) && lastPosition == (positionStart - 1))) {
                        binding.chattingRecycler.smoothScrollToPosition(positionStart);
                    }
                }
            });
            mAdapter.startListening();
        }, 1000);
        // 입력시 scroll up event
        binding.chattingEditText.setOnFocusChangeListener((view, b) -> {
            Log.d(TAG, "onStart: " + mAdapter.getItemCount());
            if (b) {
                binding.chattingRecycler.postDelayed(() -> binding.chattingRecycler.smoothScrollToPosition(mAdapter.getItemCount()), 100);
            }
        });
        postUserRef.child(postUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "nickname: " + snapshot.toString());
                location = snapshot.child("location").getValue().toString();
                locationId = snapshot.child("locationId").getValue().toString();
                binding.chattingUsername.setText(snapshot.child("nickname").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChattingActivity.this, MainActivity.class);
        ArrayList<String> locations = new ArrayList<>();
        locations.add(locationId);
        locations.add(location);
        intent.putStringArrayListExtra(MainActivity.EXTRA_CHAT_LOCATION_KEY, locations);
        startActivity(intent);
        finish();
    }
}