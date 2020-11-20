// MainActivity: 프래그먼트로 홈과 마이페이지 구현
// 홈: 글 리스트(사진, 제목, 위치)와 글쓰기 버튼
// 마이페이지: 아이디, 위치, 글 쓴 갯수 정도

// MainActivity
// 기능 : 파이어베이스 db, storage와 연동해서 글 정보를 리스트뷰에 뿌려줌. 플로팅 액션 버튼으로 전체 검색,
//      글 쓰기, 마이페이지 버튼 구현. 리스트뷰의 경우 스와이프 리프레쉬 레이아웃으로 새로고침 구현.
// 개발 : 김명호, 고동훈(리스트뷰, 플로팅 액션 버튼)

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

    String imageName;
    boolean isAll = false;  // 전체 검색 여부

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

        // 인텐트 받아옴 - user(location, address, goo, user_id, login)
        Intent intent = getIntent();
        if(intent != null) {
            user = (UserInformation) intent.getSerializableExtra("UserObject");
        }

        // 액션 바
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("메인 - 지역 검색");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        // 플로팅 액션 버튼
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

                // 인텐트 보냄 - user(location, address, goo, user_id, login)
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

                // 인텐트 보냄 - user(location, address, goo, user_id, login)
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

        // 주소 정보. 앞에 국가, 시를 빼고 구부터 표시하도록 스트링 파싱.
        locationText = (TextView) findViewById(R.id.locationText);

        String[] parsedAddress = user.getAddress().split(" ");
        String shortedAddress = "";
        for(int i=2; i<parsedAddress.length; i++){
            shortedAddress += parsedAddress[i] + " ";
        }

        locationText.setText(shortedAddress);

        // 글을 표시할 리스트뷰
        listView = (ListView) findViewById(R.id.listView);

        // 현재 구와 글들의 구와 비교 후 글 리스트 가져옴
        getList("goo");  // 처음 생성시 지역 검색으로 설정

        // 리스트 선택시
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 인텐트 보냄 - user(location, address, goo, user_id, login) / article_id
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

    // 현재 구와 글들의 구와 비교 후 글 리스트 가져옴
    public void getList(String what){
        // 리스트 아이템 초기화
        final String fwhat = what;  // 매개변수라 final로 바꿈. goo(지역)인지 all(전체) 검색인지 구별
        items = new ArrayList<ListData>();

        // DB(articles) 접속 - 현재 구와 글들의 구와 비교 후 가져옴
        articleRef = db.getReference().child("articles");
        articleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();

                while(child.hasNext()){
                    String article_id = child.next().getKey();

                    // DB 구조상 articles_count랑 articles_number 제외
                    if(article_id.equals("articles_count") || article_id.equals("articles_number")){
                        continue;
                    }
                    else{
                        String articleGoo = dataSnapshot.child(article_id).child("goo").getValue(String.class);

                        // 지역 검색
                        if(fwhat == "goo") {
                            // 현재 구와 해당 글의 구가 같을 때 그 정보를 가져옴
                            if (user.getGoo().equals(articleGoo)) {
                                String title = dataSnapshot.child(article_id).child("title").getValue(String.class);

                                // 첫번째 이미지 이름 가져와서 데이터 리스트에 추가
                                imageName = dataSnapshot.child(article_id).child("image_names").child("0").getValue(String.class);
                                ListData item = new ListData(imageName, article_id, title);
                                items.add(item);
                            } else continue;
                        }
                        // 전체 검색
                        else if(fwhat == "all"){
                            String title = dataSnapshot.child(article_id).child("title").getValue(String.class);

                            // 마찬가지
                            imageName = dataSnapshot.child(article_id).child("image_names").child("0").getValue(String.class);
                            ListData item = new ListData(imageName, article_id, title);
                            items.add(item);
                        }
                    }
                }
                // 어뎁터에 데이터 리스트 연결
                adapter = new CustomListAdapter(items);
                listView.setAdapter(adapter);

                return;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // 플로팅 액션 버튼 애니메이션 내용
    public void anim(){
        if(isFabOpen){
            // fab버튼일때, 안의 버튼들 비활성화
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
            // fab버튼 클릭되었을 때, 안의 버튼들 활성화
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
        return super.onSupportNavigateUp();

    }
}