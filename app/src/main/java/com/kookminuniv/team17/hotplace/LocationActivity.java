// LocationActivity: 글에서 위치를 누르면 지도 화면이 뜨고 그 위치에 맞는 장소가 표시됨

package com.kookminuniv.team17.hotplace;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class LocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
    }
}