// LogoActivity: 시작 전 화면으로 몇초간 로고화면을 보여주고, 위치 정보 권한을 받지 않았다면 받아옴

package com.kookminuniv.team17.hotplace;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
    }
}