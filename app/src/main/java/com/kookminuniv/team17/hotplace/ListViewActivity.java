package com.kookminuniv.team17.hotplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ListViewActivity extends AppCompatActivity {

    private MainBackPressCloseHandler mainBackPressCloseHandler;
    Button myPage_btn, write_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        myPage_btn = findViewById(R.id.myPage_btn);
        write_btn = findViewById(R.id.write_btn);

        myPage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ListViewActivity.this, myPage.class);
                startActivity(intent1);

            }
        });


        write_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(ListViewActivity.this, ArticleWriteActivity.class);
                startActivity(intent2);

            }
        });



        ListView listview ;
        ListAdapter adapter;

        // Adapter 생성
        adapter = new ListAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(adapter);

        // 첫 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.baseline_assignment_ind_black_36dp),
                "Box", "Account Box Black 36dp") ;
        // 두 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.baseline_account_circle_black_36dp),
                "Circle", "Account Circle Black 36dp") ;
        // 세 번째 아이템 추가.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.baseline_account_box_black_36dp),
                "Ind", "Assignment Ind Black 36dp") ;

        mainBackPressCloseHandler = new MainBackPressCloseHandler(this);
    }

    @Override
    public void onBackPressed() {
        mainBackPressCloseHandler.onBackPressed();
    }
}
