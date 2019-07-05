package com.remindfeedbackproject.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dto.FriendDto;

import java.util.ArrayList;

public class FriendListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //Recycler View의 행(row)를 표시하는 클래스
    public class FriendListViewHolder extends RecyclerView.ViewHolder {
        View view; //View 객체 선언

        //정의된 객체 선언
        private TextView newFriendEmail; //친구 이메일 변수 선언
        private TextView newFriendName; //친구 이름 변수 선언
        private TextView newFriendProfile; //친구 프로필 변수 선언
        private ImageView newFriendRelationship; //친구와의 관계 변수 선언

        FriendListViewHolder(View view) {
            super(view);
            this.view = view;

            newFriendEmail = view.findViewById(R.id.friendlist_itm_txt_email);
            newFriendName = view.findViewById(R.id.friendlist_itm_txt_name);
            newFriendProfile = view.findViewById(R.id.friendlist_itm_txt_profile);
            newFriendRelationship = view.findViewById(R.id.friendlist_itm_img_relationship);
        }
    }

    //아이템 객체를 집어넣을 배열 선언
    private ArrayList<FriendDto> friendArrList;

    public FriendListAdapter(ArrayList<FriendDto> friendArrList) {
        this.friendArrList = friendArrList;
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
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friendlist_item, viewGroup, false);
        return new FriendListAdapter.FriendListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        FriendListViewHolder friendListViewHolder = (FriendListViewHolder) viewHolder;

        //아이템 클래스에 정의된 변수에서 값을 가져와서 레이아웃에 적용
        friendListViewHolder.newFriendEmail.setText(friendArrList.get(position).getFriendEmail());
        friendListViewHolder.newFriendName.setText(friendArrList.get(position).getFriendNickName());

        friendListViewHolder.newFriendProfile.setText(friendArrList.get(position).getFriendProfileMessage());

        //아이템 클래스에 정의된 변수에서 값을 가져와서 레이아웃에 적용
        //tag에 따라 relationColor 변경("FALSE" : 회색, "TRUE" : 녹색)
        int relationColor;
        //친구 관계가 일방인 경우
        if(friendArrList.get(position).getFriendRelationship().equals("false")){
            //일방적 친구 관계이므로 회색으로 표시
            relationColor = Color.parseColor("#5d5d5d");
        }
        //친구 관계가 쌍방인 경우
        else {
            //피드백을 완료하였으므로 녹색으로 표시
            relationColor= Color.parseColor("#1ddb16");
        }
        friendListViewHolder.newFriendRelationship.setColorFilter(relationColor, PorterDuff.Mode.SRC_IN);

        //recycler view의 각각의 아이템에 OnClickListener를 등록
        friendListViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null){
                    itemClick.onClick(v, position);
                }
            }
        });

        //recycler view의 각각의 아이템에 OnLongClickListener를 등록
        friendListViewHolder.view.setOnLongClickListener(new View.OnLongClickListener(){
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
        return friendArrList.size();
    }
}
