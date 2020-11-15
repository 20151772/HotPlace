package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Array;
import java.sql.Ref;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ArticleWriteActivity extends AppCompatActivity {
    UserInformation user;

    final int PICTURE_REQUEST_CODE = 100;

    int articles_count;
    ArrayList<String> imageNames;
    ArrayList<Uri> filePath;

    ImageView imageView;
    EditText titleText, contentsText;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference dbRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_write);

        Intent intent = getIntent();
        if(intent != null){
            user = (UserInformation)intent.getSerializableExtra("UserObject");
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("글쓰기");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        imageView = (ImageView) findViewById(R.id.imageView);
        contentsText = (EditText) findViewById(R.id.contentsText);
        titleText = (EditText) findViewById(R.id.titleText);

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

        currentLocation = getLocation();

        // 이미지 다중 선택
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICTURE_REQUEST_CODE);
            }
        });
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
                    filePath = new ArrayList<Uri>();

                    for(int i=0; i<clipData.getItemCount(); i++){
                        filePath.add(clipData.getItemAt(i).getUri());
                    }

                    imageView.setImageURI(filePath.get(0));
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
                if(TextUtils.isEmpty(titleText.getText())){
                    Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                if(TextUtils.isEmpty(contentsText.getText())){
                    Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    saveContents();

                    Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                    intent.putExtra("UserObject", user);
                    intent.putExtra("article_id", Integer.toString(articles_count));

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(this, "저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                    startActivity(intent);
                    finish();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveContents(){
        String title = titleText.getText().toString();
        String contents = contentsText.getText().toString();
        String strAC = Integer.toString(articles_count);
        imageNames = new ArrayList<String>();

        // 글 db에 저장
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formatDate = sdfNow.format(date);

        dbRef.child(strAC).child("time").setValue(formatDate);  // 글 쓴 시간
        dbRef.child(strAC).child("title").setValue(title);  // 제목
        dbRef.child(strAC).child("contents").setValue(contents);  // 글 내용
        dbRef.child(strAC).child("user_id").setValue(user.getUser_id());  // 글쓴이
        dbRef.child(strAC).child("location_Latitude").setValue(currentLocation.getLatitude());  // 위도
        dbRef.child(strAC).child("location_Longitude").setValue(currentLocation.getLongitude());  // 경도
        dbRef.child("articles_count").setValue(articles_count+1);  // 글 총 갯수 증가

        // 이미지 스토리지에 저장
        if(filePath != null){
            FirebaseStorage storage = FirebaseStorage.getInstance();

            for(int i=0; i<filePath.size(); i++){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
                String strI = Integer.toString(i);
                String fileName = sdf.format(new Date()) + strI + ".png";
                String totalPath = strAC + "/" + fileName;
                imageNames.add(totalPath);
                FirebaseUser fbuser = mAuth.getCurrentUser();
                if(fbuser != null) {
                    StorageReference imageRef = storage.getReference().child(totalPath);
                    imageRef.putFile(filePath.get(i));
                } else{
                    signInAnonymously();
                }
            }
            dbRef.child(strAC).child("image_count").setValue(imageNames.size());
        }
        else{
            imageNames.add("None");
            dbRef.child(strAC).child("image_count").setValue(0);
        }
        dbRef.child(strAC).child("image_names").setValue(imageNames);
    }

    private Location getLocation() {
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한에 동의해주세요.", Toast.LENGTH_LONG).show();
            finish();
        }
        Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return userLocation;
    }

    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
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