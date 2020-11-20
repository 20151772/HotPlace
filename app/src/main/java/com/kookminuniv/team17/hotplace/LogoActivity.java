// LogoActivity
// 기능 : 위치권한 받아옴 -> 로고 클릭 -> 현재 위치 받아옴 -> User에 저장 후 인텐트로 보냄
// 개발 : 김명호, 장요셉(현재 위치 받아오기)
// 컬러 : 연빨강 #E85F5D, 빨강 #E53C39, 진빨강 #B22C2B

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

public class LogoActivity extends AppCompatActivity {
    private MainBackPressCloseHandler mainBackPressCloseHandler;
    UserInformation user;

    ImageView logoImg;
    TextView tmp;

    Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        // 액션 바
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        tmp = (TextView) findViewById(R.id.logoText);
        logoImg = (ImageView) findViewById(R.id.logoImage);

        // 잠깐 기다림
        try {
            user = new UserInformation();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 위치 권한을 받아옴
        getPermission();

        Toast.makeText(this, "로고를 클릭해주세요.", Toast.LENGTH_LONG).show();

        // 로고 이미지 클릭
        logoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 현재 위치 받아오고 user에 저장
                getLocationInfo();

                // 못 받아왔으면 리턴
                if(user.getAddress() == null){
                    Toast.makeText(getApplicationContext(), "현재 위치를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 인텐트 보냄 - user(location, address, goo)
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("UserObject", user);
                startActivity(intent);
                finish();
            }
        });

        // 뒤로 가기 버튼
        mainBackPressCloseHandler = new MainBackPressCloseHandler(this);
    }

    // 위치 권한 받아옴
    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    // 현재 위치 받아오고 user에 저장
    private void getLocationInfo() {
        // 다시 한 번 권한 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한에 동의해주세요.", Toast.LENGTH_LONG).show();
            finish();
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // GPS 설정 꺼져있으면 켜기
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }

        locationManager.removeUpdates(locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        // 현재 위치를 무사히 받아왔다면,
        if(user.getUserLocation() != null) {
            // 로케이션 -> 주소
            Geocoder gc = new Geocoder(this);
            List<Address> list = null;
            try {
                // user location의 위도 경도를 가져와 주소 정보로 변환
                list = gc.getFromLocation(user.getUserLocation().getLatitude(), user.getUserLocation().getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (list != null) {
                if (list.size() == 0) ;
                else {
                    // 그 중 주소만 따와서 user의 address에 저장
                    user.setAddress(list.get(0).getAddressLine(0));
                }
            }

            // 주소 -> 구
            String[] parsedAddress = user.getAddress().split(" ");
            String currentGoo = parsedAddress[2];
            user.setGoo(currentGoo);
        }
    }

    // 현재 위치를 가져오기 위한 로케이션 리스너
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(location == null){
                Toast.makeText(getApplicationContext(), "위치 정보 획득에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                user.setUserLocation(location);
            }
            locationManager.removeUpdates(this);
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }
        @Override
        public void onProviderEnabled(String s) { }
        @Override
        public void onProviderDisabled(String s) { }
    };

    // 뒤로가기
    @Override
    public void onBackPressed() {
        mainBackPressCloseHandler.onBackPressed();
    }
}