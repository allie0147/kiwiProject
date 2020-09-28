package com.android.projectchatting.viewAdapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectchatting.R;
import com.android.projectchatting.model.Chat;
import com.android.projectchatting.ui.activity.ChattingActivity;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends FirebaseRecyclerAdapter<HashMap<String, String>, ChatListAdapter.ChatListViewHolder> {

    private static final String TAG = "CHAT_LIST_ADAPTER";
    private String mUid;
    private ShimmerFrameLayout shimmer;
    private View divider;

    public ChatListAdapter(@NonNull FirebaseRecyclerOptions options, String mUid, View view) {
        super(options);
        this.mUid = mUid;
        shimmer = view.findViewById(R.id.chat_list_shimmer);
        divider = view.findViewById(R.id.chatting_list_divider);
        Log.d(TAG, "ChatListAdapter: constructed");
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ChatListViewHolder(inflater.inflate(R.layout.chatting_list, parent, false));
    }

    @Override
    public void onChildChanged(@NonNull ChangeEventType type, @NonNull DataSnapshot snapshot, int newIndex, int oldIndex) {
        super.onChildChanged(type, snapshot, newIndex, oldIndex);
        if (newIndex < 0) {
            divider.setVisibility(View.GONE);
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
        } else if ((newIndex + 1) == getItemCount()) { // newIndex starts with 0
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatListViewHolder holder, int position, @NonNull HashMap<String, String> model) {
        Log.d(TAG, "onBindViewHolder: " + model.get("nickname"));
        Log.d(TAG, "onBindViewHolder: " + model.get("uId"));
        Log.d(TAG, "onBindViewHolder: " + model.get("location"));
        Log.d(TAG, "onBindViewHolder: " + mUid);
        Log.d(TAG, "itemcount in bind: " + getItemCount());
        // 1:1 채팅창으로
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ChattingActivity.class);
            intent.putExtra(ChattingActivity.EXTRA_USER_ID_KEY, model.get("uId"));
            view.getContext().startActivity(intent);
        });
        // 채팅 최신 글 가져오기
        FirebaseDatabase.getInstance().getReference().child("user-chats").child(mUid).child(model.get("uId")).getRef().limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onDataChange: " + snapshot.getValue());
                        if (snapshot.getValue() != null) {
                            Log.d(TAG, "not null: " + mUid + " model: " + model.get("uId"));
                            getValue(snapshot, holder, model);
                        } else {
                            FirebaseDatabase.getInstance().getReference().child("user-chats").child(model.get("uId")).child(mUid).getRef().limitToLast(1)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Log.d(TAG, "null: " + mUid + " model: " + model.get("uId"));
                                            getValue(snapshot, holder, model);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    void getValue(DataSnapshot snapshot, ChatListViewHolder holder, @NonNull HashMap<String, String> model) {
        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            Log.d(TAG, "getValue: " + dataSnapshot.toString());
            Chat chat = dataSnapshot.getValue(Chat.class);
            if (chat != null) {
                holder.location.setText(model.get("location"));
                holder.recentMsg.setText(chat.getMessage());
                holder.username.setText(model.get("nickname"));
                holder.dot.setText("·");
                holder.wdate.setReferenceTime(chat.getwDate());
                holder.setProfileImage(model.get("uId"));
            }
        }
    }

    public static class ChatListViewHolder extends RecyclerView.ViewHolder {

        public TextView username, recentMsg, location, dot;
        public RelativeTimeTextView wdate;
        public CircleImageView profileImage;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.chatting_list_username);
            recentMsg = itemView.findViewById(R.id.chatting_list_recent_message);
            dot = itemView.findViewById(R.id.chatting_list_dot);
            location = itemView.findViewById(R.id.chatting_list_location);
            wdate = itemView.findViewById(R.id.chatting_list_wdate);
            profileImage = itemView.findViewById(R.id.chatting_list_profile);
        }

        public void setProfileImage(String uId) {
            FirebaseStorage.getInstance().getReference().child("userImages").child(uId).getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(itemView).load(uri).into(profileImage))
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: no such profile img");
                        Glide.with(itemView).load(R.drawable.round_account_circle_24).into(profileImage);
                    });
        }
    }
}
