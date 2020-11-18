// ArticleWriteActivity : 글 쓰기 구현

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ArticleWriteActivity extends AppCompatActivity {
    UserInformation user;

    final int PICTURE_REQUEST_CODE = 100;

    int articles_count, articles_number;
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

        // 인텐트 받아옴 - user(location, user_id, login)
        Intent intent = getIntent();
        if(intent != null){
            user = (UserInformation)intent.getSerializableExtra("UserObject");
            Log.d("AWA : user_id", user.getUser_id());
        }

        // 액션 바
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("글쓰기");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        imageView = (ImageView) findViewById(R.id.iv);
        contentsText = (EditText) findViewById(R.id.contentsText);
        titleText = (EditText) findViewById(R.id.titleText);

        // DB 접속 - 전체 글 갯수를 가져옴
        dbRef = db.getReference().child("articles");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                articles_count = dataSnapshot.child("articles_count").getValue(int.class);
                articles_number = dataSnapshot.child("articles_number").getValue(int.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // 현재 위치를 받아옴
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

    // 이미지 다중 선택 후 불러옴
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

                imageView.setImageURI(singlePath);  // 화면에 실제로 표시되는건 첫번째 이미지만
            }
        }
    }

    // 액션바 메뉴
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.article_write_menu, menu);
        return true;
    }

    // 액션바 메뉴 선택
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            // 완료 버튼일 때,
            case R.id.completeBtn:
                // 제목이 없음
                if(TextUtils.isEmpty(titleText.getText())){
                    Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                // 내용이 없음
                if(TextUtils.isEmpty(contentsText.getText())){
                    Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                // 제목과 내용이 있을 때,
                else {
                    // 제목, 내용, 사진 저장
                    saveContents();

                    // 인텐트 보냄 - user(location, user_id, login) / article_id
                    Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                    intent.putExtra("UserObject", user);
                    intent.putExtra("article_id", Integer.toString(articles_number));

                    // 스토리지에 저장하는 것을 기다림 - 바로 화면 전환을 하면 저장하기도 전에 로딩되서 에러 발생
                    try {
                        Thread.sleep(1000);
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

    // 제목, 내용, 사진 저장
    public void saveContents(){
        // 제목, 내용 가져옴
        String title = titleText.getText().toString();
        String contents = contentsText.getText().toString();
        String strAC = Integer.toString(articles_number);
        imageNames = new ArrayList<String>();

        // 현재 시간 포맷
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formatDate = sdfNow.format(date);

        // 현재 위도경도 -> 주소정보 변환 후 가져옴
        String address = getAddressFromLocation(currentLocation);
        String[] parsedAddress = address.split(" ");
        String goo = parsedAddress[2];

        // DB(articles)에 저장
        dbRef.child(strAC).child("time").setValue(formatDate);  // 글 쓴 시간
        dbRef.child(strAC).child("title").setValue(title);  // 글 제목
        dbRef.child(strAC).child("contents").setValue(contents);  // 글 내용
        dbRef.child(strAC).child("user_id").setValue(user.getUser_id());  // 글쓴이
        dbRef.child(strAC).child("location_Latitude").setValue(currentLocation.getLatitude());  // 위도
        dbRef.child(strAC).child("location_Longitude").setValue(currentLocation.getLongitude());  // 경도
        dbRef.child(strAC).child("address").setValue(address);  // 주소
        dbRef.child(strAC).child("goo").setValue(goo);  // 구
        dbRef.child("articles_number").setValue(articles_number+1);  // 글 넘버 증가
        dbRef.child("articles_count").setValue(articles_count+1);  // 글 총 갯수 증가

        // 이미지 파일이 있을 때
        if(filePath != null){
            // 스토리지 접근 - 이미지 업로드
            FirebaseStorage storage = FirebaseStorage.getInstance();

            for(int i=0; i<filePath.size(); i++){
                // 저장될 이미지 주소 : 글 번호 + / +  현재 시간 포맷
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
                String strI = Integer.toString(i);
                String fileName = sdf.format(new Date()) + strI + ".png";
                String totalPath = strAC + "/" + fileName;
                imageNames.add(totalPath);

                // 스토리지 접근 전 singin
                FirebaseUser fbuser = mAuth.getCurrentUser();
                if(fbuser != null) {
                    StorageReference imageRef = storage.getReference().child(totalPath);
                    imageRef.putFile(filePath.get(i));
                } else{
                    signInAnonymously();
                }
            }
            // DB(articles)에 이미지 갯수 저장
            dbRef.child(strAC).child("image_count").setValue(imageNames.size());
        }
        // 이미지 파일 없을 때
        else{
            imageNames.add("None");
            dbRef.child(strAC).child("image_count").setValue(0);
        }
        // 이미지 이름 저장
        dbRef.child(strAC).child("image_names").setValue(imageNames);
    }

    // 현재 위치를 받아옴
    private Location getLocation() {
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한에 동의해주세요.", Toast.LENGTH_LONG).show();
            finish();
        }
        Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return userLocation;
    }

    // 현재 위도경도 -> 주소정보 변환 후 가져옴
    private String getAddressFromLocation(Location loc) {
        Geocoder gc = new Geocoder(this);
        List<Address> list = null;
        try {
            list = gc.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list != null){
            if(list.size() == 0) return "";
            else return list.get(0).getAddressLine(0);
        }
        return "";
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
    // 뒤로가기
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("UserObj", user);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}