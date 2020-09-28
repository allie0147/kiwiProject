package com.android.projectchatting.ui.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.projectchatting.R;
import com.android.projectchatting.model.Locations;
import com.android.projectchatting.custom.CustomDialog;
import com.android.projectchatting.custom.CustomProgressDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MAPS_ACTIVITY";
    //permissions
    private static final int PERMISSIONS_REQUEST_CODE = 200;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    boolean needRequest = false;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    //    private static final int UPDATE_INTERVAL_MS = 10000;  // 10초
    private static final int ZOOM = 17;
    private static final float ALPHA = 0.9f;

    private Marker currentMarker = null;
    private GoogleMap mMap;
    private Geocoder geocoder;
    private EditText searchText;
    Location mCurrentLocation;
    LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private String markerTitle = "여기가 맞나요?";

    private CustomProgressDialog progressDialog;
    private CustomDialog dialog;
    private ArrayList<String> locationKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_maps);
        locationKey = new ArrayList<>();
        dialog = new CustomDialog(MapsActivity.this);
        progressDialog = new CustomProgressDialog(MapsActivity.this);
        progressDialog.setTitleView("현재 위치를 찾는 중이에요!");
        searchText = findViewById(R.id.maps_search_address);
        locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.maps_search_button).setOnClickListener(v -> {
            // 키보드 내리기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
            String search = searchText.getText().toString();
            if (search.length() == 0) {
                dialog.makeSimpleDialog("주소를 입력하세요!", "확인");
                dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                    @Override
                    public void onPositiveClicked() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegativeClicked() {
                    }
                });
                dialog.show();
            } else {
                progressDialog.show();

                searchAddress(search);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            progressDialog.show();
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(() -> {
                    progressDialog.show();
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    return false;
                });
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            if (progressDialog.isShowing()) progressDialog.dismiss();
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);
        setDefaultLocation();
        progressDialog.show();
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            startLocationUpdates(); // 3. 위치 업데이트 시작
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                dialog.makeSimpleDialog("동네 인증을 하시려면 위치 서비스 권한이 필요해요", "설정하기");
                dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                    @Override
                    public void onPositiveClicked() {
                        if (dialog.isShowing()) dialog.dismiss();
                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(MapsActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }

                    @Override
                    public void onNegativeClicked() {
                    }
                });

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                if (progressDialog.isShowing()) progressDialog.dismiss();
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(() -> {
            progressDialog.show();
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            return false;
        });
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                // 현재 주소 가져오는 method 실행
                String markerSnippet = getCurrentAddress(currentPosition);
                Log.d(TAG, "marker Snippet : " + markerSnippet);
                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocation = location;
            }
        }
    };

    //지오코더... GPS를 주소로 변환
    private String getCurrentAddress(LatLng currentPosition) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    currentPosition.latitude,
                    currentPosition.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(this, "서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        if (addresses == null || addresses.size() == 0) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0);
        }
    }

    // 현재 위치 찾기
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).alpha(ALPHA);
        currentMarker = mMap.addMarker(markerOptions);
        currentMarker.showInfoWindow();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
        mMap.setOnInfoWindowClickListener(this);
    }

    // 디폴트 위치 : 서울(역)
    private void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치 정보를 가져올 수 없어요";
        String markerSnippet = "위치 권한과 GPS 활성 여부를 확인하세요";
        if (currentMarker != null) currentMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).alpha(ALPHA);
        currentMarker = mMap.addMarker(markerOptions);
        currentMarker.showInfoWindow();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, ZOOM);
        mMap.moveCamera(cameraUpdate);
    }

    // 주소 검색 시
    private void searchAddress(String search) {
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocationName(search, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "search: " + addressList.get(0).toString());
        String[] splitStr = addressList.get(0).toString().split(",");
        String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
        String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
        String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
        LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        if (currentMarker != null) currentMarker.remove();
        if (progressDialog.isShowing()) progressDialog.dismiss();
        currentMarker = mMap.addMarker(new MarkerOptions().position(point).title(markerTitle)
                .snippet(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).alpha(ALPHA));
        currentMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, ZOOM));
        mMap.setOnInfoWindowClickListener(this);
    }

    // 인포 윈도우 클릭시, 디비 저장 및 메인화면이동
    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "onInfoWindowClick: " + marker.getPosition());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        Log.d(TAG, "user: " + user);
        Log.d(TAG, "onInfoWindowClick: " + Arrays.toString(marker.getSnippet().split(" ")));
        String goo = marker.getSnippet().split(" ")[2];
        String dong = marker.getSnippet().split(" ")[3];
        dialog.makeFullDialog(goo + "를 내 동네로 설정하시겠어요?", marker.getSnippet(), "네", "다시 찾을래요");
        dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
            @Override
            public void onPositiveClicked() {
                //db 저장
                Locations locations = new Locations();
                String gooId = String.valueOf(locations.getGooList().indexOf(goo));
                locationKey.add(gooId);
                locationKey.add(goo);
                Map<String, Object> userLocations = new HashMap<>();
                userLocations.put(user.getUid() + "/locationId", gooId);
                userLocations.put(user.getUid() + "/location", goo);
                userLocations.put(user.getUid() + "/dong", dong);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("users").updateChildren(userLocations).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (dialog.isShowing()) dialog.dismiss();
                        sendUserHome();
                    } else {
                        dialog.makeSimpleDialog("오류 발생! 재시도 하세요", "확인");
                        dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                            @Override
                            public void onPositiveClicked() {
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegativeClicked() {

                            }
                        });
                        dialog.show();
                    }
                });
            }

            @Override
            public void onNegativeClicked() {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    //permissions
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //permissions
    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        dialog.makeTitleDialog("위치 서비스 비활성화", "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n어플 설정을 수정하세요", "설정하기");
        dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
            @Override
            public void onPositiveClicked() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }

            @Override
            public void onNegativeClicked() {

            }
        });
        dialog.show();
    }

    //permissions
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                dialog.makeSimpleDialog("위치정보 요청이 거부되었습니다\n앱을 다시 실행하여 요청을 허가 해 주세요", "확인");
                dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                    @Override
                    public void onPositiveClicked() {
                        if (dialog.isShowing()) dialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onNegativeClicked() {

                    }
                });
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    dialog.show();
                } else {
                    // "다시 묻지 않음"을 사용자가 체크하/고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    dialog.makeSimpleDialog("위치정보 요청이 거부되었습니다\n설정(앱 정보)에서 요청을 허용 해 주세요", "확인");
                    dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
                        @Override
                        public void onPositiveClicked() {
                            if (dialog.isShowing()) dialog.dismiss();
                            finish();
                        }

                        @Override
                        public void onNegativeClicked() {

                        }
                    });
                    dialog.show();
                }
            }
        }
    }

    //permissions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_ENABLE_REQUEST_CODE) {//사용자가 GPS 활성 시켰는지 검사
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {
                    Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                    needRequest = true;
                    return;
                }
            }
        }
    }

    //런타임 퍼미션 처리를 위한 메서드
    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    // 인증 성공시
    private void sendUserHome() {
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.putStringArrayListExtra(MainActivity.LOCATION_KEY, locationKey);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        dialog.makeSimpleDialog("동네 인증 성공!", "둘러보기");
        dialog.setDialogListener(new CustomDialog.CustomDialogListener() {
            @Override
            public void onPositiveClicked() {
                if (dialog.isShowing()) dialog.dismiss();
                startActivity(intent);
                finish();
            }

            @Override
            public void onNegativeClicked() {
            }
        });
        dialog.show();
    }
}