// SignupActivity
// 기능 : 회원가입 기능. 파이어베이스 db와 연결해서 아이디와 비밀번호 값 추가.
// 개발 : 김명호

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class SignupActivity extends AppCompatActivity {

    EditText userIdText, passwordText, passwordCheckText;
    Button signupBtn;

    FirebaseDatabase db = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 액션 바
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("회원가입");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 뒤로가기

        userIdText = findViewById(R.id.userIdText);
        passwordText = findViewById(R.id.passwordText);
        passwordCheckText = findViewById(R.id.passwordCheckText);
        signupBtn = findViewById(R.id.signupBtn);

        // 회원가입 버튼 클릭
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_id = userIdText.getText().toString().trim();
                final String password = passwordText.getText().toString().trim();
                String passwordCheck = passwordCheckText.getText().toString().trim();

                // 아이디 혹은 비밀번호 empty 체크
                if(user_id.equals("") || password.equals("")){
                    Toast.makeText(getApplicationContext(), "빈 칸을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 아이디 혹은 비밀번호 space 체크
                if(user_id.contains(" ") || password.contains(" ")){
                    Toast.makeText(getApplicationContext(), "띄어쓰기가 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 일치 체크
                if(password.equals(passwordCheck)){
                    final DatabaseReference dbRef = db.getReference().child("users");
                    // DB(users) 접속
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener(){
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                            while(child.hasNext()){
                                // 아이디 중복 체크
                                if(user_id.equals(child.next().getKey())){
                                    Toast.makeText(getApplicationContext(), "존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
                                    dbRef.removeEventListener(this);
                                    return;
                                }
                            }
                            // 존재하지 않다면,
                            // 가입 시간 포맷
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            String formatDate = sdfNow.format(date);

                            // 아이디와 비밀번호, 가입시간 추가
                            dbRef.child(user_id).child("user_id").setValue(user_id);
                            dbRef.child(user_id).child("password").setValue(password);
                            dbRef.child(user_id).child("signup_date").setValue(formatDate);
                            Toast.makeText(SignupActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
                else{
                    Toast.makeText(SignupActivity.this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    // 액션 바 뒤로가기
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}