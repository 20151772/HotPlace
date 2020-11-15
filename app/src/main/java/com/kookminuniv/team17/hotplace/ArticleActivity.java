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

    public String title, contents, time, user_id;
    public Double longitude, latitude;
    int imageCount;
    public ArrayList<String> imageNames;
    public ArrayList<Uri> filePath;
    ArrayList<ImgData> list;
    File cacheFolder;

    ViewPager2 vp2;
    TextView user_idText, timeText, titleText, contentsText, noimgText;
    ImageButton locationBtn;
    Button imageBtn;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference articlesRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    String article_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Intent intent = getIntent();
        if(intent != null){
            user = (UserInformation)intent.getSerializableExtra("UserObject");
            article_id = intent.getStringExtra("article_id");
        }

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        vp2 = (ViewPager2) findViewById(R.id.vp2);
        user_idText = (TextView) findViewById(R.id.user_idText);
        timeText = (TextView) findViewById(R.id.timeText);
        titleText = (TextView) findViewById(R.id.titleText);
        contentsText = (TextView) findViewById(R.id.contentsText);
        noimgText = (TextView) findViewById(R.id.noimgText);
        locationBtn = (ImageButton) findViewById(R.id.locationBtn);
        imageBtn = (Button) findViewById(R.id.imageBtn);

        imageNames = new ArrayList<String>();
        filePath = new ArrayList<Uri>();
        list = new ArrayList<>();

        storageRef = storage.getReference();
        cacheFolder = new File(getApplicationContext().getCacheDir().toString() + "/images");
        if(!cacheFolder.exists()){
            cacheFolder.mkdir();
        }
        getInfoFromDB();

        // 위치 정보 버튼
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageCount != 0) {
                    File[] imageFileList = cacheFolder.listFiles();
                    for (int j = 0; j < imageFileList.length; j++) {
                        Log.d("fuck", imageFileList[j].toString());
                        Bitmap bmap = BitmapFactory.decodeFile(imageFileList[j].getAbsolutePath());
                        list.add(new ImgData(bmap));
                    }
                    vp2.setAdapter(new PhotoAdapter(list));
                }
                else{
                    noimgText.setVisibility(View.VISIBLE);
                }
                imageBtn.setVisibility(View.GONE);
            }
        });
    }

    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    // articles db에 접근해서 텍스트 정보를 받아오고 뿌려줌
    public void getInfoFromDB(){
        articlesRef = db.getReference().child("articles");
        articlesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 정보 받아와서
                user_id = dataSnapshot.child(article_id).child("user_id").getValue(String.class);
                title = dataSnapshot.child(article_id).child("title").getValue(String.class);
                contents = dataSnapshot.child(article_id).child("contents").getValue(String.class);
                time = dataSnapshot.child(article_id).child("time").getValue(String.class);
                latitude = dataSnapshot.child(article_id).child("location_Latitude").getValue(Double.class);
                longitude = dataSnapshot.child(article_id).child("location_Longitude").getValue(Double.class);
                imageCount = dataSnapshot.child(article_id).child("image_count").getValue(int.class);

                // 텍스트 정보 입력
                user_idText.setText(user_id);
                titleText.setText(title);
                contentsText.setText(contents);
                timeText.setText(time);

                for (int i = 0; i < imageCount; i++) {
                    final int finalI = i;
                    String strI = Integer.toString(i);
                    DatabaseReference imageDRef = articlesRef.child(article_id).child("image_names").child(strI);

                    imageDRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String tmp = dataSnapshot.getValue(String.class);
                            imageNames.add(finalI, tmp);

                            // 그때마다 이미지 저장
                            FirebaseUser fbuser = mAuth.getCurrentUser();
                            if(fbuser != null) {
                                Log.d("asdf", "1");
                                StorageReference imageSRef = storageRef.child(imageNames.get(finalI));

                                try {
                                    Log.d("asdf", "2");
                                    Log.d("asdf", imageNames.get(finalI));
                                    File localFile = File.createTempFile("images", ".png", cacheFolder);
                                    imageSRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Log.d("asdf", "good");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("asdf", "fuck");
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else{
                                signInAnonymously();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CacheClear cc = new CacheClear();
        cc.clearCache(this);
    }

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
}