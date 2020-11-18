package com.kookminuniv.team17.hotplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class myPage extends AppCompatActivity {

    Button mainPage_btn;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);


        mainPage_btn = findViewById(R.id.mainPage_btn);

        mainPage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(myPage.this,ListViewActivity.class);
                startActivity(intent);
            }
        });
    }
}
