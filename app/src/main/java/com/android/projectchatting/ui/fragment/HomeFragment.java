package com.android.projectchatting.ui.fragment;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class HomeFragment extends PostsFragment {
    private static final String TAG = "HOME_FRAGMENT";
    private static final String LOCATION_KEY = "location_key";
    ArrayList<String> locations = new ArrayList<>();

    public HomeFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        locations = getLocation();
        return databaseReference.child("posts").child(locations.get(0));
    }

    @Override
    public ArrayList<String> getLocation() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            locations = bundle.getStringArrayList(LOCATION_KEY); //0 : locationId, 1: location
        }
        return locations;
    }
}