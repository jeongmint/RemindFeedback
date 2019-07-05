package com.remindfeedbackproject.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dto.MemoDto;

import java.util.ArrayList;

public class Fragment1Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //Recycler View의 행(row)를 표시하는 클래스
    public class Fragment1ViewHolder extends RecyclerView.ViewHolder {
        View view; //View 객체 선언

        //정의된 객체 선언
        private TextView txtTitle; //게시글 제목 변수 선언
        private TextView txtContent; //게시글 내용 변수 선언
        private TextView txtTime; //게시글 날짜 변수 선언

        Fragment1ViewHolder(View view) {
            super(view);
            this.view = view;

            txtTitle = view.findViewById(R.id.fragment1_itm_txt_title);
            txtContent = view.findViewById(R.id.fragment1_itm_txt_content);
            txtTime = view.findViewById(R.id.fragment1_itm_txt_time);
        }
    }

    //아이템 객체를 집어넣을 배열 선언
    private ArrayList<MemoDto> memoArrList;

    public Fragment1Adapter(ArrayList<MemoDto> memoArrList) {
        this.memoArrList = memoArrList;
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
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment1_item, viewGroup, false);
        return new Fragment1ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        Fragment1ViewHolder fragment1ViewHolder = (Fragment1ViewHolder) viewHolder;

        //아이템 클래스에 정의된 변수에서 값을 가져와서 레이아웃에 적용
        fragment1ViewHolder.txtTitle.setText(memoArrList.get(position).getMemoTitle());
        fragment1ViewHolder.txtContent.setText(memoArrList.get(position).getMemoContent());
        fragment1ViewHolder.txtTime.setText(memoArrList.get(position).getMemoDate());

        //recycler view의 각각의 아이템에 OnClickListener를 등록
        fragment1ViewHolder.view.setOnClickListener(new View.OnClickListener() {
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
        return memoArrList.size();
    }
}
