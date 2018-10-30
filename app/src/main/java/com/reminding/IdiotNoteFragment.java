package com.reminding;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.text.SymbolTable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.kakaolink.internal.LinkObject;
import com.kakao.util.KakaoParameterException;
import com.kakao.util.helper.log.Logger;
import com.reminding.MyRecyclerAdapter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static android.app.Activity.RESULT_OK;

public class IdiotNoteFragment extends Fragment implements MyRecyclerAdapter.MyRecyclerViewClickListener { //리사이클러어댑터의 인터페이스를 쓸꺼다. {

    //기본
    private int listNumber; //리사이클러뷰 위치상수
    MyRecyclerAdapter adapter; //리사이클러뷰 어댑터
    NestedScrollView nestedScrollView; //리사이클러뷰 감싸는 스크롤뷰
    FloatingActionButton addImageButton; //추가버튼
    FloatingActionButton topButton; //탑 버튼
    ImageView settingButton; //세팅 버튼
    ImageView settingButton2; //세팅 버튼 2
    LinearLayout viewBar; //뷰버튼과 세팅버튼 포함하는 바

    //메모에서 넘어온 요소
    private static Bundle mainBundle = null; //메모에서 넘어온 번들
    int key; //메모에서 넘어온 번들키

    //아이템
    List<CardItem> dataList; //아이템 리스트
    String resultNick; //제목
    String time; //시간
    Uri uri; //이미지경로
    int degrees = 1; //각도
    int alpha = 0; //투명도

    //뷰변환 요소
    ImageView listButton; //리스트뷰 변환버튼
    ImageView listButton2; //색
    ImageView gridButton; //그리드뷰 변환버튼
    ImageView gridButton2; //색
    int selectViewNumber = 0; //리스트뷰(0), 그리드뷰(1) 매칭 상수
    int spanCount = 1; //그리드뷰레이아웃매니지먼트 줄 요소

    //검색바 요소
    EditText searchEdit; //검색바
    ImageView searchButton;
    ImageView searchButton2;
    Boolean saveNumber = true; //검색시 저장 여부

    //선택삭제 버튼
    FloatingActionButton deleteCompleteButton;
    LinearLayout searchLinear;
    boolean DELETE_STATE = false;

    //광고배너
    ConstraintLayout adBanner;
    ImageView adImage;
    TextView adText;
    TextView adSubject;
    String connectUrl;
    String adTextStr;
    String adSubjectStr;

    //쉐어드프리퍼런스 요소
    SharedPreferences dp;
    SharedPreferences.Editor dEdit;
    String title = "title";
    String date = "date";
    String uriPath = "uri_path";
    String degreesString = "degrees";
    String alphaString = "alpha";

    //----------------------------------------------------------------------------------------------

    //프래그먼트 인스턴스 생성
    public static IdiotNoteFragment newInstance(Bundle bundle) {
        Bundle args = new Bundle();
        IdiotNoteFragment fragment = new IdiotNoteFragment();
        mainBundle = bundle; //프래그먼트 생성시 메인에서 넘겨준 번들을 받음
        fragment.setArguments(args);
        return fragment;
    }

    //----------------------------------------------------------------------------------------------

    //프래그먼트 뷰 생성
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        //뷰 연결 및 구성요소 연결
        final View view = inflater.inflate(R.layout.fragment_one, container, false);

        //쉐어드프리퍼런스 연결
        dp = Objects.requireNonNull(getContext()).getSharedPreferences("data", Context.MODE_PRIVATE);
        dEdit = dp.edit();

        //로드
        dataList = new ArrayList<>(); //아이템리스트 선언
        onLoadData();

        if(!dp.getBoolean("first",false)){
            dataList.add(0, new CardItem(
                    "첫 사용자라면\n" +
                            "사용설명서(URL)를\n" +
                            "참조해주세요.\n" +
                            "https://goo.gl/sCNxL7\n" +
                            "- 리마인딩",
                    20180917,
                    null,
                    0,
                    0));
            dEdit.putBoolean("first",true);
            dEdit.apply();
            onSaveData();
        }

        adapter = new MyRecyclerAdapter(dataList); //어댑터 선언

        //광고배너
        adBanner = view.findViewById(R.id.ad_banner);
        adImage = view.findViewById(R.id.ad_image);
        adText = view.findViewById(R.id.ad_text);
        adSubject = view.findViewById(R.id.ad_subject);
        Random rd = new Random();
        int adRandomNumber = rd.nextInt(3);
        switch ( adRandomNumber){
            case 0 :
                connectUrl = "https://goo.gl/forms/mvW9cFJyPwzZSPdi2";
                adTextStr = "여러분의 피드백은\n" +
                        "더나은 서비스를 만듭니다.";
                adSubjectStr = "- 리마인딩 설문지";
                adImage.setImageResource(R.drawable.back4); break;
            case 1 :
                connectUrl = "https://docs.google.com/document/d/1bdlTNBxVZfU-ShSLYfqvhJBsIXiqa251EaFhQ8CCeLg/edit?usp=sharing";
                adTextStr = "도움말이 필요하다면\n" +
                        "이 글을 정독해주세요. 도움이 될겁니다.";
                adSubjectStr = "- 리마인딩 도움말";
                adImage.setImageResource(R.drawable.ad1); break;
            case 2 :
                connectUrl = "https://goo.gl/forms/mvW9cFJyPwzZSPdi2";
                adTextStr = "이 앱이 괜찮다면\n" +
                        "친구들에게 소개시켜주세요!";
                adSubjectStr = "- 리마인딩 홍보대사";
                adImage.setImageResource(R.drawable.back8); break;
        }
        adText.setText(adTextStr);
        adSubject.setText(adSubjectStr);
        adBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(connectUrl));
                startActivity(intent);
            }
        });

//        adBanner.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Intent intent = new Intent(getContext(),EditActivity.class);
//                    intent.putExtra("nick",adTextStr+"\n"+adSubjectStr);
//                    intent.putExtra("ad_key",3333);
//                    startActivityForResult(intent,2000);
//                    Objects.requireNonNull(getActivity()).overridePendingTransition(android.R.anim.fade_in, 0); //화면전환 애니메이션 효과 삭제
//                return true;
//
//            }
//        });

        //메모에서 넘어온 작업
        if(mainBundle != null){
            key = mainBundle.getInt("key",0);
            if( key == 4000 ){
                add(); //추가창 실행
                mainBundle = null;
            }
        }

        //검색바 생성
        viewBar = view.findViewById(R.id.view_bar);
        searchButton = view.findViewById(R.id.search_button);
        searchButton2 = view.findViewById(R.id.search_button2);
        searchEdit = view.findViewById(R.id.search_edit);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //타이핑 중에 원상태로 로드시킴
                searchButton2.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.GONE);
                dataList.clear();
                onLoadData();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = searchEdit.getText().toString();
                if ( searchText.length() != 0 ){
                    saveNumber = false; //이 상태를 세이브 하지않음
                    addImageButton.hide();
                    viewBar.setVisibility(View.INVISIBLE);
                    adBanner.setVisibility(View.INVISIBLE);
                    //타이틀과 본문에 검색어가 둘 다 없으면 지움
                    for ( int i = dataList.size()-1 ; i > -1 ; i-- ){
                        if( !dataList.get(i).getTitle().contains(searchText)){
                            dataList.remove(i);
                        }
                    }
                } else {
                    saveNumber = true; //이 상태를 세이브함
                    dataList.clear(); //다 지움
                    onLoadData();
                    searchButton2.setVisibility(View.GONE);
                    searchButton.setVisibility(View.VISIBLE);
                    addImageButton.show();
                    viewBar.setVisibility(View.VISIBLE);
                    adBanner.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged(); //리사이클러뷰 갱신
            }
        });

        //네스트스크롤뷰 생성 및 컨트롤
        nestedScrollView = view.findViewById(R.id.nest);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {
                if(!DELETE_STATE){
                    if(i1 <= i3 ){ //올라갈때
                        addImageButton.show();
                        topButton.hide();
                    } else {
                        addImageButton.hide();
                        topButton.show();
                    }}}});

        //뷰변환 설정
        listButton = view.findViewById(R.id.list_button);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {         setListView();}});
        listButton2 = view.findViewById(R.id.list_button2);
        listButton2. setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { setListView();}});
        gridButton = view.findViewById(R.id.grid_button);
        gridButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {         setGridView();}});
        gridButton2 = view.findViewById(R.id.grid_button2);
        gridButton2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {         setGridView();}});

        //온크리에이트에 뷰 필터에 따른 색 상태 유지
        selectViewNumber = dp.getInt("view",0); //0리1그
        spanCount = dp.getInt("count",1); //1리2그
        if ( selectViewNumber == 0 && spanCount == 1 ){
            listButton.setVisibility(View.GONE);
            listButton2.setVisibility(View.VISIBLE);
            gridButton.setVisibility(View.VISIBLE);
            gridButton2.setVisibility(View.GONE);
        } else {
            listButton.setVisibility(View.VISIBLE);
            listButton2.setVisibility(View.GONE);
            gridButton.setVisibility(View.GONE);
            gridButton2.setVisibility(View.VISIBLE);
        }

        //리사이클러뷰 설정
        try{
            //설정내용 불러와서 적용
            selectViewNumber = dp.getInt("view",0);
            adapter.adapterViewNumber = selectViewNumber;
            spanCount = dp.getInt("count",1);
        } catch (Exception ignored){ }
        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view); //리사이클러뷰와 화면을 연결한다.
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(),spanCount); //레이아웃매니저 선언
        recyclerView.setLayoutManager(layoutManager); //리사이클러뷰에 레이아웃매니저 연결
        adapter.setOnClickListener(this); //클릭 인터페이스 연결
        recyclerView.setAdapter(adapter); //리사이클러뷰에 어댑터 연결

        // 추가버튼
        addImageButton = view.findViewById(R.id.main_add_button);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });
        addImageButton.show();

        // 탑버튼
        topButton = view.findViewById(R.id.main_top_button);
        topButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nestedScrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });


        //스크린샷과 바로메모에서 넘어온 요소
        if ( String.valueOf(getActivity().getIntent().getIntExtra("key",0)).equals("101") ){
            Intent intent = getActivity().getIntent();
            //가져온 데이터를 활용
            resultNick = intent.getStringExtra("nick");
            uri = intent.getData();
            degrees = intent.getIntExtra("degrees",0);
            key = intent.getIntExtra("key",0);
            alpha = intent.getIntExtra("alpha",0);
            dataList.add(0, new CardItem(resultNick,System.currentTimeMillis(),uri,degrees,alpha));
            adapter.notifyDataSetChanged();
            onSaveData();
            Toast.makeText(getContext(), "메모가 추가되었습니다.", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
            Objects.requireNonNull(getActivity()).overridePendingTransition(android.R.anim.fade_in,0); //화면전환 애니메이션 효과 삭제
            getActivity().finish();
        }

        //선택 삭제 완료 버튼
        searchLinear = view.findViewById(R.id.search_linear);
        deleteCompleteButton = view.findViewById(R.id.delete_complete_button);
        deleteCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("삭제 확인")
                        .setMessage("삭제를 진행하면 되겠습니까?")
                        .setCancelable(false)
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                adapter.adapterSelectNumber = false;
                                adapter.notifyDataSetChanged();
                                onSaveData();
                                deleteCompleteButton.clearAnimation();
                                deleteCompleteButton.hide();
                                searchLinear.setVisibility(View.VISIBLE);
                                DELETE_STATE = false;
                                dEdit.putBoolean("delete_state", DELETE_STATE);
                                dEdit.apply();
                                addImageButton.show();
                                dialogInterface.cancel();
                            }
                        })
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ArrayList<Integer> check = adapter.checkList;
                                //백개를 뒤져서 데이터사이즈보다 작고 값이 1인 친구들을 찾아서 없애.
                                for(int j = 99 ; j > -1 ; j--){
                                    if ( j < dataList.size() && check.get(j) == 1 ){
                                        dataList.remove(j);
                                    }
                                }
                                adapter.adapterSelectNumber = false;
                                adapter.notifyDataSetChanged();
                                onSaveData();
                                deleteCompleteButton.hide();
                                searchLinear.setVisibility(View.VISIBLE);
                                DELETE_STATE = false;
                                dEdit.putBoolean("delete_state", DELETE_STATE);
                                dEdit.commit();
                                addImageButton.show();
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //세팅 버튼
        settingButton = view.findViewById(R.id.main_setting_button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(dp.getInt("listNumber",0) == 0){
                    Toast.makeText(getContext(), "첫화면에 표시할 메모가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    settingButton.setVisibility(View.GONE);
                    settingButton2.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(getContext(), ScreenService.class); //스크린서비스 연결
                    dEdit.putBoolean("first_screen_key",true);
                    dEdit.commit();
                    Toast.makeText(getContext(), "첫화면에 그림메모가 랜덤하게 나타납니다.", Toast.LENGTH_SHORT).show();
                    getActivity().startService(intent);
                }
            }
        });
        settingButton2 = view.findViewById(R.id.main_setting_button2);
        settingButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                settingButton.setVisibility(View.VISIBLE);
                settingButton2.setVisibility(View.GONE);

                Intent intent = new Intent(getContext(), ScreenService.class);
                dEdit.putBoolean("first_screen_key",false);
                dEdit.commit();
                Toast.makeText(getContext(), "첫화면에 앱 실행을 멈춥니다.", Toast.LENGTH_SHORT).show();
                getActivity().stopService(intent);

            }
        });
        if ( dp.getBoolean("first_screen_key",false) ){
            settingButton.setVisibility(View.GONE);
            settingButton2.setVisibility(View.VISIBLE);
        } else {
            settingButton.setVisibility(View.VISIBLE);
            settingButton2.setVisibility(View.GONE);
        }
        return view;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null ) {
            //가져온 데이터를 활용합니다.
            resultNick = data.getStringExtra("nick");
            uri = data.getData();
            degrees = data.getIntExtra("degrees",0);
            key = data.getIntExtra("key",0);
            alpha = data.getIntExtra("alpha",0);

            //수정에 따른 리턴입니다.
            if( requestCode == 1000 ){
                dataList.remove(listNumber);
                dataList.add(listNumber, new CardItem(resultNick,System.currentTimeMillis(),uri,degrees,alpha));
                adapter.notifyItemChanged(listNumber);
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "메모가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                onSaveData();

            //추가에 따른 리턴입니다.
            } else if ( requestCode == 2000 ) {
                dataList.add(0, new CardItem(resultNick,System.currentTimeMillis(),uri,degrees,alpha));
                adapter.notifyDataSetChanged();
                onSaveData();
                Toast.makeText(getContext(), "메모가 추가되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    public void add() {
        Intent intent = new Intent(getContext(),EditActivity.class);
        if( key == 4000 && mainBundle != null){
            intent.putExtras(mainBundle);
            startActivityForResult(intent,2000, mainBundle);
            Objects.requireNonNull(getActivity()).overridePendingTransition(android.R.anim.fade_in, 0); //화면전환 애니메이션 효과 삭제
        } else {
            startActivityForResult(intent, 2000);
            Objects.requireNonNull(getActivity()).overridePendingTransition(android.R.anim.fade_in, 0); //화면전환 애니메이션 효과 삭제
        }
    }

    //----------------------------------------------------------------------------------------------

    //인터페이스 오버라이딩
    //수정화면
    @Override
    public void itemBoxClicked(int i) {
        Intent intent = new Intent(getContext(),EditActivity.class);
        intent.putExtra("nick",dataList.get(i).getTitle());
        intent.putExtra("degrees",dataList.get(i).getDegrees());
        intent.putExtra("alpha",dataList.get(i).getAlpha());
        intent.setData(dataList.get(i).getUri());
        listNumber = i;
        Objects.requireNonNull(getActivity()).overridePendingTransition(0,0); //화면전환 애니메이션 효과 삭제
        startActivityForResult(intent, 1000);
    }

    //삭제화면
    @Override
    public void longItemBoxClicked(int i) {
        if ( !dataList.isEmpty()){
            adapter.adapterSelectNumber = true; //체크박스 활성화되도록
            adapter.passListNumber = i; //아이템은 체크되도록

            //버튼 제어
            addImageButton.hide();
            deleteCompleteButton.show();
            searchLinear.setVisibility(View.GONE);
            addImageButton.hide();
            topButton.hide();

            //삭제상태저장
            DELETE_STATE = true;
            dEdit.putBoolean("delete_state", DELETE_STATE);
            dEdit.commit();

            //갱신
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "삭제할 사진이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    //----------------------------------------------------------------------------------------------

    //프래그먼트 중지
    @Override
    public void onStop() {
        super.onStop();
        //검색시에 세이브를 하지 않음
        if(saveNumber){
            onSaveData();
        } else {
            saveNumber = true;
        }
    }

    //----------------------------------------------------------------------------------------------

    //리스트뷰 전환
    private void setListView(){
        selectViewNumber = 0; //리스트
        spanCount = 1;
        //설정내용 저장
        dEdit.putInt("view",selectViewNumber); //0리1그
        dEdit.putInt("count",spanCount); //1리2그
        dEdit.apply();
        onSaveData();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("login_key",false);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(0,0); //화면전환 애니메이션 효과 삭제
        getActivity().finish();
        listButton.setVisibility(View.GONE);
        listButton2.setVisibility(View.VISIBLE);
        gridButton.setVisibility(View.VISIBLE);
        gridButton2.setVisibility(View.GONE);
    }

    //----------------------------------------------------------------------------------------------

    //그리드뷰 전환
    private void setGridView(){
        selectViewNumber = 1; //그리드
        spanCount = 2;
        //설정내용 저장
        dEdit.putInt("view",selectViewNumber);
        dEdit.putInt("count",spanCount);
        dEdit.commit();
        onSaveData();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("login_key",false);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(0,0); //화면전환 애니메이션 효과 삭제
        getActivity().finish();
        listButton.setVisibility(View.VISIBLE);
        listButton2.setVisibility(View.GONE);
        gridButton.setVisibility(View.GONE);
        gridButton2.setVisibility(View.VISIBLE);
    }

    //----------------------------------------------------------------------------------------------

    //저장하기
    private void onSaveData(){
        if ( dataList.size() == 0 ){
            dEdit.putInt("listNumber",dataList.size());
            dEdit.commit();
        }
        for ( int i = 0 ; i < dataList.size() ; i++){
            dEdit.putInt("listNumber",dataList.size());
            dEdit.putString(title+i, dataList.get(i).getTitle());
            try{
                dEdit.putString(uriPath+i, dataList.get(i).getUri().getPath());
            } catch (NullPointerException e){
                dEdit.putString(uriPath+i, "");
            }
            dEdit.putLong(date+i,dataList.get(i).getDate());
            dEdit.putInt(degreesString+i, dataList.get(i).getDegrees());
            dEdit.putInt(alphaString+i,dataList.get(i).getAlpha());
            dEdit.commit();
        }
    }

    //----------------------------------------------------------------------------------------------

    //불러오기
    private void onLoadData(){
        for ( int i = 0 ; i < dp.getInt("listNumber",0) ; i++){
            dataList.add(i, new CardItem(
                    dp.getString(title+i,""),
                    dp.getLong(date+i,0),
                    Uri.parse(dp.getString(uriPath+i,"")),
                    dp.getInt(degreesString+i,0),
                    dp.getInt(alphaString+i,0)));
        }
    }

}


