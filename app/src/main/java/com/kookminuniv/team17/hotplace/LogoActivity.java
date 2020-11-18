// LogoActivity: 시작 전 화면으로 몇초간 로고화면을 보여주고, 위치 정보 권한을 받지 않았다면 받아옴

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class LogoActivity extends AppCompatActivity {
    private MainBackPressCloseHandler mainBackPressCloseHandler;
    UserInformation user;

    ImageView logoImg;
    TextView tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        tmp = (TextView) findViewById(R.id.logoText);
        logoImg = (ImageView) findViewById(R.id.logoImage);

        // 임시로 기다림
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
                // 현재 위치 받아오고 user에 설정 (Location, address, goo)
                getLocationInfo();

                // 인텐트 보냄 - user(location)
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

    // 현재 위치 받아오고 user에 설정 (Location, address, goo)
    private void getLocationInfo() {
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한에 동의해주세요.", Toast.LENGTH_LONG).show();
            finish();
        }
        Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        user.setUserLocation(userLocation);

        // 로케이션 -> 주소
        Geocoder gc = new Geocoder(this);
        List<Address> list = null;
        try {
            // user location의 위도 경도를 가져와 주소 정보로 변환
            list = gc.getFromLocation(user.getUserLocation().getLatitude(), user.getUserLocation().getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list != null){
            if(list.size() == 0);
            else{
                // 그 중 주소만 따와서 user의 address에 저장
                user.setAddress(list.get(0).getAddressLine(0));
            }
        }

        // 주소 -> 구
        String[] parsedAddress = user.getAddress().split(" ");
        String currentGoo = parsedAddress[2];
        user.setGoo(currentGoo);

        // 임시
        //user.setAddress("대한민국 서울특별시 성북구 정릉동 정릉로 77");
        //user.setGoo("성북구");
    }

    // 뒤로가기
    @Override
    public void onBackPressed() {
        mainBackPressCloseHandler.onBackPressed();
    }
}