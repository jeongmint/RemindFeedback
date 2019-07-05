package com.remindfeedbackproject.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.remindfeedbackproject.Etc.NetworkStatus;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Activity.TakePhotoActivity;
import com.remindfeedbackproject.Adapter.Fragment2Adapter;
import com.remindfeedbackproject.Dao.PhotoDao;
import com.remindfeedbackproject.Dialog.PhotoInfoDialog;
import com.remindfeedbackproject.Dto.PhotoDto;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BoardFragment2 extends Fragment {
    //bundle에서 받아올 사용자 로그인 키
    String loginEmailKey;
    String feedbackKey;
    String feedbackTag;
    String ONGOING_FEEDBACKLIST;
    String COMPLETE_FEEDBACKLIST;
    int feedbackPosition;

    private Context context; //Toast 메세지를 전달하기 위한 context 선언
    View view; //fragment 뷰 선언

    private final int CREATE_PHOTO_CODE = 160; //사진 게시글 생성
    private final int UPDATE_PHOTO_CODE = 161; //사진 게시글 수정

    //사용자 입력이 있을 변수 선언
    //Recycler View 관련 객체 변수 선언
    ArrayList<PhotoDto> photoArrList; //recyclerview를 담을 빈 데이터 리스트 변수
    RecyclerView recyclerView; //recyclerview 객체 변수
    Fragment2Adapter fragment2Adapter;//recycler view와 연결할 Adapter 객체
    RecyclerView.LayoutManager layoutManager; //recyclerview 레이아웃매니저 객체 변수

    FloatingActionButton fbtnCreate; //추가 버튼 객체

    //Fragment에 쓰일 View를 초기화하는 메소드
    //LayoutInflater를 인자로 받아서 layout으로 설정한 XML을 연결하거나 bundle에 의한 작업을 함
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.board_fragment2, container, false);

        //Bundle에서 값 받아옴
        loginEmailKey = getArguments().getString("loginEmailKey");
        feedbackKey = getArguments().getString("feedbackKey");
        feedbackTag = getArguments().getString("feedbackTag");
        ONGOING_FEEDBACKLIST = getArguments().getString( "ONGOING_FEEDBACKLIST");
        COMPLETE_FEEDBACKLIST = getArguments().getString("COMPLETE_FEEDBACKLIST");
        feedbackPosition = getArguments().getInt("feedbackPosition");

        //Toast 메세지를 보여주기 위한 context 선언
        context = container.getContext();

        //사용자 입력이 있을 객체를 초기화
        //Recycler View 관련 객체 초기화
        photoArrList = new ArrayList<PhotoDto>(); //피드백 목록을 담을 빈 데이터 리스트 생성

        //PhotoDao에서 친구 목록 불러와서 저장
        PhotoDao photoDao = new PhotoDao(getContext());
        //친구 목록이 있는지 없는지 확인
        //친구목록이 있는 경우(NONE을 반환하지 않은 경우)에만 친구목록 저장
        if(!photoDao.getPhotoListValue(loginEmailKey, feedbackKey).equals("NONE")) {
            photoArrList = photoDao.readAllPhotoInfo(loginEmailKey, feedbackKey);
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment2_recyclerview); //recyclerview 생성
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(context, 2);//recycler view 아이템을 Grid Layout 형태로 보여줌
        recyclerView.setLayoutManager(layoutManager);//layout manager 설정
        fragment2Adapter = new Fragment2Adapter(photoArrList);
        recyclerView.setAdapter(fragment2Adapter);

        //화면 버튼 관련 객체 초기화
        fbtnCreate = (FloatingActionButton) view.findViewById(R.id.fragment2_fbtn_create);

        //만일 진행 중 피드백 목록 탭을 선택한 경우
        if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
            fbtnCreate.show();
        }
        //만일 완료한 피드백 목록 탭을 선택한 경우
        else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
            fbtnCreate.hide();
        }

        return view;
    }

    //View를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //오늘의 피드백 화면이므로 오늘 작성한 피드백 확인/새로운 피드백 작성 등을 할 수 있음
    @Override
    public void onResume() {
        super.onResume();

        //Recycler view의 아이템을 누르는 경우
        fragment2Adapter.setItemClick(new Fragment2Adapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
              //상세 화면 커스텀 다이얼로그를 띄움
                PhotoInfoDialog itemInfoDialog = new PhotoInfoDialog(view.getContext(), photoArrList.get(position).getPhotoTitle(),
                        photoArrList.get(position).getPhotoPath(), photoArrList.get(position).getPhotoDate());
                itemInfoDialog.setContentView(R.layout.fragment2_item_info);

                //다이얼로그 크기 조정
                DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                int width = displayMetrics.widthPixels;//디바이스 화면 너비
                int height = displayMetrics.heightPixels;//디바이스 화면 높이

                WindowManager.LayoutParams wm = itemInfoDialog.getWindow().getAttributes();
                wm.width = width;//화면의 가로 넓이
                wm.height = wm.WRAP_CONTENT;//화면의 세로 넓이
                itemInfoDialog.getWindow().setAttributes(wm);

                itemInfoDialog.show();
            }
        });

        //Recycler view의 아이템을 길게 누르는 경우
        fragment2Adapter.setItemLongClick(new Fragment2Adapter.ItemLongClick() {
            @Override
            public void onLongClick(View view, final int position) {
                //수정 혹은 삭제 커스텀 다이얼로그를 띄움
                CharSequence info[] = new CharSequence[] {"수정", "삭제"};

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("제목");
                builder.setItems(info, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int status = NetworkStatus.getConnectivityStatus(context.getApplicationContext());
                        switch(which) {
                            case 0: //수정
                                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                                //인터넷 연결 상태 확인
                                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                                    //만일 진행중 피드백 목록 탭을 선택한 경우
                                    if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
                                        //아이템을 짧게 누르는 경우
                                        Intent intent = new Intent(getActivity(), TakePhotoActivity.class);
                                        String[] photoData = new String[3];
                                        photoData[0] = photoArrList.get(position).getPhotoTitle();
                                        photoData[1] = photoArrList.get(position).getPhotoPath();
                                        intent.putExtra("intentData", photoData);
                                        intent.putExtra("position", position);
                                        intent.putExtra("activityState", "update");
                                        startActivityForResult(intent, UPDATE_PHOTO_CODE);
                                    }
                                    //만일 완료한 피드백 목록을 선택한 경우
                                    else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
                                        //메세지를 띄움
                                        Toast.makeText(context.getApplicationContext(), "완료한 피드백의 게시글은 수정할 수 없습니다!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                //인터넷이 연결되어 있지 않은 경우
                                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                                else {
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                                    builder.setTitle("인터넷 연결 확인");
                                    builder.setMessage("인터넷이 연결되어있지 않아 게시글을 수정할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    builder.show();
                                }
                                break;
                            case 1: // 삭제
                                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                                //인터넷 연결 상태 확인
                                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                                    //만일 내 피드백 목록 탭을 선택한 경우
                                    //삭제 확인하는 alert dialog 불러옴
                                    delConfirmAlertDialog(position);
                                }
                                //인터넷이 연결되어 있지 않은 경우
                                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                                else {
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                                    builder.setTitle("인터넷 연결 확인");
                                    builder.setMessage("인터넷이 연결되어있지 않아 게시글을 생성할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    builder.show();
                                }

                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        //새 글 추가 버튼을 누르는 경우
        //게시글 추가 버튼 이벤트를 받는 리스너
        fbtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(context.getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                    //아이템을 짧게 누르는 경우
                    Intent intent = new Intent(getActivity(), TakePhotoActivity.class);
                    intent.putExtra("loginEmailKey", loginEmailKey);
                    intent.putExtra("intentData", "NONE");
                    intent.putExtra("activityState", "create");
                    startActivityForResult(intent, CREATE_PHOTO_CODE);
                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어있지 않아 게시글을 생성할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    //activity에서 값을 받아오는 메소드
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {

        if (resultCode == getActivity().RESULT_OK) {
            String[] getIntentData;
            switch (requestCode) {
                case CREATE_PHOTO_CODE:
                    getIntentData = intent.getStringArrayExtra("intentData");
                    createItemInRecyclerView(getIntentData[0], getIntentData[1]); //게시글 생성 메소드 호출
                    break;
                case UPDATE_PHOTO_CODE:
                    getIntentData = intent.getStringArrayExtra("intentData");
                    int position = intent.getIntExtra("position", -1);
                    updateItemInRecyclerView(position, getIntentData[0], getIntentData[1]); //게시글 수정 메소드 호출
                    break;
                default:
                    break;
            }
        }
    }

    //recycler view 아이템 삭제 확인하는 dialog 띄우는 메소드
    private void delConfirmAlertDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("피드백 삭제");
        alertDialogBuilder.setMessage("정말 피드백을 삭제하시겠습니까?");

        //아니오를 선택한 경우 alert dialog 끔
        alertDialogBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        //예를 선택한 경우 삭제 메소드 호출
        alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItemInRecyclerView(position);
            }
        });
        alertDialogBuilder.show();//alert dialog 보여줌
    }

    //recycler view에 아이템을 추가하는 메소드
    private void createItemInRecyclerView(String photoTitle, String photoPath) {
        //새로운 PhotoDao, PhotoDto 객체 생성
        PhotoDao photoDao = new PhotoDao(getContext());
        PhotoDto photoDto = new PhotoDto();

        String photoDate = setPhotoDate();

        //날짜를 포함한 문자열을 키 값으로 줌
        String tempPhotoKey = loginEmailKey.replace("@", "_");
        String accountPhotoKey = tempPhotoKey.replace(".", "_");
        String photoKey = "photoKey_" + accountPhotoKey + "_" + photoDate;

        //arrayList에 저장할 값을 friendDto에 집어넣음
        photoDto.setPhotoItem(photoKey, photoTitle, photoPath, photoDate);

        //shared preference에 사진 추가 메소드 호출
        photoDao.insertPhotoInfo(loginEmailKey, feedbackKey, photoDto); //shaed preference에 메모 추가

        //firebase에 사진 추가 메소드 호출
        photoDao.insertOnePhotoInfoToFirebase(loginEmailKey, feedbackKey, photoDto);

        //글쓰기 제목과 내용을 넣음
        photoArrList.add(photoDto);

        //데이터 변경을 알림
        fragment2Adapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(context, "게시글이 추가되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 수정하는 메소드
    private void updateItemInRecyclerView(int position, String photoTitle, String photoPath) {
        //새로운 PhotoDao, PhotoDto 객체 생성
        PhotoDao photoDao = new PhotoDao(getContext());
        PhotoDto photoDto = new PhotoDto();

        //arrayList에 저장할 값을 friendDto에 집어넣음
        photoDto.setPhotoItem(photoArrList.get(position).getPhotoKey(), photoTitle, photoPath, photoArrList.get(position).getPhotoDate());

        //shared preference에 사진 수정 메소드 호출
        photoDao.updateOnePhotoInfo(loginEmailKey, feedbackKey, photoDto); //shaed preference에 저장된 피드백 수정

        //firebase에 사진 수정 메소드 호출
        photoDao.updateOnePhotoInfoToFirebase(loginEmailKey, feedbackKey, photoDto);

        //아이템 수정
        photoArrList.set(position, photoDto);

        //데이터 변경을 알림
        fragment2Adapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(context, "게시글이 수정되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 삭제하는 메소드
    private void deleteItemInRecyclerView(int position) {
        //PhotoDao에서 데이터 삭제 메소드 호출
        PhotoDao photoDao = new PhotoDao(getContext());
        //shared preference에 사진 삭제 메소드 호출
        photoDao.deleteOnePhotoInfo(loginEmailKey, feedbackKey, photoArrList.get(position).getPhotoKey());

        //firebase 사진 삭제 메소드 호출
        photoDao.deleteOnePhotoInfoToFirebase(loginEmailKey, feedbackKey, photoArrList.get(position));

        photoArrList.remove(position); //배열에서 아이템 삭제
        fragment2Adapter.notifyItemRemoved(position); //아이템 삭제 알림
        fragment2Adapter.notifyItemRangeChanged(position, fragment2Adapter.getItemCount()); //길이 달라졌음을 알림
    }

    //현재 날짜를 저장하는 메소드
    private String setPhotoDate(){
        long nowTime = System.currentTimeMillis(); //현재 시간을 시스템에서 가져옴
        Date date = new Date(nowTime); //Date 생성하기
        SimpleDateFormat timeSDF = new SimpleDateFormat("yyyyMMdd_hhmmss");//yyyy-MM-dd 형식으로 가져옴
        return timeSDF.format(date);
    }
}
