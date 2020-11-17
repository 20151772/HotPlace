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
                // 현재 위치를 받아옴
                getLocation();

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

    // 현재 위치 받아옴
    private void getLocation() {
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한에 동의해주세요.", Toast.LENGTH_LONG).show();
            finish();
        }
        Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        user.setUserLocation(userLocation);
    }

    // 뒤로가기
    @Override
    public void onBackPressed() {
        mainBackPressCloseHandler.onBackPressed();
    }
}