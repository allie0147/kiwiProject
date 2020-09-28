package com.android.projectchatting.viewAdapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectchatting.R;
import com.android.projectchatting.model.Chat;
import com.android.projectchatting.viewHolder.ChatViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatAdapter extends FirebaseRecyclerAdapter<Chat, ChatViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public static final int MESSAGE_TYPE_LEFT = 0;
    public static final int MESSAGE_TYPE_RIGHT = 1;
    private static final String TAG = "CHAT_ADAPTER";
    private Context context;
    private FirebaseUser mUser;
    private Chat mChat;
    private String sender = "";

    public ChatAdapter(@NonNull FirebaseRecyclerOptions<Chat> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Chat chat) {
        holder.bindToChat(chat);
        Log.d(TAG, "onBindViewHolder: " + chat.toString());
        Log.d(TAG, "onBindViewHolder: " + sender.equals(mUser.getUid()));
        Log.d(TAG, "onBindViewHolder: position " + position);
        if (!sender.equals(mUser.getUid())) { // the other
            holder.bindUserProfile(context, sender);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Log.d(TAG, "onCreateViewHolder: ");
        if (viewType == MESSAGE_TYPE_RIGHT) { //my message
            return new ChatViewHolder(inflater.inflate(R.layout.chat_item_right, parent, false), MESSAGE_TYPE_RIGHT);
        } else { //the other's message
            return new ChatViewHolder(inflater.inflate(R.layout.chat_item_left, parent, false), MESSAGE_TYPE_LEFT);
        }
    }

    @Override
    public int getItemViewType(int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "getItemViewType: " + position);
        mChat = getItem(position);
        if (mChat.getSender().equals(mUser.getUid())) {
            sender = mUser.getUid();
            return MESSAGE_TYPE_RIGHT;
        } else {
            sender = mChat.getSender();
            Log.d(TAG, "getItemViewType: " + sender);
            return MESSAGE_TYPE_LEFT;
        }
    }
}
