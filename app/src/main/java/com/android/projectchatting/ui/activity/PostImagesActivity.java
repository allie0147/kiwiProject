package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.projectchatting.R;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class PostImagesActivity extends AppCompatActivity {
    public static final String EXTRA_POST_IMAGES_KEY = "post_images_key";
    private static final String TAG = "POST_IMAGES_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_images);
        Intent intent = getIntent();
        ArrayList<String> list = intent.getStringArrayListExtra(EXTRA_POST_IMAGES_KEY);
        Log.d(TAG, "onCreate: " + list.toString());

    }

    public class ImageAdapter extends PagerAdapter {
        private Context context;
        private StorageReference[] images;

        public ImageAdapter(Context context, StorageReference[] images) {
            this.context = context;
            this.images = images;
            Log.d(TAG, "ImageAdapter: " + Arrays.toString(this.images));
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
            Glide.with(context).load(images[position]).centerCrop().into(imageView);
            container.addView(imageView, 0);
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
}