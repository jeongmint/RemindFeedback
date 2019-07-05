package com.remindfeedbackproject.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dto.PhotoDto;

import java.util.ArrayList;

public class Fragment2Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //Recycler View의 행(row)를 표시하는 클래스
    public class Fragment2ViewHolder extends RecyclerView.ViewHolder {
        View view; //View 객체 선언

        //정의된 객체 선언
        private TextView txtTitle; //게시글 제목 변수 선언
        private ImageView imgContent; //게시글 내용 변수 선언
        private TextView txtTime; //게시글 날짜 변수 선언

        Fragment2ViewHolder(View view) {
            super(view);
            this.view = view;

            txtTitle = view.findViewById(R.id.fragment2_itm_txt_title);
            imgContent = view.findViewById(R.id.fragment2_itm_img_content);
            txtTime = view.findViewById(R.id.fragment2_itm_txt_time);
        }
    }

    //아이템 객체를 집어넣을 배열 선언
    private ArrayList<PhotoDto> photoArrList;

    //Adapter에서 recycler view에 집어넣을 arraylist 값을 가져옴
    public Fragment2Adapter(ArrayList<PhotoDto> photoArrList) {
        this.photoArrList = photoArrList;
    }

    //아이템 클릭시 실행 함수
    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(View view, int position);
    }

    //아이템 클릭 시 실행 함수를 등록하는 함수
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
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment2_item, viewGroup, false);
        return new Fragment2ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        Fragment2ViewHolder fragment2ViewHolder = (Fragment2ViewHolder) viewHolder;

        //아이템 클래스에 정의된 변수에서 값을 가져와서 레이아웃에 적용
        fragment2ViewHolder.txtTitle.setText(photoArrList.get(position).getPhotoTitle());

        //이미지 파일의 경로에서 이미지를 불러와 적용
        Bitmap imgBitmap = BitmapFactory.decodeFile(photoArrList.get(position).getPhotoPath());
        fragment2ViewHolder.imgContent.setImageBitmap(imgBitmap);
        fragment2ViewHolder.txtTime.setText(photoArrList.get(position).getPhotoDate());

        //recycler view의 각각의 아이템에 OnClickListener를 등록
        fragment2ViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null){
                    itemClick.onClick(v, position);
                }
            }
        });

        //recycler view의 각각의 아이템에 OnLongClickListener를 등록
        fragment2ViewHolder.view.setOnLongClickListener(new View.OnLongClickListener(){
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
        return photoArrList.size();
    }
}
