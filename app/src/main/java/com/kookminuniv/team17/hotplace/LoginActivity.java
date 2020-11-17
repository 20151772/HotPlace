// LoginActivity: 로그인 기능
package com.kookminuniv.team17.hotplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {

    private MainBackPressCloseHandler mainBackPressCloseHandler;
    UserInformation user;

    EditText userIdText, passwordText, passwordCheckText;
    Button signupBtn, loginBtn;

    FirebaseDatabase db = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        if(intent != null){
            user = (UserInformation)intent.getSerializableExtra("UserObject");
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("로그인");

        userIdText = findViewById(R.id.userIdText);
        passwordText = findViewById(R.id.passwordText);
        passwordCheckText = findViewById(R.id.passwordCheckText);
        signupBtn = findViewById(R.id.signupBtn);
        loginBtn = findViewById(R.id.loginBtn);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user_id = userIdText.getText().toString().trim();
                final String password = passwordText.getText().toString().trim();

                // 아이디 혹은 비밀번호 empty, space 체크
                if(user_id.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(), "입력 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(user_id.contains(" ") || password.contains(" ")){
                    Toast.makeText(getApplicationContext(), "띄어쓰기가 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                final DatabaseReference dbRef = db.getReference().child("users");
                // 아이디 일치 체크
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                        while(child.hasNext()){
                            if(user_id.equals(child.next().getKey())){
                                // 비밀번호 일치 체크
                                String correctPassword = dataSnapshot.child(user_id).child("password").getValue(String.class);
                                if(password.equals(correctPassword)){
                                    //+ 로그인 정보 저장
                                    // 임시로 바로 글로 전환
                                    user.setUser_id(user_id);
                                    user.setLogin(true);

                                    Intent intent = new Intent(getApplicationContext(), ListViewActivity.class);
                                    intent.putExtra("UserObject", user);

                                    Toast.makeText(getApplicationContext(), "로그인 되었습니다.", Toast.LENGTH_SHORT).show();

                                    startActivity(intent);
                                    finish();
                                    return;
                                    //+ 로그인 액티비티 종료
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
        mainBackPressCloseHandler = new MainBackPressCloseHandler(this);
    }

    @Override
    public void onBackPressed() {
        mainBackPressCloseHandler.onBackPressed();
    }
}