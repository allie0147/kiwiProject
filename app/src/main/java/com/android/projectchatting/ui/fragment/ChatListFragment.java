package com.android.projectchatting.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.projectchatting.databinding.FragmentChatListBinding;
import com.android.projectchatting.model.UserModel;
import com.android.projectchatting.viewAdapter.ChatListAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatListFragment extends Fragment {
    public static final String EXTRA_CHAT_LOCATION_KEY = "chat-location";
    private static final String TAG = "CHAT_LIST_FRAGMENT";
    private static final String CHAT_LIST = "chat-lists";
    private View rootView;
    private FragmentChatListBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mReference;
    private ChatListAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        rootView = binding.getRoot();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference().child(CHAT_LIST).child(mUser.getUid());
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<HashMap<String, String>>()
                .setQuery(mReference, snapshot -> {
                    Log.d(TAG, "onActivityCreated: " + snapshot.toString());
                    HashMap<String, String> users = new HashMap<>();
                    users.put("uId", snapshot.getKey()); //상대 id
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Log.d(TAG, "onActivityCreated: for " + snap.toString());
                        users.put(snap.getKey(), (String) snap.getValue()); // nickname, location
                    }
                    return users;
                })
                .build();
        mAdapter = new ChatListAdapter(options, mUser.getUid(), rootView);
        binding.chattingListRecycler.setHasFixedSize(true);
        binding.chattingListRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }
}