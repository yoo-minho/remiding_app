package com.reminding;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class EditActivity extends AppCompatActivity {

    //기본구성요소
    ImageView imageView; //이미지
    ImageView frontView; //검은색 투명
    ImageView memoButton; //메모버튼
    ImageButton editButton; //완료 버튼
    CardView useInfo; //안내 문구

    //에딧텍스트 연동 요소
    EditText nick; // 본문
    TextView nick2; // 본문 텍시트
    String beforeNick; //에딧텍스트 체크

    //활용 요소
    Uri uri;
    int degrees = 0;
    int alpha = 0; //실질투명도와 비례하는 투명 수
    int alphaNumber = 0 ; //실질 투명도

    //로딩다이얼로그
    ProgressDialog progressDialog;

    //메모에서 올 때 활용 추가 요소
    Bundle bundle = new Bundle();
    int key = 0;

    //공유에서 올 때 활용 추가 요소
    String action ;

    //이미지 작업 클래스
    MyImageMaker myImageMaker = new MyImageMaker();

    //----------------------------------------------------------------------------------------------

    //화면 구성
    @SuppressLint({"CheckResult", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //상태바 없앰
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_edit);

        //요소들 연결
        imageView = findViewById(R.id.gallery_imageView);
        frontView = findViewById(R.id.front_color);
        frontView.setImageAlpha(50); //초기 상태 : 50 투명도
        memoButton = findViewById(R.id.memo_button);
        editButton = findViewById(R.id.add_button);
        useInfo = findViewById(R.id.use_info);
        nick = findViewById(R.id.nick_editText);
        nick2 = findViewById(R.id.nick_editText2);
        Intent intent = getIntent(); //겟인텐트를 인텐트로 선언
        bundle = intent.getExtras(); //번들 품
        if( bundle != null ){
            key = bundle.getInt("key"); //번들의 키 값 확인
        }

        //로딩다이얼로그
        progressDialog = new ProgressDialog(this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("잠시만 기다려 주세요");

        //메모버튼
        memoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EditActivity.this, "중앙을 한 번 더 클릭해주세요!", Toast.LENGTH_SHORT).show();
                nick.setVisibility(View.VISIBLE);
                nick2.setVisibility(View.GONE);
            }
        });

        //버튼 제어.
        useInfo.setVisibility(View.VISIBLE);

        //0번. 기본 추가
        //1번. 메모에서 추가
        //2번. 공유를 활용하여 추가
        //3번. 메모 수정
        //4번. 잠금화면에서 메모 추가
        //5번. 광고에서 추가

        // 1번. 메모에서 온 데이터 : 겟인텐트 번들에서 꺼냄
        if (key == 4000){

            //버튼 제어.
            useInfo.setVisibility(View.VISIBLE);

            //데이터 세팅
            nick.setText(bundle.getString("nick"));
            nick2.setText(bundle.getString("nick"));
            nick2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nick.setVisibility(View.VISIBLE);
                    nick2.setVisibility(View.GONE);
                }
            });
            nick.setVisibility(View.GONE);
            nick2.setVisibility(View.VISIBLE);
            uri = bundle.getParcelable("uri");
            degrees = bundle.getInt("degrees");
            frontView.setImageAlpha(50);
            Glide.with(this)
                    .load(myImageMaker.getImage(uri,degrees,800*600))
                    .into(imageView);

        }

        //메모에서 오지 않은 데이터 : 2번. 공유로 왔을때, 3번. 수정으로 왔을때
        else {

            action = intent.getAction();
            String type = intent.getType();

            // 2번. 공유로 왔을때 : 겟인텐트로 꺼냄
            if( Intent.ACTION_SEND.equals(action) ){

                //버튼 제어.
                useInfo.setVisibility(View.VISIBLE);

                //글자가 있다면
                if("text/plain".equals(type)){
                    nick.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
                    nick2.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
                    nick2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            nick.setVisibility(View.VISIBLE);
                            nick2.setVisibility(View.GONE);
                        }
                    });
                    nick.setVisibility(View.GONE);
                    nick2.setVisibility(View.VISIBLE);

                //이미지가 있다면
                } else if (type != null && type.startsWith("image/")) {

                    nick.setVisibility(View.VISIBLE);
                    nick2.setVisibility(View.GONE);
                    //갤러리 경로를 실제경로로 바꿔준다.
                    Uri mediaUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    uri = myImageMaker.getRealPath(getApplicationContext(), mediaUri);
                    //uri 존재한다면
                    if (uri != null) {
                        Glide.with(this)
                                .load(myImageMaker.getImage(uri, degrees, 800 * 600))
                        .into(imageView);
            } else {
                Toast.makeText(this, "이미지 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }}}

    // 0번. 기본 추가, 3번. 수정으로 왔을때, 5번 광고에서 추가, 겟인텐트로 꺼냄
            else {

                //버튼 제어.
                editButton.setVisibility(View.GONE);
                useInfo.setVisibility(View.VISIBLE);

                //5번 광고에서 올때
                if ( intent.getIntExtra("ad_key",0) == 3333 ){
                    editButton.setVisibility(View.VISIBLE);
                    useInfo.setVisibility(View.VISIBLE);
                    nick.setVisibility(View.GONE);
                    nick2.setVisibility(View.VISIBLE);
                    nick.setText(intent.getStringExtra("nick"));
                    nick2.setText(intent.getStringExtra("nick"));
                    nick2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            nick.setVisibility(View.VISIBLE);
                            nick2.setVisibility(View.GONE);
                        }
                    });
                    //3번 수정으로 올때
                } else {
                    //데이터를 활용함
                    nick.setText(intent.getStringExtra("nick"));
                    nick2.setText(intent.getStringExtra("nick"));
                    nick2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            nick.setVisibility(View.VISIBLE);
                            nick2.setVisibility(View.GONE);
                        }
                    });
                    nick.setVisibility(View.GONE);
                    nick2.setVisibility(View.VISIBLE);
                    uri = intent.getData();
                    degrees = intent.getIntExtra("degrees",0);
                    alphaNumber = intent.getIntExtra("alpha",0);
                    frontView.setImageAlpha(alphaNumber);

                    //uri 존재한다면
                    if(uri != null){
                        Glide.with(this)
                                .load(myImageMaker.getImage(uri,degrees,800*600))
                                .into(imageView);
                    }}}

            //에딧버튼 활성화 유무 ( 글을 쓰거나 글이 달라졌을때 )
            beforeNick = nick.getText().toString(); //초기 글
            nick.addTextChangedListener(new TextWatcher() {
                String afterNick;
                @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }
                @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }
                @Override public void afterTextChanged(Editable editable) {
                    //쓰면서 달라지는 글
                    afterNick = nick.getText().toString();
                    if ( afterNick.length() > 0 ){
                        if ( beforeNick.equals(afterNick)){
                            editButton.setVisibility(View.GONE); useInfo.setVisibility(View.VISIBLE);
                        } else {
                            editButton.setVisibility(View.VISIBLE); useInfo.setVisibility(View.GONE);
                        }
                    } else {
                        if ( uri == null ){
                            editButton.setVisibility(View.GONE); useInfo.setVisibility(View.VISIBLE);
                        }
                    }}});
                }
        }

    //----------------------------------------------------------------------------------------------

    //0번. 기본 추가
    //1번. 메모에서 추가
    //2번. 공유를 활용하여 추가
    //3번. 메모 수정
    //4번. 잠금화면에서 메모 추가

    //완료버튼
    public void edit(View view) {

        progressDialog.show();

        //2번. 공유를 활용하여 추가했거나, 잠금화면에서 메모 추가했을 때
        if ( Intent.ACTION_SEND.equals(action) || String.valueOf(getIntent().getIntExtra("screen",0)).equals("101")) {

            //데이터 넣어서 스타트액티비티로 보냄
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("degrees",degrees);
            intent.setData(uri);
            intent.putExtra("nick",nick.getText().toString());
            intent.putExtra("key",101);
            intent.putExtra("alpha",alphaNumber);
            if( uri == null && nick.getText().toString().equals("") ){
                Toast.makeText(this, "저장할 내용이 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                overridePendingTransition(0, 0);
                startActivity(intent);
                finish();
            }}

        //0번. 기본 추가, 1번. 메모에서 추가, 3번. 메모 수정일 때
        else {

            //데이터 넣어서 셋리절트로 보냄
            Intent intent = new Intent();
            intent.putExtra("degrees",degrees);
            intent.setData(uri);
            intent.putExtra("nick",nick.getText().toString());
            intent.putExtra("key",key);
            intent.putExtra("alpha",alphaNumber);
            if( uri == null && nick.getText().toString().equals("") ){
                Toast.makeText(this, "저장할 내용이 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                setResult(RESULT_OK, intent);
                overridePendingTransition(0, 0);
                finish();
            }}
        progressDialog.dismiss();
        editButton.setVisibility(View.VISIBLE);

    }

    //----------------------------------------------------------------------------------------------

    //갤러리 불러옴
    public void gallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, 5000);
    }

    //----------------------------------------------------------------------------------------------

    //갤러리를 받음
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 5000 && resultCode == RESULT_OK && data !=null ){
            //갤러리 사진을 실제 경로화
            Uri mediaUri = data.getData();
            uri = myImageMaker.getRealPath(getApplicationContext(), mediaUri);
            Glide.with(getApplicationContext())
                    .load(myImageMaker.getImage(uri,degrees,800*600))
                    .into(imageView);
            frontView.setImageAlpha(50);
            editButton.setVisibility(View.VISIBLE);
            useInfo.setVisibility(View.GONE);
        } }

    //----------------------------------------------------------------------------------------------

    //사진 회전
    public void rotate(View view) {
        if ( uri != null ){
            degrees = degrees + 90;
            Glide.with(this)
                    .load(myImageMaker.getImage(uri,degrees,800*600))
                    .into(imageView);
            editButton.setVisibility(View.VISIBLE);
            useInfo.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //----------------------------------------------------------------------------------------------

    //투명도 설정
    public void setStepAlpha(View view) {

        if ( alpha%5 == 0 ) {
            alphaNumber = 100;
            frontView.setImageAlpha(alphaNumber);
        } else if ( alpha%5 == 1 ) {
            alphaNumber = 150;
            frontView.setImageAlpha(alphaNumber);
        } else if ( alpha%5 == 2) {
            alphaNumber = 200;
            frontView.setImageAlpha(alphaNumber);
        } else if (alpha%5 == 3 ){
            alphaNumber = 0;
            frontView.setImageAlpha(alphaNumber);
        } else if (alpha%5 == 4 ){
            alphaNumber = 50;
            frontView.setImageAlpha(alphaNumber);
        }
        editButton.setVisibility(View.VISIBLE);
        useInfo.setVisibility(View.GONE);
        alpha++;
    }

    //----------------------------------------------------------------------------------------------

    //백버튼 설정
    public void back(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}
