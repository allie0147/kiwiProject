package com.android.projectchatting.viewHolder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.projectchatting.R;
import com.android.projectchatting.model.Posts;
import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class PostsViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "POST_VIEW_HOLDER";
    TextView titleView, dongView, priceView;
    RelativeTimeTextView wDateView;
    ImageView imgView;

    public PostsViewHolder(View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.home_title);
        dongView = itemView.findViewById(R.id.home_dong);
        priceView = itemView.findViewById(R.id.home_price);
        imgView = itemView.findViewById(R.id.home_image);
        wDateView = itemView.findViewById(R.id.home_wdate);
    }

    public void bindToPost(Posts post, View view, String locationId, String postId) {
        Log.d(TAG, "bindToPost: ");
        titleView.setText(post.getTitle());
        dongView.setText(post.getDong());
        priceView.setText(post.getPrice());
        wDateView.setReferenceTime(post.getwDate());
        Glide.with(view.getContext()).load(R.raw.spinner).fitCenter().into(imgView);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("/postImages/" + locationId + "/" + postId + "/1");
        Log.d(TAG, "loadWithGlide: " + storageReference);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(view.getContext())
                    .load(uri)
                    .centerCrop().into(imgView);
        });
    }
}