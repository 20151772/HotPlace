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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private MainBackPressCloseHandler mainBackPressCloseHandler;
    UserInformation user;
    CacheClear cc;

    String selectedArticleId;  // 선택된 글 아이디
    String imageName;

    SwipeRefreshLayout swipeLayout;
    ListView listView;
    CustomListAdapter adapter;

    ImageButton createBtn, refreshBtn;
    Button mypageBtn;
    TextView locationText;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference articleRef;
    //StorageReference storageRef = FirebaseStorage.getInstance().getReference();

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
        actionBar.setTitle("메인");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        // 캐쉬 클리어
        cc = new CacheClear();
        cc.clearCache(MainActivity.this);

        createBtn = (ImageButton) findViewById(R.id.createBtn);
        refreshBtn = (ImageButton) findViewById(R.id.refreshBtn);
        mypageBtn = (Button) findViewById(R.id.mypageBtn);
        locationText = (TextView) findViewById(R.id.locationText);

        locationText.setText(user.getAddress());

        adapter = new CustomListAdapter();

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

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
                        // 현재 구와 해당 글의 구가 같을 때 그 정보를 가져옴
                        if(user.getGoo().equals(articleGoo)){
                            // 이미지 카운트를 불러와
                            int imageCount = dataSnapshot.child(article_id).child("image_count").getValue(int.class);
                            // 이미지가 1개 이상이면 첫번째 이미지를 가져옴
                            if(imageCount > 0) {
                                Log.d("asdf", "0");
                                imageName = dataSnapshot.child(article_id).child("image_names").child("0").getValue(String.class);
                                String title = dataSnapshot.child(article_id).child("title").getValue(String.class);
                                adapter.addItem(imageName, article_id, title);
                            }
                        }
                        else continue;
                    }
                    listView.setAdapter(adapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // 글쓰기 버튼
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 인텐트 보냄 - user(location, user_id, login)
                cc.clearCache(MainActivity.this);
                Intent intent = new Intent(getApplicationContext(), ArticleWriteActivity.class);
                intent.putExtra("UserObject", user);
                startActivity(intent);
            }
        });

        // 새로고침 버튼
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cc.clearCache(MainActivity.this);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("UserObject", user);
                startActivity(intent);
            }
        });

        // 마이페이지 버튼
        mypageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cc.clearCache(MainActivity.this);
                Intent intent = new Intent(getApplicationContext(), MypageActivity.class);
                intent.putExtra("UserObject", user);
                startActivity(intent);
            }
        });

        //swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        //swipeLayout.setOnRefreshListener(getApplicationContext());

        // 뒤로가기 버튼
        mainBackPressCloseHandler = new MainBackPressCloseHandler(this);
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