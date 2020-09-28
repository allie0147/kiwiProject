//package com.android.projectchatting.ui.fragment;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.android.projectchatting.R;
//import com.android.projectchatting.viewAdapter.HomeViewAdapter;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.ArrayList;
//
//public class HomeFragment2 extends Fragment {
//    private static final String TAG = "HOME_FRAGMENT";
//    private static final String LOCATION_KEY = "location_key";
//    private DatabaseReference mPostReference;
//    private HomeViewAdapter mAdapter;
//    private ArrayList<String> locations;
//    private View rootview;
//    private RecyclerView mRecyclerView;
//    private LinearLayoutManager mLayoutManager;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        rootview = inflater.inflate(R.layout.fragment_home, container, false);
//        Bundle bundle = this.getArguments();
//        if (bundle != null) {
//            locations = bundle.getStringArrayList(LOCATION_KEY); //0 : locationId, 1: location
//        }
//        mPostReference = FirebaseDatabase.getInstance().getReference().child("posts").child(locations.get(0));
//        // 유저의 지역(구)
//        TextView toolbarText = rootview.findViewById(R.id.home_location);
//        toolbarText.setText(locations.get(1));
//        mAdapter = new HomeViewAdapter(rootview.getContext(), mPostReference);
//        mRecyclerView = rootview.findViewById(R.id.home_recycler);
//        mRecyclerView.setHasFixedSize(true);
//        return rootview;
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        // 역순으로 출력
//        mLayoutManager = new LinearLayoutManager(getActivity());
//        mLayoutManager.setReverseLayout(true);
//        mLayoutManager.setStackFromEnd(true);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.setAdapter(mAdapter);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        mAdapter.cleanupListener();
//    }
//}