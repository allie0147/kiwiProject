package com.android.projectchatting.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.projectchatting.R;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.custom.MyDividerItemDecoration;
import com.android.projectchatting.model.Posts;
import com.android.projectchatting.ui.activity.PostActivity;
import com.android.projectchatting.ui.activity.PostDetailActivity;
import com.android.projectchatting.viewHolder.PostsViewHolder;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public abstract class PostsFragment extends Fragment {
    private static final String TAG = "POSTS_FRAGMENT";
    private DatabaseReference mPostReference;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter<Posts, PostsViewHolder> mAdapter;
    private View rootView;
    private ArrayList<String> locations;
    private ShimmerFrameLayout shimmerFrameLayout;

    public PostsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView: ");
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        locations = getLocation();
        // fragment home
        TextView toolbar = rootView.findViewById(R.id.home_location);
        toolbar.setText(locations.get(1)); // locationStr: ㅇㅇ구
        mPostReference = FirebaseDatabase.getInstance().getReference();
        shimmerFrameLayout = rootView.findViewById(R.id.home_shimmer);
        mRecyclerView = rootView.findViewById(R.id.home_recycler);
        mRecyclerView.setHasFixedSize(true);
        // 역순으로 출력
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        shimmerFrameLayout.startShimmer();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        // override getQuery & getLocation!
        Query postsQuery = getQuery(mPostReference);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postsQuery, Posts.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                Log.d(TAG, "onCreateViewHolder: ");
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PostsViewHolder(inflater.inflate(R.layout.home_list, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder viewHolder, int position, @NonNull Posts model) {
                Log.d(TAG, "onBindViewHolder: " + position);
                Log.d(TAG, "onBindViewHolder: " + getItemCount());
                DatabaseReference postRef = getRef(position);
                if (getItemCount() == (position + 1)) {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
                // Set click listener for the whole post view
                String locationId = locations.get(0);
                String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(v -> {
                    // Launch PostDetailActivity
                    Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                    if (locations.size() != 2) {
                        ArrayList<String> locationPost = new ArrayList<>(); // id, str, postkey
                        locationPost.add(locationId);
                        locationPost.add(locations.get(1));
                        locationPost.add(postKey);
                        intent.putExtra(PostDetailActivity.EXTRA_LOCATION_POST_KEY, locationPost);
                    } else {
                        locations.add(postKey); // id, str, postkey
                        intent.putExtra(PostDetailActivity.EXTRA_LOCATION_POST_KEY, locations);
                    }
                    startActivity(intent);
                });
                viewHolder.bindToPost(model, rootView, locationId, postKey);
            }
        };
        SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.home_refresh);
        refreshLayout.setColorSchemeResources(android.R.color.black);
        refreshLayout.setOnRefreshListener(() -> {
            mAdapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(rootView.getContext(), LinearLayoutManager.VERTICAL, 16));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        } else {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            CustomDialog dialog = new CustomDialog(rootView.getContext(), "아직 게시글이 없어요!\n새로운 중고 거래 글을 작성하시겠어요?", "네!", "괜찮아요");
            dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                @Override
                public void onPositiveClicked() {
                    Intent intent = new Intent(rootView.getContext(), PostActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }

                @Override
                public void onNegativeClicked() {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            mAdapter.stopListening();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    public abstract ArrayList<String> getLocation();

}