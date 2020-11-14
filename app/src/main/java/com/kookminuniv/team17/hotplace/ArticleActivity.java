// ArticleActivity: 글 화면. 사진뷰, 글쓴이, 시간, 제목, 내용, 위치 버튼

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArticleActivity extends AppCompatActivity {

    String title, contents, location, time, user_id;

    ViewPager2 vp2;

    TextView user_idText, timeText, titleText, contentsText;
    ImageButton locationBtn;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference dbRef;

    //+ article_id
    String article_id = "5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        vp2 = (ViewPager2) findViewById(R.id.vp2);
        user_idText = (TextView) findViewById(R.id.user_idText);
        timeText = (TextView) findViewById(R.id.timeText);
        titleText = (TextView) findViewById(R.id.titleText);
        contentsText = (TextView) findViewById(R.id.contentsText);
        locationBtn = (ImageButton) findViewById(R.id.locationBtn);

        // article_id를 기준으로 db에 접근, 텍스트 정보 가져옴.
        dbRef = db.getReference().child("articles");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 정보 받아와서
                user_id = dataSnapshot.child(article_id).child("user_id").getValue(String.class);
                title = dataSnapshot.child(article_id).child("title").getValue(String.class);
                contents = dataSnapshot.child(article_id).child("contents").getValue(String.class);
                time = dataSnapshot.child(article_id).child("time").getValue(String.class);
                location = dataSnapshot.child(article_id).child("location").getValue(String.class);

                // 텍스트 정보 입력
                user_idText.setText(user_id);
                titleText.setText(title);
                contentsText.setText(contents);
                timeText.setText(time);
                actionBar.setTitle(title);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


        // 이미지 정보 입력
        vp2 = findViewById(R.id.vp2);
        ArrayList<ImgData> list = new ArrayList<>();
        list.add(new ImgData(R.drawable.ic_launcher_background));
        list.add(new ImgData(R.drawable.ic_launcher_foreground));
        vp2.setAdapter(new PhotoAdapter(list));

        /*mIndicator = (CircleIndicator3) findViewById(R.id.indicator);
        mIndicator.setViewPager(vp2);
        mIndicator.createIndicator(3, 0);

        vp2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position){
                super.onPageSelected(position);
                //mIndicator.animatePageSelected(position);
            }
        });*/

        // 위치 정보 버튼
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                intent.putExtra("location", location);
                startActivity(intent);
            }
        });
    }

    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}