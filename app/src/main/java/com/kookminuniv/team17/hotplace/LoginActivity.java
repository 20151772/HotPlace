// LoginActivity
// 기능 : 로그인 화면. 파이어베이스 db와 연동해서 아이디와 비밀번호 정보 확인 후 로그인. 회원가입 버튼.
// 개발 : 김명호

package com.kookminuniv.team17.hotplace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {
    private MainBackPressCloseHandler mainBackPressCloseHandler;
    UserInformation user;

    EditText userIdText, passwordText;
    Button signupBtn, loginBtn;

    FirebaseDatabase db = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 인텐트 받아옴 - user(location, address, goo)
        Intent intent = getIntent();
        if(intent != null){
            user = (UserInformation)intent.getSerializableExtra("UserObject");
        }

        // 액션 바
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("로그인");
        actionBar.setDisplayHomeAsUpEnabled(true);  // 액션 바 뒤로가기

        userIdText = findViewById(R.id.userIdText);
        passwordText = findViewById(R.id.passwordText);
        signupBtn = findViewById(R.id.signupBtn);
        loginBtn = findViewById(R.id.loginBtn);

        // 회원가입 버튼
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 버튼
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user_id = userIdText.getText().toString().trim();
                final String password = passwordText.getText().toString().trim();

                // 아이디, 비밀번호 empty 체크
                if(user_id.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(), "입력 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 아이디, 비밀번호 space 체크
                if(user_id.contains(" ") || password.contains(" ")){
                    Toast.makeText(getApplicationContext(), "띄어쓰기가 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // DB(users) 접속 - 아이디 및 비밀번호 체크
                final DatabaseReference dbRef = db.getReference().child("users");
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                        while(child.hasNext()){
                            // DB에 존재하는 아이디 일때,
                            if(user_id.equals(child.next().getKey())){
                                // 그 아이디의 비밀번호를 가져오고,
                                String correctPassword = dataSnapshot.child(user_id).child("password").getValue(String.class);
                                // 입력된 비밀번호와 비교
                                if(password.equals(correctPassword)){
                                    // user에 uesr_id와 login 됬음을 저장함
                                    user.setUser_id(user_id);
                                    user.setLogin(true);

                                    // 못 받아왔으면 리턴
                                    if(user.getUser_id() == null){
                                        Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // 인텐트 보냄 - user(location, address, goo, user_id, login)
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra("UserObject", user);
                                    Toast.makeText(getApplicationContext(), "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                        Toast.makeText(getApplicationContext(), "등록된 아이디가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });

        // 뒤로가기 버튼
        mainBackPressCloseHandler = new MainBackPressCloseHandler(this);
    }

    // 뒤로가기
    @Override
    public void onBackPressed() {
        mainBackPressCloseHandler.onBackPressed();
    }
    // 액션 바 뒤로가기
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}