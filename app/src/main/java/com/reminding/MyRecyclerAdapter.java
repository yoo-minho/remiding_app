package com.reminding;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    //받아올 데이터리스트
    private final List<CardItem> mDataList;

    //선택삭제 요소
    ArrayList<Integer> checkList = new ArrayList<>(); //체크상태 확인하여 체크(1), 비체크(0) 담음
    Boolean adapterSelectNumber = false; //체크박스 활성화 수
    int passListNumber = 0; //클릭 아이템 포지션

    //뷰선택 요소
    int adapterViewNumber = 0; //리스트뷰(0), 그리드뷰(1)

    //----------------------------------------------------------------------------------------------

    //생성자
    MyRecyclerAdapter(List<CardItem> dataList) { //어레이리스트 셋온 (생성자)
        mDataList = dataList;
    }

    //----------------------------------------------------------------------------------------------

    //인터페이스(메쏘드를 공유하는 간이 클래스 = 도구)
    public interface MyRecyclerViewClickListener { //메쏘드 선언
        void itemBoxClicked(int i);
        void longItemBoxClicked(int i);
    }
    private MyRecyclerViewClickListener mListener; //인터페이스 객체 선언
    public void setOnClickListener(MyRecyclerViewClickListener listener){ //객체 셋온
        mListener = listener;
    }

    //----------------------------------------------------------------------------------------------

    //뷰홀더(=틀, 리사이클러뷰 틀에 쓰일 아이템 선언)
    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView; //전체
        ImageView backImage; //이미지
        ImageView frontColor; //투명
        TextView title; //내용
        TextView date; //날짜
        CheckBox checkBox; //체크박스 (기본값 : 비활성)
        FrameLayout imageFrame; //리스트뷰 전용 이미지 공간
        ImageView imageInFrame; //리스트뷰 이미지

        //뷰홀더와 뷰 연결
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            backImage = itemView.findViewById(R.id.back_image);
            frontColor = itemView.findViewById(R.id.front_color);
            title = itemView.findViewById(R.id.title_text);
            date = itemView.findViewById(R.id.date_textView);
            checkBox = itemView.findViewById(R.id.item_checkBox);
            imageFrame = itemView.findViewById(R.id.image_frame);
            imageInFrame = itemView.findViewById(R.id.image_in_frame);

        }
    }

    //----------------------------------------------------------------------------------------------

    //뷰홀더 생성
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        //리스트뷰
        if( adapterViewNumber == 0 ){
            view = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.item_list, viewGroup, false);
        //그리드뷰
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.item_card, viewGroup, false);
        }
        return new ViewHolder(view);
    }

    //----------------------------------------------------------------------------------------------

    //뷰홀더 연결
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, @SuppressLint("RecyclerView") final int i) {

        //AsyncTask 활용 비동기 이미지 로딩
        new RecyclerViewLoadTask(mDataList, viewHolder, i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //체크박스 활성화 유무 체크
        checkList = new ArrayList<>();
            if(adapterSelectNumber){

                // 선택삭제 시 최대 선택 가능한 아이템 수
                int MAX_DELETE_ITEM = 100;

                //체크박스 활성화
                viewHolder.checkBox.setVisibility(View.VISIBLE);

                //아이템 제외 체크박스 100개 초기화
                if ( checkList.isEmpty() ){
                    for (int j = 0; j < MAX_DELETE_ITEM; j++ ){
                        if ( passListNumber == j ){ checkList.add(j,1);
                        } else { checkList.add(j,0); }
                    }
                } else {
                    for (int j = 0; j < MAX_DELETE_ITEM; j++ ){
                        if ( passListNumber == j ){
                            checkList.remove(j); checkList.add(j,0);
                        } else { checkList.remove(j); checkList.add(j,0); }
                    }}

                //아이템 체크 활성화
                if ( passListNumber == i ) { viewHolder.checkBox.setChecked(true);
                } else { viewHolder.checkBox.setChecked(false); }

            } else {

                //체크박스 비활성화
                viewHolder.checkBox.setVisibility(View.INVISIBLE);

            }

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        if(viewHolder.checkBox.isChecked()){
                            //체크클릭하면 1값을 부여
                            checkList.remove(i);
                            checkList.add(i, 1);
                        } else {
                            //비체크클릭하면 않으면 0값을 부여
                            checkList.remove(i);
                            checkList.add(i, 0);
                        }}});

        //인터페이스 체크
        if (mListener != null){
            viewHolder.cardView.setOnClickListener(new View.OnClickListener(){
                @Override
            public void onClick(View v){ mListener.itemBoxClicked(i);}
        });
            viewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) { mListener.longItemBoxClicked(i);
                    return true; }
            });}}

    //----------------------------------------------------------------------------------------------

    //리사이클러뷰 사이즈 필수 메쏘드
    @Override public int getItemCount() { return mDataList.size();}

    //----------------------------------------------------------------------------------------------

    //AsyncTask 활용 부분
    private static class RecyclerViewLoadTask extends AsyncTask<Void, Bitmap, Bitmap> {

        //기본 요소
        List<CardItem> taskDataList; ViewHolder taskViewHolder; int taskI;
        CardItem item;

        //리사이클러뷰 연결
        public  RecyclerViewLoadTask(List<CardItem> list, ViewHolder viewHolder, int i){
            taskDataList = list; taskViewHolder = viewHolder; taskI = i; }

        //쓰레드 실행되기 전 세팅 영역
        @Override protected void onPreExecute() {
            item = taskDataList.get(taskI);
            taskViewHolder.backImage.setImageBitmap(null);
            taskViewHolder.title.setText("간직한것이 떠오르도록"); //셋
            taskViewHolder.date.setText("이디엇노트");
            try{ taskViewHolder.frontColor.setImageAlpha(50);
            } catch (NullPointerException e){ e.printStackTrace(); }
    }

        //쓰레드 실행 영역 : 비트맵 조합
        @Override protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = new MyImageMaker().
                        getImage(item.getUri(), item.getDegrees(),400*300);
            return bitmap;
        }

        //쓰레드 실행과 동시에 UI 쓰레드 적용 영역
        @Override protected void onProgressUpdate(Bitmap... values) { }

        //쓰레드 실행이 후 세팅 영역 : 비트맵을 세팅함
        @Override protected void onPostExecute(Bitmap bitmap) {
            taskViewHolder.backImage.setImageBitmap(bitmap);
            if(bitmap != null){ taskViewHolder.imageFrame.setVisibility(View.VISIBLE);
            taskViewHolder.imageInFrame.setImageBitmap(bitmap);
            } else { taskViewHolder.imageFrame.setVisibility(View.GONE);}
            taskViewHolder.title.setText(item.getTitle());
            try{ taskViewHolder.frontColor.setImageAlpha(item.getAlpha());
            } catch (NullPointerException e) { e.printStackTrace(); }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
            Date currentTime = new Date(item.getDate());
            String cTime = formatter.format(currentTime);
            taskViewHolder.date.setText(cTime);
        }
    }

}
