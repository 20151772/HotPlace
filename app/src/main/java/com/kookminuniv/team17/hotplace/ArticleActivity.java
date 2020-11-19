// ArticleActivity: 글 화면. 사진뷰, 글쓴이, 시간, 제목, 내용, 위치 버튼

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ArticleActivity extends AppCompatActivity {
    UserInformation user;
    CacheClear cc;

    public String title, contents, time, user_id, address, article_id;
    public Double longitude, latitude;
    int imageCount, articles_count;
    public ArrayList<String> imageNames;
    public ArrayList<Uri> filePath;
    ArrayList<ImgData> items = null;
    File cacheFolder;

    ViewPager2 vp2;
    TextView user_idText, timeText, titleText, contentsText, noimgText, addressText;
    ImageButton locationBtn;
    Button deleteBtn;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference articleRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        // 인텐트 받아옴 - user(location, user_id, login) / article_id
        Intent intent = getIntent();
        if(intent != null){
            user = (UserInformation)intent.getSerializableExtra("UserObject");
            article_id = intent.getStringExtra("article_id");
            Log.d("AA : user_id", user.getUser_id());
            Log.d("AA : article_id", article_id);
        }

        // 액션 바
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("글");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        // 캐쉬 클리어
        cc = new CacheClear();
        cc.clearCache(this);

        vp2 = (ViewPager2) findViewById(R.id.vp2);
        user_idText = (TextView) findViewById(R.id.user_idText);
        timeText = (TextView) findViewById(R.id.timeText);
        titleText = (TextView) findViewById(R.id.titleText);
        contentsText = (TextView) findViewById(R.id.contentsText);
        addressText = (TextView) findViewById(R.id.addressText);
        locationBtn = (ImageButton) findViewById(R.id.locationBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);

        imageNames = new ArrayList<String>();
        filePath = new ArrayList<Uri>();

        storageRef = storage.getReference();
        // 캐쉬 폴더(cache/images)
        cacheFolder = new File(getApplicationContext().getCacheDir().toString() + "/images");
        if(!cacheFolder.exists()){
            cacheFolder.mkdir();
        }

        // DB와 스토리지에서 글 정보 가져옴
        getInfoFromDB();

        // 위치 정보 버튼
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 인텐트 넣음 - latitude / longtitude
                Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });

        // 삭제 버튼
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // articles_count 가져옴
                articleRef = db.getReference().child("articles");
                articleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        articles_count = dataSnapshot.child("articles_count").getValue(int.class);
                        articleRef.child("articles_count").setValue(articles_count-1);  // 갯수 하나 감소
                        // 이미지가 있다면 스토리지에 접근해서 삭제
                        for(int i=0; i<imageCount; i++){
                            //String strI = Integer.toString(i);
                            String deleteImgPath = imageNames.get(i);
                            FirebaseUser fbuser = mAuth.getCurrentUser();
                            if(fbuser != null){
                                storageRef.child(deleteImgPath).delete();
                            }
                            else{
                                signInAnonymously();
                            }
                        }

                        // DB article 삭제
                        articleRef.child(article_id).removeValue();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

                Toast.makeText(getApplicationContext(), "해당 글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                cc.clearCache(ArticleActivity.this);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("UserObject", user);
                startActivity(intent);
                finish();
            }
        });
    }

    // DB와 스토리지에서 글 정보 가져옴
    public void getInfoFromDB(){
        items = new ArrayList<>();

        // DB(articles) 접속 - 글 정보 가져옴
        articleRef = db.getReference().child("articles");
        articleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 해당 글이 존재하는지 확인
                Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                while(child.hasNext()){
                    // 존재할 경우
                    if(article_id.equals(child.next().getKey())){
                        user_id = dataSnapshot.child(article_id).child("user_id").getValue(String.class);  // 글쓴이
                        title = dataSnapshot.child(article_id).child("title").getValue(String.class);  // 글 제목
                        contents = dataSnapshot.child(article_id).child("contents").getValue(String.class);  // 글 내용
                        time = dataSnapshot.child(article_id).child("time").getValue(String.class);  // 글 쓴 시간
                        address = dataSnapshot.child(article_id).child("address").getValue(String.class);  // 주소
                        latitude = dataSnapshot.child(article_id).child("location_Latitude").getValue(Double.class);  // 위도
                        longitude = dataSnapshot.child(article_id).child("location_Longitude").getValue(Double.class);  // 경도
                        imageCount = dataSnapshot.child(article_id).child("image_count").getValue(int.class);  // 이미지 갯수

                        // articles의 user_id 와 user의 user_id가 같으면 삭제 가능
                        if(user.getUser_id().equals(user_id)){
                            deleteBtn.setVisibility(View.VISIBLE);
                        }

                        // 주소를 불러와 구부터 시작하도록 파싱
                        String[] parsedAddress = address.split(" ");
                        String shortedAddress = "";
                        for(int i=2; i<parsedAddress.length; i++){
                            shortedAddress += parsedAddress[i] + " ";
                        }

                        // 텍스트 정보 입력
                        user_idText.setText(user_id);
                        titleText.setText(title);
                        contentsText.setText(contents);
                        timeText.setText(time);
                        addressText.setText(shortedAddress);

                        DatabaseReference imageDREf = articleRef.child(article_id).child("image_names");
                        imageDREf.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (imageCount == 0) {
                                    ImgData item = new ImgData("None");
                                    items.add(item);
                                }
                                else {
                                    for (int i = 0; i < imageCount; i++) {
                                        String imageName = dataSnapshot.child(Integer.toString(i)).getValue(String.class);
                                        imageNames.add(imageName);
                                        ImgData item = new ImgData(imageName);
                                        items.add(item);
                                    }
                                }
                                PhotoAdapter adapter = new PhotoAdapter(items);
                                vp2.setAdapter(adapter);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });

                        return;
                    }
                }
                Toast.makeText(getApplicationContext(), "해당 글이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    // 스토리지 접근 전 singin
    private void signInAnonymously(){
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        }) .addOnFailureListener(this, new OnFailureListener() {
            @Override public void onFailure(@NonNull Exception exception) {
                Log.e("TAG", "signInAnonymously:FAILURE", exception);
            }
        });
    }

    // 액션 바 뒤로가기
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        cc.clearCache(this);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("UserObject", user);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    // 종료시 캐쉬 클리어
    @Override
    public void onDestroy() {
        super.onDestroy();
        cc.clearCache(this);
    }
}