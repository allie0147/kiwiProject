package com.android.projectchatting.viewHolder;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectchatting.R;
import com.android.projectchatting.model.Chat;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "CHAT_VIEW_HOLDER";
    private CircleImageView profileView;
    private TextView messageView;
    private TextView wDateView;

    public ChatViewHolder(@NonNull View itemView, int MESSAGE_VIEW_TYPE) {
        super(itemView);
        Log.d(TAG, "ChatViewHolder: ");
        if (MESSAGE_VIEW_TYPE == 0) {
            profileView = itemView.findViewById(R.id.chat_left_profile_image);
        }
        messageView = itemView.findViewById(R.id.chat_message);
        wDateView = itemView.findViewById(R.id.chat_wdate);
    }

    public void bindToChat(Chat chat) {
        messageView.setText(chat.getMessage());
        SimpleDateFormat timeFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
        String time = timeFormat.format(chat.getwDate());
        wDateView.setText(time);
    }

    public void setChatImages() {

    }

    public void bindUserProfile(Context context, String sender) {
        Log.d(TAG, "bindUserProfile: " + sender);
        FirebaseStorage.getInstance().getReference()
                .child("userImages")
                .child(sender)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(context).load(uri).circleCrop().fitCenter().into(profileView))
                .addOnFailureListener(e -> Glide.with(context).load(R.drawable.round_account_circle_24).circleCrop().fitCenter().into(profileView));
    }
}
