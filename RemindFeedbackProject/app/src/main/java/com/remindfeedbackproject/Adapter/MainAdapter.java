package com.remindfeedbackproject.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dto.FeedbackDto;

import java.util.ArrayList;

//Main Adapter를 정의하는 클래스
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //Recycler View의 행(row)를 표시하는 클래스
    public class MainViewHolder extends RecyclerView.ViewHolder {
        View view; //View 객체 선언

        //main_item.xml에 정의된 객체 선언
        private ImageView imgTag; //피드백 그룹 태그 변수 선언
        private TextView txtTitle;//피드백 제목 변수 선언
        private TextView txtTime; //피드백 시간 변수 선언
        private TextView txtName; //피드백 제공자 이름 변수 선언
        private LinearLayout linearLayout; //피드백 배경 변수 선언
        private TextView txtState; //작성자 혹은 조언자 이름 저장 변수

        MainViewHolder(View view) {
            super(view);
            this.view = view;
            imgTag = view.findViewById(R.id.main_itm_img_tag);
            txtTitle = view.findViewById(R.id.main_itm_txt_title);
            txtTime = view.findViewById(R.id.main_itm_txt_time);
            txtName = view.findViewById(R.id.main_itm_txt_name);
            linearLayout = view.findViewById(R.id.main_itm_layout_linear);
            txtState = view.findViewById(R.id.main_itm_txt_state);
        }
    }

    //아이템 객체를 집어넣을 배열 선언
    private ArrayList<FeedbackDto> feedbackArrList;

    public MainAdapter(ArrayList<FeedbackDto> feedbackArrList) {
        this.feedbackArrList = feedbackArrList;
    }

    //아이템 클릭시 실행 함수
    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(View view, int position);
    }

    //아이템 클릭시 실행 함수 등록 함수
    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    //아이템 길게 클릭 시 실행 함수
    private ItemLongClick itemLongClick;

    public interface ItemLongClick{
        public void onLongClick(View view, int position);
    }

    //아이템 길게 클릭 시 실행 함수 등록
    public void setItemLongClick(ItemLongClick itemLongClick){
        this.itemLongClick = itemLongClick;
    }

    //View Holder 생성
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //새로운 View를 생성
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.main_item, viewGroup, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        MainViewHolder mainViewHolder = (MainViewHolder) viewHolder;

        //아이템 클래스에 정의된 변수에서 값을 가져와서 레이아웃에 적용
        //tag에 따라 tagColor 변경("진행중" : 적색, "완료" : 녹색)
        int tagColor;
        if(feedbackArrList.get(position).getFeedbackTag().equals("진행중")){
            //피드백을 진행 중이므로 적색으로 표시
            tagColor = Color.rgb(255, 80, 80);
        }
        else if(feedbackArrList.get(position).getFeedbackTag().equals("완료")) {
            //피드백을 완료하였으므로 녹색으로 표시
            tagColor= Color.rgb(00, 200, 80);
        }else{
            //회원 탈퇴한 경우 검정으로 표시
            tagColor = Color.rgb(00, 00, 00);
        }

        //background에 따라 backgroundColor 변경("내 피드백" : 회색, "남 피드백" : 노란 색)
        int backgroundColor;
        if(feedbackArrList.get(position).getFeedbackBackground().equals("myFeedback")){
            //내 피드백이므로 회색 표시
            backgroundColor = Color.rgb(234, 234, 234);
            mainViewHolder.txtState.setText("[피드백 조언자]");
        } else {
            //남 피드백이므로 노란색 표시
            backgroundColor= Color.rgb(255, 228, 0);
            mainViewHolder.txtState.setText("[피드백 작성자]");
        }

        mainViewHolder.imgTag.setBackgroundColor(tagColor);
        mainViewHolder.txtTitle.setText(feedbackArrList.get(position).getFeedbackTitle());
        mainViewHolder.txtTime.setText(feedbackArrList.get(position).getFeedbackDate());
        mainViewHolder.linearLayout.setBackgroundColor(backgroundColor);

        mainViewHolder.txtName.setText(feedbackArrList.get(position).getAdviserNickName());

        //recycler view의 각각의 아이템에 OnClickListener를 등록
        mainViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null){
                    itemClick.onClick(v, position);
                }
            }
        });

        //recycler view의 각각의 아이템에 OnLongClickListener를 등록
        mainViewHolder.view.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            //길게 눌렀을 때
            public boolean onLongClick(View v){
                if(itemLongClick != null){
                    itemLongClick.onLongClick(v, position);
                }
                //이벤트 처리를 완료한 경우 true를 리턴
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedbackArrList.size();
    }
}
