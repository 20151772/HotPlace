// MainActivity: 프래그먼트로 홈과 마이페이지 구현
// 홈: 글 리스트(사진, 제목, 위치)와 글쓰기 버튼
// 마이페이지: 아이디, 위치, 글 쓴 갯수 정도

package com.kookminuniv.team17.hotplace;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("text");

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Activity");

    }
}

    /*
    protected void onStart() {
        super.onStart();

        conditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                textView1.setText(text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                conditionRef.setValue(editText1.getText().toString());
            }
        });
    }
     */
