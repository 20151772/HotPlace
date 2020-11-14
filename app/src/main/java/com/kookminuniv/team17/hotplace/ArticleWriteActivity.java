package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.sql.Ref;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArticleWriteActivity extends AppCompatActivity {

    final int PICTURE_REQUEST_CODE = 100;

    int articles_count;

    ImageView imageView;
    EditText titleText, contentsText;
    Uri[] filePath;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference dbRef;

    //+ 로그인된 아이디
    String user_id = "test_account";
    String location = "Korea";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_write);

        dbRef = db.getReference().child("articles");
        // 글 갯수 가져옴
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                articles_count = dataSnapshot.child("articles_count").getValue(int.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("글쓰기");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        // 이미지 다중 선택
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICTURE_REQUEST_CODE);
            }
        });

        contentsText = (EditText) findViewById(R.id.contentsText);
        titleText = (EditText) findViewById(R.id.titleText);
    }

    // 갤러리 접근 후 이미지 불러옴
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICTURE_REQUEST_CODE){
            if(data == null){}
            else{
                Uri singlePath = data.getData();
                ClipData clipData = data.getClipData();

                if(clipData != null){
                    filePath = new Uri[clipData.getItemCount()];
                    for(int i=0; i<clipData.getItemCount(); i++){
                        filePath[i] = clipData.getItemAt(i).getUri();
                    }

                    imageView.setImageURI(filePath[0]);
                }

                imageView.setImageURI(singlePath);  // 실제로 표시되는건 첫번째 이미지만
            }
        }
    }

    // 액션바 메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.article_write_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.completeBtn:
                saveContents();
                Toast.makeText(this, "저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                startActivity(intent);
                //return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveContents(){
        String title = titleText.getText().toString();
        String contents = contentsText.getText().toString();
        String strAC = Integer.toString(articles_count);

        // 글 db에 저장
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formatDate = sdfNow.format(date);

        dbRef.child(strAC).child("time").setValue(formatDate);  // 글 쓴 시간
        dbRef.child(strAC).child("title").setValue(title);  // 제목
        dbRef.child(strAC).child("contents").setValue(contents);  // 글 내용
        dbRef.child(strAC).child("user_id").setValue(user_id);  // 글쓴이
        dbRef.child(strAC).child("location").setValue(location);  // 위치
        dbRef.child("articles_count").setValue(articles_count+1);  // 글 총 갯수 증가

        // 이미지 스토리지에 저장
        if(filePath != null){
            FirebaseStorage storage = FirebaseStorage.getInstance();

            for(int i=0; i<filePath.length; i++){
                String totalPath = strAC + "/" + filePath[i].getLastPathSegment();
                StorageReference imageRef = storage.getReference().child(totalPath);
                imageRef.putFile(filePath[i]);
                /*.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "업로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
        }
    }

    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}