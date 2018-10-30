package com.reminding;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ScreenActivity extends AppCompatActivity {

    ImageView screenImage;
    ImageView frontColor;
    TextView titleText;
    TextView dateText;
    TextView timeText;
    TextView ampmText;
    SeekBar seekBar;
    Long now ;
    Date dateNow;
    boolean isRunning = true;
    Animation backAnim;

    //키관리
    String title = "title";
    String contents = "contents";
    String date = "date";
    String uriPath = "uri_path";
    String degreesString = "degrees";
    String number = "number";
    String alphaString = "alpha";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //상태바를 없앤다.
        setContentView(R.layout.activity_screen);

        //잠금화면
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | //기본 잠금화면보다 위면서
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD); //기본 잠금화면을 없애면서

        screenImage = findViewById(R.id.screen_image);
        frontColor = findViewById(R.id.front_color);
        titleText = findViewById(R.id.title_text);
        dateText = findViewById(R.id.date_text);
        timeText = findViewById(R.id.time_text);
        ampmText = findViewById(R.id.ampm_text);
        seekBar = findViewById(R.id.seekBar);
        seekBar.bringToFront();
        backAnim = AnimationUtils.loadAnimation(this, R.anim.scale_repeat);
        backAnim.setInterpolator(this, android.R.anim.accelerate_decelerate_interpolator);

        //에이씽크 시작
        new LockScreenTask().execute();

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ( motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    seekBar.setProgress(50);
                }
                return false;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(seekBar.getProgress() >= 85 ){
                    finish();
                } else if ( seekBar.getProgress() <= 15){
                    Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                    intent.putExtra("screen",101);
                    startActivity(intent);
                    finish();
                } else {
                    seekBar.setProgress(50);
                }
            }
        });
    }

    private class LockScreenTask extends AsyncTask<Void, Date, Void> {

        SharedPreferences dp;
        SharedPreferences.Editor dEdit;
        Uri uri;
        int degrees;
        int alpha;
        String getTitle;
        String getContext;
        String strings0;
        String strings1;
        String strings2;
        String strings3;
        SimpleDateFormat minute = new SimpleDateFormat("hh:mm", Locale.KOREA);
        SimpleDateFormat day = new SimpleDateFormat("MM.dd", Locale.KOREA);
        SimpleDateFormat amPm = new SimpleDateFormat("a", Locale.KOREA);
        SimpleDateFormat week = new SimpleDateFormat("E", Locale.KOREA);

        @Override
        protected void onPreExecute() { //쓰레드 실행전에 다 해놓고 기다려
            //쉐프
            dp = getSharedPreferences("data",MODE_PRIVATE);
            dEdit = dp.edit();

            //랜덤수
            Random rd = new Random();
            int screenRandom = rd.nextInt(dp.getInt("listNumber",0));

            //데이터로드
            getTitle = dp.getString(title + screenRandom,"");
            getContext = dp.getString(contents + screenRandom,"");
            uri = Uri.parse(dp.getString(uriPath + screenRandom,""));
            degrees = dp.getInt(degreesString+screenRandom,0);
            alpha = dp.getInt(alphaString+screenRandom,0);
            if( alpha == 0 ) {
                frontColor.setImageAlpha(100);
            } else {
                frontColor.setImageAlpha(alpha);
            }
            titleText.setText(getTitle);

            //이미지
            MyImageMaker imageMaker = new MyImageMaker();
            if ( !uri.getPath().equals("")){
                screenImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Bitmap bitmap = imageMaker.getImage(uri,degrees,800*600);
                Glide.with(getApplicationContext())
                        .load(bitmap)
                        .into(screenImage);
            } else {
                //이미지 없으면 준비한 랜덤 이미지가 나옴
                screenImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                int backRandom = 1 + rd.nextInt(10);
                int backImage;
                switch ( backRandom ){
                    case 1 : backImage = R.drawable.back1; break;
                    case 2 : backImage = R.drawable.back2; break;
                    case 3 : backImage = R.drawable.back3; break;
                    case 4 : backImage = R.drawable.back4; break;
                    case 5 : backImage = R.drawable.back5; break;
                    case 6 : backImage = R.drawable.back6; break;
                    case 7 : backImage = R.drawable.back7; break;
                    case 8 : backImage = R.drawable.back8; break;
                    case 9 : backImage = R.drawable.back9; break;
                    case 10 : backImage = R.drawable.back10; break;
                    default : backImage = R.drawable.back1; break;
                }
                Glide.with(getApplicationContext())
                        .load(backImage)
                        .into(screenImage);
            }

            //시간 초기 로드
            now = System.currentTimeMillis();
            dateNow = new Date(now);
            strings0 = minute.format(dateNow);
            strings1 = day.format(dateNow);
            strings2 = amPm.format(dateNow);
            strings3 = week.format(dateNow);
            timeText.setText(strings0);
            ampmText.setText(strings2);
            dateText.setText(strings1 + " " + strings3);

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) { //쓰레드 실행하면 해라

            //여러번 쓰면 쓰레드가 겹쳐서 이상하게 되구나
            //시간 변화 로드
            while ( isRunning ){
                now = System.currentTimeMillis();
                dateNow = new Date(now);
                publishProgress(dateNow);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Date... dates) { //UI 쓰레드

            Date date = dates[0];
            strings0 = minute.format(date);
            strings1 = day.format(date);
            strings2 = amPm.format(date);
            strings3 = week.format(date);
            timeText.setText(strings0);
            ampmText.setText(strings2);
            dateText.setText(strings1 + " " + strings3);

            super.onProgressUpdate(dates);
        }

        @Override
        protected void onPostExecute(Void voids) {

        }
    }


    //백버튼 비활성
    @Override
    public void onBackPressed() {
        seekBar.startAnimation(backAnim);
    }

    @Override
    protected void onStop() {
        isRunning = false;
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Intent intent = new Intent(this, ScreenActivity.class);
        startActivity(intent);
        finish();
        super.onRestart();
    }
}
