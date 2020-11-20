// LocationActivity
// 기능 : ArticleActivity에서 불러온 위도 경도 값으로 지도에 그 위치 표시
// 개발 : 장요셉, 김명호(구현 적용)

package com.kookminuniv.team17.hotplace;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationActivity extends AppCompatActivity implements  OnMapReadyCallback {
    Double latitude, longitude;

    MapFragment fragment;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Intent intent = getIntent();
        if(intent != null){
            latitude = intent.getDoubleExtra("latitude", 0);
            longitude = intent.getDoubleExtra("longitude", 0);
        }

        // 액션 바
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("지도");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment1);
        fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map){
        this.map = map;
        drawMap();
    }

    void drawMap(){
        int check = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(check != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }
        else{
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //지도 종류 : NORMAL
            map.setMyLocationEnabled(true);
            // 줌컨트롤 표시 여부(1~21)
            map.getUiSettings().setZoomControlsEnabled(true);
            // 좌표 지정 new LatLng
            LatLng geoPoint = new LatLng(latitude, longitude);
            // 지도를 바라보는 카메라 이동
            map.moveCamera(CameraUpdateFactory.newLatLng(geoPoint));
            // 카메라 애니메이션 효과
            map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(geoPoint,15));
            // 줌 : 1~21 숫자가 클수록 자세한 지도

            //맵에 마커 표시

            MarkerOptions marker = new MarkerOptions();
            marker.position(geoPoint); // 마커 표시할 좌표
            marker.title("현재 위치"); // 마커의 제목
            marker.snippet("ㅁㄴㅇㄹ"); // 마커의 설명
            map.addMarker(marker); //지도에 마커 추가가
        }
    }

    // 액션 바 뒤로가기
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}