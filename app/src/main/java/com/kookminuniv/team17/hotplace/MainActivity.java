// MainActivity: 프래그먼트로 홈과 마이페이지 구현
// 홈: 글 리스트(사진, 제목, 위치)와 글쓰기 버튼
// 마이페이지: 아이디, 위치, 글 쓴 갯수 정도

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MainBackPressCloseHandler mainBackPressCloseHandler;
    UserInformation user;
    CacheClear cc;

    String currentGoo;  // 현재 구
    String selectedArticleId;  // 선택된 글 아이디


    TextView tv, tv2;  //+글 리스트 중 하나라고 생각
    Button btn;  //+글 쓰기 버튼이라고 생각

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference articleRef;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Activity");

        // 인텐트 받아옴 - user(location, user_id, login)
        Intent intent = getIntent();
        if(intent != null){
            user = (UserInformation)intent.getSerializableExtra("UserObject");
            Log.d("MA : user_id", user.getUser_id());
        }

        // 액션 바
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("메인");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        // 캐쉬 클리어
        cc = new CacheClear();
        cc.clearCache(MainActivity.this);

        tv = (TextView) findViewById(R.id.tv);  //+
        tv2 = (TextView) findViewById(R.id.tv2);  //+
        btn = (Button) findViewById(R.id.btn);  //+

        // 위도경도 -> 주소정보 변환 후 user에 저장
        setAddressFromLocation();

        // 주소 정보를 파싱해서 현재 구를 가져옴
        String[] parsedAddress = user.getAddress().split(" ");
        currentGoo = parsedAddress[2];

        // DB(articles) 접속 - 현재 구와 글들의 구와 비교 후 가져옴
        articleRef = db.getReference().child("articles");
        articleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                int count = 1;  //+

                while(child.hasNext()){
                    // 현재 구와 해당 글의 구가 같을 때 그 정보를 가져옴
                    // 가져올 정보는 article_id, title, address
                    //+
                    String article_id = child.next().getKey();
                    if(article_id.equals("articles_count") || article_id.equals("articles_number")){
                        continue;
                    }
                    else{
                        String articleGoo = dataSnapshot.child(article_id).child("goo").getValue(String.class);
                        if(currentGoo.equals(articleGoo)){
                            if(count == 1){
                                //tv.setText(dataSnapshot.child(article_id).child("title").getValue(String.class));
                                tv.setText(article_id);
                                count += 1;
                            }
                            else if(count == 2){
                                //tv2.setText(dataSnapshot.child(article_id).child("title").getValue(String.class));
                                tv2.setText(article_id);
                                count += 1;
                            }
                            else{}
                        }
                        else continue;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        //+ 글쓰기 버튼
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 인텐트 보냄 - user(location, user_id, login)
                cc.clearCache(MainActivity.this);
                Intent intent = new Intent(getApplicationContext(), ArticleWriteActivity.class);
                intent.putExtra("UserObject", user);
                startActivity(intent);
                finish();
            }
        });

        //+ 글 선택
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 인텐트 보냄 - user(location, user_id, login) / article_id
                cc.clearCache(MainActivity.this);
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("UserObject", user);
                intent.putExtra("article_id", Integer.parseInt(tv.getText().toString()));
                startActivity(intent);
                finish();
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 인텐트 보냄 - user(location, user_id, login) / article_id
                cc.clearCache(MainActivity.this);
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("UserObject", user);
                intent.putExtra("article_id", Integer.parseInt(tv2.getText().toString()));
                startActivity(intent);
                finish();
            }
        });

        // 뒤로가기 버튼
        mainBackPressCloseHandler = new MainBackPressCloseHandler(this);
    }

    // 위도경도 -> 주소정보 변환 후 user에 저장
    private void setAddressFromLocation() {
        Geocoder gc = new Geocoder(this);
        List<Address> list = null;
        try {
            // user location의 위도 경도를 가져와 주소 정보로 변환
            //list = gc.getFromLocation(user.getUserLocation().getLatitude(),
            //        user.getUserLocation().getLongitude(), 1);
            list = gc.getFromLocation(37.610883, 126.995546, 1);  //+ 국민대
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
    }

    // 뒤로가기
    @Override
    public void onBackPressed() { mainBackPressCloseHandler.onBackPressed(); }
    // 액션 바 뒤로가기
    public boolean onSupportNavigateUp(){
        onBackPressed();
        cc.clearCache(this);
        return super.onSupportNavigateUp();
    }
}