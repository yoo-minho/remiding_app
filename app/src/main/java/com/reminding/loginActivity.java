package com.reminding;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class loginActivity extends AppCompatActivity{

    //가이드 뷰페이져
    ViewPager viewPager;

    //로딩다이얼로그
    ProgressDialog progressDialog;

    //권한 선언
    private String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 콜백 함수에 쓰일 변수수

    //쉐어드프리퍼런스 : 체크키(거짓)
    SharedPreferences lp;
    SharedPreferences.Editor lEdit;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //권한 묻기
        checkPermissions();

        //시작해보기
        Button noSignButton = findViewById(R.id.login_button3);
        noSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //시작키 저장
                lEdit.putBoolean("login_key",true);
                lEdit.commit();

                //메인으로 돌아감
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //쉐어드프리퍼런스 연결
        lp = getSharedPreferences("login",MODE_PRIVATE);
        lEdit = lp.edit();

        // 처음켤때 시작키가 있으면 이 화면을 생략함 -> 나중에 로그인창으로 활용 가능
        if ( lp.getBoolean("login_key", false) ){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        //로딩다이얼로그
        progressDialog = new ProgressDialog(this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("잠시만 기다려 주세요");

        //뷰페이져 연결
        viewPager = findViewById(R.id.pager);
        MyGuideViewPagerAdapter adapter = new MyGuideViewPagerAdapter(getApplicationContext());
        viewPager.setAdapter(adapter);

    }

    //----------------------------------------------------------------------------------------------

    //백버튼 설정
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;
        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //----------------------------------------------------------------------------------------------

    //권한체크
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>(); //권한 배열리스트 하나 만든다
        for (String pm : permissions) { // 세 권한 다 따져봐
            result = ContextCompat.checkSelfPermission(this, pm); //셀프로 권한 체크해본 결과를 담아서
            if (result != PackageManager.PERMISSION_GRANTED) { //부여된 권한과 비교해서 없으면
                permissionList.add(pm); //리스트에 추가하자.
            }
        }
        if (!permissionList.isEmpty()) { //부여 권한이 아닌 애들이 존재한다면
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]),
                    MULTIPLE_PERMISSIONS); //사용자에게 리스트의 권한 요청 페이지를 차례대로 요청하자 + 콜백할 때 멀티플 퍼미션 변수 쓰자.
            return false; // 권한 요청할게 있네
        }
        return true; // 권한 다 충족하네
    }

    //----------------------------------------------------------------------------------------------

    //권한요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) { //동의했든 안했든 권한이 1개라도 있다면
                    for (int i = 0; i < permissions.length; i++) { //그 권한 개수만큼 따져보자
                        if (permissions[i].equals(this.permissions[0])) { //저장소읽기 권한이랑 같은 권한이 있을때
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) { //부여된 권한이 아니라면
                                showNoPermissionToastAndFinish(); //토스트 보여주고 꺼버려
                            }
                        } else if (permissions[i].equals(this.permissions[1])) { //저장소쓰기 권한이랑 같은 권한이 있을때
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) { //부여된 권한이 아니라면
                                showNoPermissionToastAndFinish(); //토스트 보여주고 꺼버려
                            }
                        } else if (permissions[i].equals(this.permissions[2])) { //저장소쓰기 권한이랑 같은 권한이 있을때
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) { //부여된 권한이 아니라면
                                showNoPermissionToastAndFinish(); //토스트 보여주고 꺼버려
                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish(); //토스트 보여주고 꺼버려
                }
                return;
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    //권한토스트
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용가능합니다. 설정에서 권한을 허용하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

}
