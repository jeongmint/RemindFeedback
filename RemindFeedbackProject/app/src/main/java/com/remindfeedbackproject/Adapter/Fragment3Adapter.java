package com.remindfeedbackproject.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dto.VoiceDto;

import java.util.ArrayList;

public class Fragment3Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //Recycler View의 행(row)를 표시하는 클래스
    public class Fragment3ViewHolder extends RecyclerView.ViewHolder {
        View view; //View 객체 선언

        //정의된 객체 선언
        private TextView txtTitle; //게시글 제목 변수 선언
        private TextView txtRecordLength; //게시글 내용 변수 선언
        private TextView txtTime; //게시글 날짜 변수 선언

        Fragment3ViewHolder(View view) {
            super(view);
            this.view = view;

            txtTitle = view.findViewById(R.id.fragment3_itm_txt_title);
            txtRecordLength = view.findViewById(R.id.fragment3_itm_txt_recordlength);
            txtTime = view.findViewById(R.id.fragment3_itm_txt_time);
        }
    }

    //아이템 객체를 집어넣을 배열 선언
    private ArrayList<VoiceDto> voiceArrList;

    //Adapter에서 recycler view에 집어넣을 arraylist 값을 가져옴
    public Fragment3Adapter(ArrayList<VoiceDto> voiceArrList) {
        this.voiceArrList = voiceArrList;
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

    //View Holder 생성
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //새로운 View를 생성
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment3_item, viewGroup, false);
        return new Fragment3ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        Fragment3ViewHolder fragment3ViewHolder = (Fragment3ViewHolder) viewHolder;

        //아이템 클래스에 정의된 변수에서 값을 가져와서 레이아웃에 적용
        fragment3ViewHolder.txtTitle.setText(voiceArrList.get(position).getVoiceTitle());
        fragment3ViewHolder.txtRecordLength.setText(voiceArrList.get(position).getVoiceLength());
        fragment3ViewHolder.txtTime.setText(voiceArrList.get(position).getVoiceDate());

        //recycler view의 각각의 아이템에 OnClickListener를 등록
        fragment3ViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null){
                    itemClick.onClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return voiceArrList.size();
    }
}
