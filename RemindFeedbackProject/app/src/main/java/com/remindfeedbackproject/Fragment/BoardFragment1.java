package com.remindfeedbackproject.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.remindfeedbackproject.Adapter.Fragment1Adapter;
import com.remindfeedbackproject.Etc.SwipeController;
import com.remindfeedbackproject.Etc.SwipeControllerAction;
import com.remindfeedbackproject.Dao.MemoDao;
import com.remindfeedbackproject.Dialog.Fragment1Dialog;
import com.remindfeedbackproject.Dto.MemoDto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BoardFragment1 extends Fragment {
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
    ArrayList<MemoDto> memoArrList; //recyclerview를 담을 빈 데이터 리스트 변수
    RecyclerView recyclerView; //recyclerview 객체 변수
    Fragment1Adapter fragment1Adapter;//recycler view와 연결할 Adapter 객체
    RecyclerView.LayoutManager layoutManager; //recyclerview 레이아웃매니저 객체 변수

    FloatingActionButton fbtnCreate; //추가 버튼 객체

    //Swipe Controller 관련 객체 변수 선언
    SwipeController swipeController; //swipe controller 객체 선언

    //Fragment에 쓰일 View를 초기화하는 메소드
    //LayoutInflater를 인자로 받아서 layout으로 설정한 XML을 연결하거나 bundle에 의한 작업을 함
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.board_fragment1, container, false);

        //Bundle에서 값 받아옴
        loginEmailKey = getArguments().getString("loginEmailKey");
        feedbackKey = getArguments().getString("feedbackKey");
        feedbackTag = getArguments().getString("feedbackTag");
        ONGOING_FEEDBACKLIST = getArguments().getString("ONGOING_FEEDBACKLIST");
        COMPLETE_FEEDBACKLIST = getArguments().getString("COMPLETE_FEEDBACKLIST");
        feedbackPosition = getArguments().getInt("feedbackPosition");

        //Toast 메세지를 보여주기 위한 context 선언
        context = container.getContext();

        //사용자 입력이 있을 객체를 초기화
        //Recycler View 관련 객체 초기화
        memoArrList = new ArrayList<MemoDto>(); //피드백 목록을 담을 빈 데이터 리스트 생성

        //MemoDao에서 친구 목록 불러와서 저장
        MemoDao memoDao = new MemoDao(getContext());
        //친구 목록이 있는지 없는지 확인
        //친구목록이 있는 경우(NONE을 반환하지 않은 경우)에만 친구목록 저장
        if (!memoDao.getMemoListValue(loginEmailKey, feedbackKey).equals("NONE")) {
            memoArrList = memoDao.readAllMemoInfo(loginEmailKey, feedbackKey);
        }


        recyclerView = (RecyclerView) view.findViewById(R.id.fragment1_recyclerview); //recyclerview 생성
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);//recycler view 아이템을 linear 형태로 보여줌
        recyclerView.setLayoutManager(layoutManager);//layout manager 설정
        fragment1Adapter = new Fragment1Adapter(memoArrList);
        recyclerView.setAdapter(fragment1Adapter);

        //화면 버튼 관련 객체 초기화
        fbtnCreate = (FloatingActionButton) view.findViewById(R.id.fragment1_fbtn_create);

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
                        //편집하는 메소드 호출
                        //수정 다이얼로그 객체 생성
                        Fragment1Dialog updateDialog = new Fragment1Dialog(context, memoArrList.get(position).getMemoTitle(),
                                memoArrList.get(position).getMemoContent(), position);

                        //다이얼로그 레이아웃 지정
                        updateDialog.setContentView(R.layout.fragment1_dialog);

                        //다이얼로그를 보여주기 전 리스너 등록
                        updateDialog.setFragment1DialogListener(new Fragment1Dialog.Fragment1DialogListener() {
                            //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
                            @Override
                            public void onCreateClicked(String etxtTitle, String etxtContent) {
                                //피드백 수정 메소드 호출
                                updateItemInRecyclerView(position, etxtTitle, etxtContent);
                            }
                        });

                        //다이얼로그 크기 조정
                        DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                        int width = displayMetrics.widthPixels;//디바이스 화면 너비
                        int height = displayMetrics.heightPixels;//디바이스 화면 높이

                        WindowManager.LayoutParams wm = updateDialog.getWindow().getAttributes();
                        wm.width = width;//화면의 가로 넓이
                        wm.height = height / 2;//화면의 세로 넓이
                        updateDialog.getWindow().setAttributes(wm);

                        //게시판 수정 다이얼로그 호출
                        updateDialog.show();
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
                    Fragment1Dialog createDialog = new Fragment1Dialog(context);
                    //다이얼로그 레이아웃 지정
                    createDialog.setContentView(R.layout.fragment1_dialog);

                    //다이얼로그를 보여주기 전 리스너 등록
                    createDialog.setFragment1DialogListener(new Fragment1Dialog.Fragment1DialogListener() {
                        //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
                        @Override
                        public void onCreateClicked(String etxtTitle, String etxtContext) {
                            createItemInRecyclerView(etxtTitle, etxtContext);//피드백 메소드 호출
                        }
                    });

                    //다이얼로그 크기 조정
                    DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                    int width = displayMetrics.widthPixels;//디바이스 화면 너비
                    int height = displayMetrics.heightPixels;//디바이스 화면 높이

                    WindowManager.LayoutParams wm = createDialog.getWindow().getAttributes();
                    wm.width = width;//화면의 가로 넓이
                    wm.height = height / 2;//화면의 세로 넓이
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
    private void createItemInRecyclerView(String memoTitle, String memoContent) {
        //새로운 MemoDao, MemoDto 객체 생성
        MemoDao memoDao = new MemoDao(getContext());
        MemoDto memoDto = new MemoDto();

        String memoDate = setMemoDate();

        //날짜를 포함한 문자열을 키 값으로 줌
        String tempMemoKey = loginEmailKey.replace("@", "_");
        String accountMemoKey = tempMemoKey.replace(".", "_");
        String memoKey = "memoKey_" + accountMemoKey + "_" + memoDate;

        //arrayList에 저장할 값을 friendDto에 집어넣음
        memoDto.setMemoItem(memoKey, memoTitle, memoContent, memoDate);

        //shaed preference에 메모 추가
        memoDao.insertOneMemoInfo(loginEmailKey, feedbackKey, memoDto);
        //firebase에 memo 정보 추가
        memoDao.insertOneMemoInfoToFirebase(loginEmailKey, feedbackKey, memoDto);


        //글쓰기 제목과 내용을 넣음
        memoArrList.add(memoDto);

        //데이터 변경을 알림
        fragment1Adapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(context, "게시글이 추가되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 수정하는 메소드
    private void updateItemInRecyclerView(int position, String memoTitle, String memoContent) {
        //새로운 MemoDao, MemoDto 객체 생성
        MemoDao memoDao = new MemoDao(getContext());
        MemoDto memoDto = new MemoDto();

        //arrayList에 저장할 값을 friendDto에 집어넣음
        memoDto.setMemoItem(memoArrList.get(position).getMemoKey(), memoTitle, memoContent, memoArrList.get(position).getMemoDate());

        //shaed preference에 저장된 피드백 수정
        memoDao.updateOneMemoInfo(loginEmailKey, feedbackKey, feedbackPosition, memoDto);
        //firebase에 memo 정보 수정
        memoDao.updateOneMemoInfoToFirebase(loginEmailKey, feedbackKey, memoDto);

        //아이템 수정
        memoArrList.set(position, memoDto);

        //데이터 변경을 알림
        fragment1Adapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(context, "게시글이 수정되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 삭제하는 메소드
    private void deleteItemInRecyclerView(int position) {
        //MemoDao에서 데이터 삭제 메소드 호출
        MemoDao memoDao = new MemoDao(getContext());
        memoDao.deleteOneMemoInfo(loginEmailKey, feedbackKey, memoArrList.get(position).getMemoKey());
        //firebase에서 memo 정보 삭제
        memoDao.deleteOneMemoInfoToFirebase(loginEmailKey, feedbackKey, memoArrList.get(position).getMemoKey());

        memoArrList.remove(position); //배열에서 아이템 삭제
        fragment1Adapter.notifyItemRemoved(position); //아이템 삭제 알림
        fragment1Adapter.notifyItemRangeChanged(position, fragment1Adapter.getItemCount()); //길이 달라졌음을 알림
    }

    //현재 날짜를 저장하는 메소드
    private String setMemoDate() {
        long nowTime = System.currentTimeMillis(); //현재 시간을 시스템에서 가져옴
        Date date = new Date(nowTime); //Date 생성하기
        SimpleDateFormat timeSDF = new SimpleDateFormat("yyyyMMdd_hhmmss");//yyyy-MM-dd 형식으로 가져옴
        return timeSDF.format(date);
    }
}
