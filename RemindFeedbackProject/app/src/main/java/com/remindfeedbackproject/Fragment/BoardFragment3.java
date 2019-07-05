package com.remindfeedbackproject.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.remindfeedbackproject.Etc.NetworkStatus;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Adapter.Fragment3Adapter;
import com.remindfeedbackproject.Etc.SwipeController;
import com.remindfeedbackproject.Etc.SwipeControllerAction;
import com.remindfeedbackproject.Dao.VoiceDao;
import com.remindfeedbackproject.Dialog.Fragment3Dialog;
import com.remindfeedbackproject.Dialog.RecordInfoDialog;
import com.remindfeedbackproject.Dialog.TitleDialog;
import com.remindfeedbackproject.Dto.VoiceDto;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BoardFragment3 extends Fragment {
    //bundle에서 받아올 사용자 로그인 키
    String loginEmailKey;
    String feedbackKey;
    String feedbackTag;
    String ONGOING_FEEDBACKLIST;
    String COMPLETE_FEEDBACKLIST;
    int feedbackPosition;

    private Context context; //Toast 메세지를 전달하기 위한 context 선언
    View view; //fragment 뷰 선언

    //사용자 입력이 있을 변수 선언
    //Recycler View 관련 객체 변수 선언
    ArrayList<VoiceDto> voiceArrList; //recyclerview를 담을 빈 데이터 리스트 변수
    RecyclerView recyclerView; //recyclerview 객체 변수
    Fragment3Adapter fragment3Adapter;//recycler view와 연결할 Adapter 객체
    RecyclerView.LayoutManager layoutManager; //recyclerview 레이아웃매니저 객체 변수

    FloatingActionButton fbtnCreate; //추가 버튼 객체

    //Swipe Controller 관련 객체 변수 선언
    SwipeController swipeController; //swipe controller 객체 선언

    //Fragment에 쓰일 View를 초기화하는 메소드
    //LayoutInflater를 인자로 받아서 layout으로 설정한 XML을 연결하거나 bundle에 의한 작업을 함
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.board_fragment3, container, false);

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
        voiceArrList = new ArrayList<VoiceDto>(); //피드백 목록을 담을 빈 데이터 리스트 생성

        //VoiceDao에서 친구 목록 불러와서 저장
        VoiceDao voiceDao = new VoiceDao(getContext());
        //친구 목록이 있는지 없는지 확인
        //친구목록이 있는 경우(NONE을 반환하지 않은 경우)에만 친구목록 저장
        if(!voiceDao.getVoiceListValue(loginEmailKey, feedbackKey).equals("NONE")) {
            voiceArrList = voiceDao.readAllVoiceInfo(loginEmailKey, feedbackKey);
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment3_recyclerview); //recyclerview 생성
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);//recycler view 아이템을 linear 형태로 보여줌
        recyclerView.setLayoutManager(layoutManager);//layout manager 설정
        fragment3Adapter = new Fragment3Adapter(voiceArrList);
        recyclerView.setAdapter(fragment3Adapter);

        //화면 버튼 관련 객체 초기화
        fbtnCreate = (FloatingActionButton) view.findViewById(R.id.fragment3_fbtn_create);

        //만일 진행 중 피드백 목록 탭을 선택한 경우
        if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
            fbtnCreate.show();
        }
        //만일 완료한 피드백 목록 탭을 선택한 경우
        else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
            fbtnCreate.hide();
        }

        //SwipeController 관련 객체 초기화
        swipeController = new SwipeController(new SwipeControllerAction() {
            //왼쪽 클릭하면 편집
            @Override
            public void onLeftClicked(final int position) {
                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(context.getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                    //만일 내 피드백 목록 탭을 선택한 경우
                    if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
                        //만일 피드백이 진행 중인 경우
                        if(feedbackTag.equals("진행중")) {
                            //피드백 수정 다이얼로그 메소드 호출
                            //다이얼로그 객체 생성
                            TitleDialog updateDialog = new TitleDialog(context, "update", voiceArrList.get(position).getVoiceTitle());
                            //다이얼로그 레이아웃 지정
                            updateDialog.setContentView(R.layout.title_dialog);

                            //다이얼로그를 보여주기 전 리스너 등록
                            updateDialog.setTitleDialogListener(new TitleDialog.TitleDialogListener() {
                                //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
                                @Override
                                public void onClicked(String state, String fileTitle) {
                                    updateItemInRecyclerView(position, state, fileTitle);
                                }
                            });

                            //다이얼로그 크기 조정
                            DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                            int width = displayMetrics.widthPixels;//디바이스 화면 너비
                            int height = displayMetrics.heightPixels;//디바이스 화면 높이

                            WindowManager.LayoutParams wm = updateDialog.getWindow().getAttributes();
                            wm.width = width;//화면의 가로 넓이
                            wm.height = height / 3;//화면의 세로 넓이
                            updateDialog.getWindow().setAttributes(wm);
                            //게시글 수정 다이얼로그 호출
                            updateDialog.show();
                        }
                        //피드백이 완료된 경우
                        else{
                            Toast.makeText(context.getApplicationContext(), "완료한 피드백은 내용을 수정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
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
            }

            //오른쪽 클릭하면 삭제
            @Override
            public void onRightClicked(int position) {
                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(context.getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                    //삭제 확인하는 alert dialog 불러옴
                    delConfirmAlertDialog(position);
                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어있지 않아 게시글을 삭제할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //recyclerView의 좌우에 아이템 추가
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        return view;
    }

    //View를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //오늘의 피드백 화면이므로 오늘 작성한 피드백 확인/새로운 피드백 작성 등을 할 수 있음
    @Override
    public void onResume() {
        super.onResume();

        //Recycler view의 아이템을 누르는 경우
        fragment3Adapter.setItemClick(new Fragment3Adapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
                //아이템을 짧게 누르는 경우
                //상세 화면 커스텀 다이얼로그를 띄움
                RecordInfoDialog itemInfoDialog = new RecordInfoDialog(view.getContext(), voiceArrList.get(position).getVoiceTitle(),
                        voiceArrList.get(position).getVoiceLength(), voiceArrList.get(position).getVoicePath());
                itemInfoDialog.setContentView(R.layout.fragment3_record_info);
                itemInfoDialog.setCancelable(false); //화면 외부 클릭해도 종료되지 않게 함

                //다이얼로그 크기 조정
                DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                int width = displayMetrics.widthPixels;//디바이스 화면 너비
                int height = displayMetrics.heightPixels;//디바이스 화면 높이

                WindowManager.LayoutParams wm = itemInfoDialog.getWindow().getAttributes();
                wm.width = width;//화면의 가로 넓이
                wm.height = height/3;//화면의 세로 넓이
                itemInfoDialog.getWindow().setAttributes(wm);

                itemInfoDialog.show();
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
                    //피드백 작성 다이얼로그 메소드 호출
                    //다이얼로그 객체 생성
                    Fragment3Dialog createDialog = new Fragment3Dialog(context, loginEmailKey, "create");
                    //다이얼로그 레이아웃 지정
                    createDialog.setContentView(R.layout.fragment3_record);
                    createDialog.setCancelable(false); //화면 외부 클릭해도 종료되지 않게 함

                    //다이얼로그를 보여주기 전 리스너 등록
                    createDialog.setFragment3DialogListener(new Fragment3Dialog.Fragment3DialogListener() {
                        //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
                        @Override
                        public void onCreateClicked(String etxtTitle, String txtRecordLength, String txtRecordPath) {
                            createItemInRecyclerView(etxtTitle, txtRecordLength, txtRecordPath);//피드백 메소드 호출
                        }
                    });

                    //다이얼로그 크기 조정
                    DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                    int width = displayMetrics.widthPixels;//디바이스 화면 너비
                    int height = displayMetrics.heightPixels;//디바이스 화면 높이

                    WindowManager.LayoutParams wm = createDialog.getWindow().getAttributes();
                    wm.width = width;//화면의 가로 넓이
                    wm.height = (height/3)*2;//화면의 세로 넓이
                    createDialog.getWindow().setAttributes(wm);
                    //게시판 생성 다이얼로그 호출
                    createDialog.show();
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
    private void createItemInRecyclerView(String voiceTitle, String voiceLength, String voicePath) {
        //새로운 VoiceDao, VoiceDto 객체 생성
        VoiceDao voiceDao = new VoiceDao(getContext());
        VoiceDto voiceDto = new VoiceDto();

        String voiceDate = setVoiceDate();

        //날짜를 포함한 문자열을 키 값으로 줌
        String tempVoiceKey = loginEmailKey.replace("@", "_");
        String accountVoiceKey = tempVoiceKey.replace(".", "_");

        String voiceKey = "voiceKey_" + accountVoiceKey + "_" + voiceDate;

        //arrayList에 저장할 값을 friendDto에 집어넣음
        voiceDto.setVoiceItem(voiceKey, voiceTitle, voiceLength, voicePath, voiceDate);

        //shared preference에 녹음 추가 메소드 호출
        voiceDao.insertVoiceInfo(loginEmailKey, feedbackKey, voiceDto); //shaed preference에 메모 추가

        //firebase에 녹음 파일 추가 메소드 호출
        voiceDao.insertOneRecordInfoToFirebase(loginEmailKey, feedbackKey, voiceDto);

        //글쓰기 제목과 내용을 넣음
        voiceArrList.add(voiceDto);

        //데이터 변경을 알림
        fragment3Adapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(context, "게시글이 추가되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 수정하는 메소드
    private void updateItemInRecyclerView(int position, String state, String voiceTitle) {
        if(state.equals("cancel")) {
            //아이템이 추가되었다는 메세지 화면에 띄우기
            Toast.makeText(context, "게시글 수정을 취소하였습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        //새로운 VoiceDao, VoiceDto 객체 생성
        VoiceDao voiceDao = new VoiceDao(getContext());
        VoiceDto voiceDto = new VoiceDto();

        //게시글 이름 변경
        //arrayList에 저장할 값을 friendDto에 집어넣음
        voiceDto.setVoiceItem(voiceArrList.get(position).getVoiceKey(), voiceTitle, voiceArrList.get(position).getVoiceLength(),
                voiceArrList.get(position).getVoicePath(), voiceArrList.get(position).getVoiceDate());

        //PhotoDao에서 사진 수정 메소드 호출
        voiceDao.updateOneVoiceInfo(loginEmailKey, feedbackKey, voiceDto); //shaed preference에 저장된 피드백 수정

        //firebase에 사진 수정 메소드 호출
        voiceDao.updateOneVoiceInfoToFirebase(loginEmailKey, feedbackKey, voiceDto);

        //아이템 수정
        voiceArrList.set(position, voiceDto);

        //데이터 변경을 알림
        fragment3Adapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(context, "게시글이 수정되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 삭제하는 메소드
    //실제 파일도 같이 삭제
    private void deleteItemInRecyclerView(int position) {
        //VoiceDao에서 데이터 삭제 메소드 호출
        VoiceDao voiceDao = new VoiceDao(getContext());
        //shared preference에 데이터 삭제
        voiceDao.deleteOneVoiceInfo(loginEmailKey, feedbackKey, voiceArrList.get(position).getVoiceKey());

        //firebase에서 데이터 삭제
        voiceDao.deleteOneVoiceInfoToFirebase(loginEmailKey, feedbackKey, voiceArrList.get(position));

//        String filePath = voiceArrList.get(position).getVoicePath(); //파일 경로를 가져옴
//        File file = new File(filePath);
//        file.delete();//해당하는 파일 삭제

        voiceArrList.remove(position); //배열에서 아이템 삭제
        fragment3Adapter.notifyItemRemoved(position); //아이템 삭제 알림
        fragment3Adapter.notifyItemRangeChanged(position, fragment3Adapter.getItemCount()); //길이 달라졌음을 알림
    }

    //현재 날짜를 저장하는 메소드
    private String setVoiceDate(){
        long nowTime = System.currentTimeMillis(); //현재 시간을 시스템에서 가져옴
        Date date = new Date(nowTime); //Date 생성하기
        SimpleDateFormat timeSDF = new SimpleDateFormat("yyyyMMdd_hhmmss");//yyyy-MM-dd 형식으로 가져옴
        return timeSDF.format(date);
    }
}
