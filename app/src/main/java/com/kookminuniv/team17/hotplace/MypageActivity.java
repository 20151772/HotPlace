package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MypageActivity extends AppCompatActivity {
    UserInformation user;

    TextView useridText, dateText;
    Button deleteBtn;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

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

        useridText = (TextView) findViewById(R.id.userIdText);
        dateText = (TextView) findViewById(R.id.dateText);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);

        useridText.setText(user.getUser_id());

        // user_id로 가입일 가져옴
        usersRef = db.getReference().child("users");
        usersRef.child(user.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String date = dataSnapshot.child("signup_date").getValue(String.class);
                dateText.setText(date);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // 계정 삭제 버튼
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(user.getUser_id()).removeValue();

                Toast.makeText(getApplicationContext(), "계정이 삭제되었습니다.", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), LogoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // 액션 바 뒤로가기
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();

    }
}