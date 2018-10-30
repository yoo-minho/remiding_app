package com.reminding;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    //뷰페이져 요소
    ExtendedViewPager viewPager;
    MyViewPagerAdapter adapter;

    //쉐어드프리퍼런스 요소
    SharedPreferences lp;
    SharedPreferences.Editor lEdit;
    SharedPreferences dp;
    SharedPreferences.Editor dEdit;

    //메모에서 넘어온 요소
    Bundle bundle = null;

    //----------------------------------------------------------------------------------------------

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 화면 연결
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 쉐어드프리퍼런스 연결
        lp = getSharedPreferences("login",MODE_PRIVATE);
        lEdit = lp.edit();
        dp = getSharedPreferences("data", MODE_PRIVATE);
        dEdit = dp.edit();

        //번들, 체크키 활용 로그인 화면 제어
        bundle = getIntent().getExtras();
        //번들값 있을때 ( = 메모에서 왔을 때 )
        if( bundle != null ){
            if ( lp.getBoolean("memo_key",false)){ //메모에서 온 번들이면
                lEdit.putBoolean("memo_key", false);
                lEdit.apply();
            }
        }

        //뷰페이저 연결
        viewPager = findViewById(R.id.view_pager);
        adapter = new MyViewPagerAdapter(getSupportFragmentManager(), bundle);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

    }

    //----------------------------------------------------------------------------------------------

    //백버튼 설정
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    @Override
    public void onBackPressed() {
        //프래그먼트에서 넘어온 상수
        Boolean fragment_delete_state = dp.getBoolean("delete_state",false);
        if ( !fragment_delete_state ) {
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPressedTime;
            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                overridePendingTransition(0,0);
                super.onBackPressed();
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            dEdit.putBoolean("delete_state",false);
            dEdit.commit();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        }
    }

}
