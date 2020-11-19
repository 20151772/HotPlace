// MainActivity: 프래그먼트로 홈과 마이페이지 구현
// 홈: 글 리스트(사진, 제목, 위치)와 글쓰기 버튼
// 마이페이지: 아이디, 위치, 글 쓴 갯수 정도

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MainBackPressCloseHandler mainBackPressCloseHandler;
    UserInformation user;
    CacheClear cc;

    String imageName;
    boolean isAll = false;

    SwipeRefreshLayout swipe;
    ArrayList<ListData> items = null;
    ListView listView;
    CustomListAdapter adapter;

    Animation fab_open, fab_close;
    Boolean isFabOpen = false;
    FloatingActionButton fab, fabMyPageBtn, fabCreateBtn, fabAllBtn;
    TextView locationText;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference articleRef;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Activity");

        // 인텐트 받아옴 - user(location, user_id, login)
        Intent intent = getIntent();
        if(intent != null) {
            user = (UserInformation) intent.getSerializableExtra("UserObject");
            Log.d("MA : user_id", user.getUser_id());
        }

        // 액션 바
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("메인 - 지역 검색");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        // 캐쉬 클리어
        cc = new CacheClear();
        cc.clearCache(MainActivity.this);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabMyPageBtn = (FloatingActionButton) findViewById(R.id.fabMyPageBtn);
        fabCreateBtn = (FloatingActionButton) findViewById(R.id.fabCreateBtn);
        fabAllBtn = (FloatingActionButton) findViewById(R.id.fabAllBtn);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim();
            }
        });
        // 마이페이지
        fabMyPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim();
                cc.clearCache(MainActivity.this);
                Intent intent = new Intent(getApplicationContext(), MypageActivity.class);
                intent.putExtra("UserObject", user);
                startActivity(intent);
            }
        });
        // 글 생성
        fabCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim();
                cc.clearCache(MainActivity.this);
                Intent intent2 = new Intent(getApplicationContext(), ArticleWriteActivity.class);
                intent2.putExtra("UserObject", user);
                startActivity(intent2);
            }
        });
        // 전체 검색 <-> 지역 검색
        fabAllBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                anim();
                cc.clearCache(MainActivity.this);
                if(!isAll){
                    isAll = true;
                    getList("all");
                    actionBar.setTitle("메인 - 전체 검색");
                    Toast.makeText(getApplicationContext(), "전체 글을 검색합니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    isAll = false;
                    getList("goo");
                    actionBar.setTitle("메인 - 지역 검색");
                    Toast.makeText(getApplicationContext(), "주변 글을 검색합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        locationText = (TextView) findViewById(R.id.locationText);
        String[] parsedAddress = user.getAddress().split(" ");
        String shortedAddress = "";
        for(int i=2; i<parsedAddress.length; i++){
            shortedAddress += parsedAddress[i] + " ";
        }
        locationText.setText(shortedAddress);

        listView = (ListView) findViewById(R.id.listView);

        // DB(articles) 접속 - 현재 구와 글들의 구와 비교 후 가져옴
        getList("goo");

        // 리스트 선택
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cc.clearCache(MainActivity.this);
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("UserObject", user);
                intent.putExtra("article_id", items.get(i).getArticleNumber());
                startActivity(intent);
            }
        });

        // 스와이프
        swipe = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isAll){
                    getList("goo");
                }
                else{
                    getList("all");
                }
                if(null != swipe){
                    swipe.setRefreshing(false);
                }
            }
        });

        // 뒤로가기 버튼
        mainBackPressCloseHandler = new MainBackPressCloseHandler(this);
    }

    public void getList(String what){
        // 리스트 아이템 초기화
        final String fwhat = what;
        items = new ArrayList<ListData>();

        // DB(articles) 접속 - 현재 구와 글들의 구와 비교 후 가져옴
        articleRef = db.getReference().child("articles");
        articleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();

                while(child.hasNext()){
                    String article_id = child.next().getKey();
                    if(article_id.equals("articles_count") || article_id.equals("articles_number")){
                        continue;
                    }
                    else{
                        String articleGoo = dataSnapshot.child(article_id).child("goo").getValue(String.class);

                        if(fwhat == "goo") {
                            // 현재 구와 해당 글의 구가 같을 때 그 정보를 가져옴
                            if (user.getGoo().equals(articleGoo)) {
                                // 이미지 카운트를 불러와
                                String title = dataSnapshot.child(article_id).child("title").getValue(String.class);

                                imageName = dataSnapshot.child(article_id).child("image_names").child("0").getValue(String.class);
                                ListData item = new ListData(imageName, article_id, title);
                                items.add(item);
                            } else continue;
                        }
                        else if(fwhat == "all"){
                            String title = dataSnapshot.child(article_id).child("title").getValue(String.class);

                            imageName = dataSnapshot.child(article_id).child("image_names").child("0").getValue(String.class);
                            ListData item = new ListData(imageName, article_id, title);
                            items.add(item);
                        }
                    }
                }
                adapter = new CustomListAdapter(items);
                listView.setAdapter(adapter);
                return;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void anim(){
        if(isFabOpen){
            fab.setImageResource(R.drawable.round_add_black_18dp);
            fabMyPageBtn.startAnimation(fab_close);
            fabCreateBtn.startAnimation(fab_close);
            fabAllBtn.startAnimation(fab_close);
            fabMyPageBtn.setClickable(false);
            fabCreateBtn.setClickable(false);
            fabAllBtn.setClickable(false);
            isFabOpen = false;
        }
        else{
            fab.setImageResource(R.drawable.round_clear_black_18dp);
            fabMyPageBtn.startAnimation(fab_open);
            fabCreateBtn.startAnimation(fab_open);
            fabAllBtn.startAnimation(fab_open);
            fabMyPageBtn.setClickable(true);
            fabCreateBtn.setClickable(true);
            fabAllBtn.setClickable(true);
            isFabOpen = true;
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