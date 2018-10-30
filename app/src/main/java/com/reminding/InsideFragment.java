package com.reminding;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.reminding.MyImageMaker;
import com.reminding.loginActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import uk.co.senab.photoview.PhotoViewAttacher;

public class InsideFragment extends Fragment {

    //이미지뷰 구성요소
    ImageView randomView;
    PhotoViewAttacher mAttacher = null;

    //내용바 구성요소
    TextView dateText;
    TextView howManyText;
    TextView explainText;
    TextView dirText;
    TextView alertText;

    //랜덤버튼
    FloatingActionButton randomButton;
    int PICTURE_NUMBER = 0; //랜덤반복 상수

    //랜덤요소추출
    Cursor c; //커서는 DB의 데이터를 테이블의 행, 열 참조하는 것처럼 사용하게하는 편의성 도구
    String noFilter = "폴더별로 셔플해보세요"; //필터 미사용 키

    //메모로 보내는 요소
    Bundle bundle = new Bundle(); //번들을 new 번들까지 해줘야 보내짐
    Uri uri; //커서로 유알아이 꺼내옴
    String howManyString; //메모에 가져갈 글귀
    int degrees = 0; //회전

    //툴바
    LinearLayout toolBar;
    ImageButton shareButton;
    ImageButton memoButton;
    ImageButton rotateButton;
    ImageButton deleteButton;
    TextView useInfoText;
    Spinner spinner;

    //상단바
    ImageView alarmButton;
    ImageView alarmButton2;
    ImageView sendButton;
    ImageView logoutButton;

    //애니메이션
    Animation fabAnim;
    Animation imageAnim;

    //로딩다이얼로그
    ProgressDialog progressDialog;

    //안내말
    CardView useInfo;
    ImageButton helpButton;
    ImageButton helpButton2;

    //쉐어드프리퍼런스
    SharedPreferences lp;
    SharedPreferences.Editor lEdit;

    //----------------------------------------------------------------------------------------------

    public static InsideFragment newInstance() {
        Bundle args = new Bundle();
        InsideFragment fragment = new InsideFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //----------------------------------------------------------------------------------------------

    @SuppressLint("CommitPrefEdits")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two, container, false);

        // 쉐프 연결
        lp = Objects.requireNonNull(getActivity()).getSharedPreferences("login", Context.MODE_PRIVATE);
        lEdit = lp.edit();

        //이미지뷰 연결
        randomView = view.findViewById(R.id.randomImageView);

        //상단바 연결
        alarmButton = view.findViewById(R.id.alarm_button);
        alarmButton2 = view.findViewById(R.id.alarm_button2);
        sendButton = view.findViewById(R.id.send_button);
        logoutButton = view.findViewById(R.id.logout_button);

        //로딩다이얼로그
        progressDialog = new ProgressDialog(getContext(), R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("잠시만 기다려 주세요");

        //내용바 연결
        dirText = view.findViewById(R.id.dir_text);
        howManyText = view.findViewById(R.id.how_many_text);
        explainText = view.findViewById(R.id.explain_text);
        dateText = view.findViewById(R.id.random_date_text);
        alertText = view.findViewById(R.id.alert_text);

        //안내바
        useInfo = view.findViewById(R.id.use_info);
        helpButton = view.findViewById(R.id.help_button);
        helpButton2 = view.findViewById(R.id.help_button2);
        //초기세팅
        if(lp.getBoolean("help",false)){
            helpButton.setVisibility(View.GONE);
            helpButton2.setVisibility(View.VISIBLE);
            useInfo.setVisibility(View.VISIBLE);
        } else {
            helpButton.setVisibility(View.VISIBLE);
            helpButton2.setVisibility(View.GONE);
            useInfo.setVisibility(View.GONE);
        }
        //리스너세팅
        helpButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                      helpButton.setVisibility(View.GONE);
                      helpButton2.setVisibility(View.VISIBLE);
                      useInfo.setVisibility(View.VISIBLE);
                      lEdit.putBoolean("help",true);
                      lEdit.apply();
                  }
              }
        );
        helpButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpButton.setVisibility(View.VISIBLE);
                helpButton2.setVisibility(View.GONE);
                useInfo.setVisibility(View.GONE);
                lEdit.putBoolean("help",false);
                lEdit.apply();
            }
        });

        //랜덤버튼 연결
        randomButton = view.findViewById(R.id.random_button);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(fabAnim);
                randomView.startAnimation(imageAnim);
                toolBar.setVisibility(View.VISIBLE);
                randomImage();
            }
        });

        //툴바 연결
        toolBar = view.findViewById(R.id.tools_bar);
        shareButton = view.findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage();
            }
        });
        deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage();
            }
        }); //삭제
        rotateButton = view.findViewById(R.id.rotate_button);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotate();
            }
        });
        memoButton = view.findViewById(R.id.memo_button);
        memoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memoImage();
            }
        });
        useInfoText = view.findViewById(R.id.use_info_text);

        //스피너 : 갤러리 필터
        spinner = view.findViewById(R.id.spinner);
        ArrayList<String> list = folderList();
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, list);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                c = filterRandomImage(String.valueOf(spinner.getItemAtPosition(i)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                c = filterRandomImage(String.valueOf(spinner.getItemAtPosition(0)));
            }
        });

        //애니메이션 연결
        fabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.random_button);
        fabAnim.setInterpolator(getContext(), android.R.anim.accelerate_decelerate_interpolator);
        imageAnim = AnimationUtils.loadAnimation(getContext(), R.anim.random_load);
        imageAnim.setInterpolator(getContext(), android.R.anim.accelerate_decelerate_interpolator);

        //초기화면 연결
        yearRandomImage();

        //상단바 리스너
        //알람버튼
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmButton.setVisibility(View.GONE);
                alarmButton2.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "자정에 과거의 오늘 사진을 보내드리겠습니다.", Toast.LENGTH_LONG).show();
            }
        });

        //알람버튼2
        alarmButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmButton.setVisibility(View.VISIBLE);
                alarmButton2.setVisibility(View.GONE);

            }
        });

        //문의하기
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                String[] address = { "dellose@naver.com" } ;
                intent.putExtra(Intent.EXTRA_EMAIL, address); //배열을 받으므로 위와 같이 선언해야합니다.
                intent.putExtra(Intent.EXTRA_SUBJECT, "리마인딩에 이런 점을 문의합니다!");
                startActivityForResult(intent, 9999);
            }
        });

        //로그아웃 (이것은 원흉)
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("왜")
                        .setMessage("이 앱은 어떤 앱인지 간단하게 알아볼까요?")
                        .setCancelable(false)
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                lEdit.putBoolean("login_key",false);
                                lEdit.commit();
                                Intent intent = new Intent(getContext(), loginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        return view;
    }

    //----------------------------------------------------------------------------------------------

    //초기랜덤이미지화면
    public void yearRandomImage() {

        mAttacher = new PhotoViewAttacher(randomView);
        mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);

        //커서로 이미지 전부 불러옴
        c = getActivity().getContentResolver().query( //엘지x5
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //content://로 시작하는 콘텐츠 테이블 유알아이
                new String[]{MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.DATE_MODIFIED}, //어떤 열을 출력할 것인지
                null, //어떤 조건  출력할 것인지
                null, //조건절 파라미터
                null //어떻게 정렬할 것인지
        );

        if (c != null) {

            //이미지 개수
            int total = c.getCount();

            //이미지가 있다면
            if (total > 0) {
                for (int a = PICTURE_NUMBER; a < total; a++) {

                    //커서를 다 옮김
                    c.moveToPosition(a);

                    //날짜
                    SimpleDateFormat dayFormatter = new SimpleDateFormat("MMdd", Locale.KOREA);
                    SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy", Locale.KOREA);
                    long someday = c.getLong(c.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
                    long today = System.currentTimeMillis();

//                    long dMSecond = currentMSecond - dateMSecond;
//                    int tDay = Integer.parseInt(String.valueOf(dMSecond / (1000 * 60 * 60 * 24))); // 그날의 날

                    Date someDate = new Date(someday);
                    String someDayString = dayFormatter.format(someDate);
                    int someYear = Integer.parseInt(yearFormatter.format(someDate));

                    Date toDate = new Date(today);
                    String toDayString = dayFormatter.format(toDate);
                    int toYear = Integer.parseInt(yearFormatter.format(toDate));


                    //tDay % 365 == 0 && tDay != 0
                    //차이가 1년이라면 데이타, 폴더이름, 갱신날짜를 가져옴
                    //날짜가 동일하며 연도의 차이가 0(오늘)이 아닐때
                    if ( someDayString.equals(toDayString) && toYear - someYear != 0 ) {

                        //요소를 불러와서
                        String dirString = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                        String dateString = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED));
                        String dataString = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                        SimpleDateFormat formatter = new SimpleDateFormat("#yyyyMMdd", Locale.KOREA);
                        Date specialTime = new Date(someday);
                        String dTime = formatter.format(specialTime);
                        howManyString = String.valueOf(toYear - someYear) + "년 전 오늘";
                        explainText.setText("과거의 오늘, 당신의 추억을 공유해보세요^^");

                        //각 구성요소에 넣어줌
                        dirText.setText("#"+dirString);
                        howManyText.setText(howManyString);
                        dateText.setText(dTime);
                        if (String.valueOf(someday / 1000).equals(dateString)) {
                            alertText.setVisibility(View.VISIBLE);
                            alertText.setText("#날짜정보변경");
                        } else {
                            alertText.setVisibility(View.GONE);
                        }
                        uri = Uri.parse(dataString);
                        MyImageMaker myImageMaker = new MyImageMaker();
                        randomView.setImageBitmap(myImageMaker.getImage(uri, degrees, 800 * 600));

                        //맞는 파일을 찾았을 때, 파일의 포지션 다음부터 찾을 수 있도록 하고 반복을 끝냄.
                        PICTURE_NUMBER = a + 1; //포지션 다음
                        a = total + 1; //반복을 끝냄

                    }

                    //반복을 지속했을 때 값이 없으면
                    if (a == total - 1) {
                        randomImage();
                        useInfoText.setText("#과거의오늘없음  #오늘사진찍어두자");
                        PICTURE_NUMBER = 0;
                    }
                }
                c.close();
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    //랜덤이미지버튼
    public void randomImage() {

        progressDialog.show();
        mAttacher = new PhotoViewAttacher(randomView);
        mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);

        //랜덤 글씨 표출
        Random rd = new Random();
        int i = rd.nextInt(5);
        String explain = "내가 이런 사진도 가지고 있었구나~!!";
        String useInfo = "#과거의오늘  #힐링타임  #랜덤갤러리";
        switch (i){
            case 0 :
                explain = "이것 뭐지? 대박인데ㅋㅋㅋㅋㅋ";
                useInfo = "#추억팔이  #그땐그랬지  #힐링"; break;
            case 1 :
                explain = "내가 이런 사진도 가지고 있었구나~!!";
                useInfo = "#과거의오늘  #힐링타임  #랜덤갤러리"; break;
            case 2 :
                explain = "갤러리 오랜만에 보니까 재밌다.ㅋ";
                useInfo = "#셔플갤러리   #추억하세요"; break;
            case 3 :
                explain = "어 이거는 그 친구 보내줘야겠다.";
                useInfo = "#추억공유   #과거로떠다는여행"; break;
            case 4 :
                explain = "이 사진은 이제 필요없으려나ㅠ";
                useInfo = "#사진정리   #필요해"; break;
        }
        explainText.setText(explain);
        useInfoText.setText(useInfo);

        //이미지 개수
        int total = c.getCount();
        int position = (int) (Math.random() * total); //이 값이 전체 이미지 개수 안의 랜덤 수를 가져와

        //이미지가 있다면
        if (c != null) {
            if (total > 0) {

                if (c.moveToPosition(position)) { // 커서를 포지션 행(Row)로 이동시킨다.

                    //날짜
                    long dateMSecond = c.getLong(c.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
                    long currentMSecond = System.currentTimeMillis();
                    long dMSecond = currentMSecond - dateMSecond;
                    int dDay = Integer.parseInt(String.valueOf(dMSecond / (1000 * 60 * 60 * 24))); // 그날의 날

                    //요소를 불러와서
                    String dirString = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                    String dateString = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED));
                    String dataString = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                    SimpleDateFormat formatter = new SimpleDateFormat("#yyyyMMdd", Locale.KOREA);
                    Date specialTime = new Date(dateMSecond);
                    String dTime = formatter.format(specialTime);

                    //각 구성요소에 넣어줌
                    dirText.setText("#"+dirString);
                    if (dDay < 365) {
                        howManyString = "오늘로부터 " + dDay + "일 전";
                        howManyText.setText(howManyString);
                    } else {
                        //180일/365일로 년도를 반올림 시킴
                        int dYear = Integer.parseInt(String.valueOf(dDay / 365));
                        if ( dDay % 365 > 180 ) {
                            howManyString = "오늘로부터 " + dDay + "일 전 (" + (dYear+1)+ "년 전)";
                        } else {
                            howManyString = "오늘로부터 " + dDay + "일 전 (" + (dYear)+ "년 전)";
                        }
                        howManyText.setText(howManyString);
                    }
                    dateText.setText(dTime);
                    if (String.valueOf(dateMSecond / 1000).equals(dateString)) {
                        alertText.setVisibility(View.VISIBLE);
                        alertText.setText("#날짜정보변경");
                    } else {
                        alertText.setVisibility(View.GONE);
                    }
                    uri = Uri.parse(dataString);
                    MyImageMaker myImageMaker = new MyImageMaker();
                    randomView.setImageBitmap(myImageMaker.getImage(uri, 0, 800 * 600));

                }
//                c.close();
            }
        }
        progressDialog.dismiss();
    }

    //----------------------------------------------------------------------------------------------

    //필터로 커서 설정
    public Cursor filterRandomImage(String s) {
        Cursor filter;
        if ( !s.equals(noFilter) ){
            //커서로 이미지 전부 불러옴
            filter = getActivity().getContentResolver().query( //엘지x5
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //content://로 시작하는 콘텐츠 테이블 유알아이
                    new String[]{MediaStore.Images.ImageColumns.DATA,
                            MediaStore.Images.ImageColumns.DATE_TAKEN,
                            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                            MediaStore.Images.ImageColumns.DATE_MODIFIED}, //어떤 열을 출력할 것인지
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " = '" + s + "'", //어떤 조건절을 출력할 것인지
                    null, //조건절의 파라미터
                    null //어떻게 정렬할 것인지
            );
            return filter;
        } else {
            //커서로 이미지 전부 불러옴
            filter = getActivity().getContentResolver().query( //엘지x5
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //content://로 시작하는 콘텐츠 테이블 유알아이
                    new String[]{MediaStore.Images.ImageColumns.DATA,
                            MediaStore.Images.ImageColumns.DATE_TAKEN,
                            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                            MediaStore.Images.ImageColumns.DATE_MODIFIED}, //어떤 열을 출력할 것인지
                    null, //어떤 조건절을 출력할 것인지
                    null, //조건절의 파라미터
                    null //어떻게 정렬할 것인지
            );
            return filter;
        }
    }

    //----------------------------------------------------------------------------------------------

    //갤러리 폴더 리스트 추출
    public ArrayList<String> folderList() {

        //커서로 이미지 전부 불러옴
        c = getActivity().getContentResolver().query( //엘지x5
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //content://로 시작하는 콘텐츠 테이블 유알아이
                new String[]{MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.DATE_MODIFIED}, //어떤 열을 출력할 것인지
                null, //어떤 행을 출력할 것인지
                null,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " desc"
        );
        //폴더명 뽑기
        ArrayList<String> dirList = new ArrayList<>();
        for (int i = 0; i < c.getCount() - 1; i++) {
            if (c.moveToPosition(i)) {
                String dirString1 = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                if (c.moveToPosition(i + 1)) {
                    String dirString2 = c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                    if (!dirString1.equals(dirString2)) {
                        int dirNumber = 0;
                        dirList.add(dirNumber, dirString1);
                        dirNumber++;
                        if( i == c.getCount()-2 ){
                            dirList.add(dirNumber, dirString2);
                        }
                    }
                }
            }
        }
        dirList.add(0,noFilter);
        return dirList;
    }

    //----------------------------------------------------------------------------------------------

    //카카오톡 이미지 공유
    public void shareImage() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        if (uri == null) {
            Toast.makeText(getContext(), "공유할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setPackage("com.kakao.talk");
            startActivity(intent);
        }
    }

    //----------------------------------------------------------------------------------------------

    //파일 삭제
    Intent mediaScanIntent;
    File file;

    public void deleteImage() {
        mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        if (uri == null) {
            Toast.makeText(getContext(), "사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        } else {
            file = new File(uri.getPath());
            if (file.exists()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("삭제 확인")
                        .setMessage("삭제를 진행하면 되겠습니까?")
                        .setCancelable(false)
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                uri = Uri.fromFile(file);
                                mediaScanIntent.setData(uri); //인텐트에 추가로 넣어준다음
                                file.delete();
                                randomView.setImageBitmap(null);
                                Objects.requireNonNull(getActivity()).sendBroadcast(mediaScanIntent); //방송으로 보내
                                Toast.makeText(getContext(), "사진 파일이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    //이미지회전
    public void rotate() {
        progressDialog.show();
        mAttacher = new PhotoViewAttacher(randomView);
        if (uri != null) {
            degrees = degrees + 90;
            MyImageMaker myImageMaker = new MyImageMaker();
            randomView.setImageBitmap(myImageMaker.getImage(uri, degrees, 800 * 600));
        } else {
            Toast.makeText(getContext(), "사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();
    }

    //----------------------------------------------------------------------------------------------

    //메모보내기
    private void memoImage() {

        if (uri == null) {
            Toast.makeText(getContext(), "사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        } else {

            progressDialog.show();

            //번들 최초 포장 장소
            bundle.putParcelable("uri", uri);
            bundle.putInt("degrees", degrees);
            bundle.putString("nick", howManyString);
            bundle.putInt("key", 4000);

            //메모키값저장
            lEdit.putBoolean("memo_key", true);
            lEdit.commit();

            //메모창보내기 (수정필요)
            Intent intent = new Intent(getActivity(), MainActivity.class);
            if (bundle.getInt("key") == 4000) {
                intent.putExtras(bundle); //둘다 있어야 가는구나
                Objects.requireNonNull(getActivity()).startActivity(intent, bundle); //둘다 있어야가는구나.
                getActivity().finish(); //대신 처음에 추가한 자료는 남지 않음.
            } else {
                Toast.makeText(getContext(), "메모할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }
}

