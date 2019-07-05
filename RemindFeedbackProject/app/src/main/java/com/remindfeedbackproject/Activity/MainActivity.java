package com.remindfeedbackproject.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remindfeedbackproject.Etc.NetworkStatus;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Adapter.MainAdapter;
import com.remindfeedbackproject.Etc.SwipeController;
import com.remindfeedbackproject.Etc.SwipeControllerAction;
import com.remindfeedbackproject.Dao.ExtraDao;
import com.remindfeedbackproject.Dao.FeedbackDao;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dto.ExtraDto;
import com.remindfeedbackproject.Dto.FeedbackDto;
import com.remindfeedbackproject.Dto.MemberDto;
import com.remindfeedbackproject.Dialog.MainDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //intent 객체 선언
    String loginEmailKey;

    static final int MYPAGE_INFO_REQUEST = 110; // The request code
    static final int DELETE_ACCOUNT_REQUEST = 111; //회원 탈퇴 코드

    private String ONGOING_FEEDBACKLIST = "TRUE"; //진행 중인 피드백 목록(기본값)
    private String COMPLETE_FEEDBACKLIST = "FALSE"; //완료한 피드백 목록

    //사용자 입력이 있을 변수 선언
    //Recycler View 관련 객체 변수 선언
    ArrayList<FeedbackDto> feedbackArrList; //recyclerview를 담을 빈 데이터 리스트 변수
    ArrayList<FeedbackDto> copyArrList; //List의 모든 데이터를 복사할 데이터 리스트 변수
    RecyclerView recyclerView; //recyclerview 객체 변수
    MainAdapter mainAdapter;//recycler view와 연결할 Adapter 객체
    RecyclerView.LayoutManager layoutManager; //recyclerview 레이아웃매니저 객체 변수

    //Swipe Controller 관련 객체 변수 선언
    SwipeController swipeController; //swipe controller 객체 선언
    ItemTouchHelper itemTouchHelper; //item touch helper 객체 선언
    Toolbar mainToolbar; //메인 액티비티 커스텀 toolbar 변수

    DrawerLayout drawerLayout; //열리고 닫힐 좌측 drawer layout 변수
    ActionBarDrawerToggle drawerToggle; //drawer layout의 액션바 toggle 변수
    NavigationView navigationView; //상단의 navigation View 변수
    FloatingActionButton fbtnCreate; //게시판 추가 버튼 변수

    //알람 표시 관련 객체 변수 선언
    private FrameLayout frameLayoutCircle;
    private TextView txtCount;
    private int alertCount = 0;

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //intent값 저장
        loginEmailKey = getIntent().getStringExtra("loginEmailValue");

        //사용자 입력이 있을 객체를 초기화
        //TAB 관련 객체 초기화
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_layout_tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                changeTabView(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        //RecyclerView 관련 객체 초기화
        feedbackArrList = new ArrayList<FeedbackDto>(); //피드백 목록을 담을 빈 데이터 리스트 생성
        copyArrList = new ArrayList<FeedbackDto>(); //피드백 목록을 카피할 빈 데이터 리스트 생성

        FeedbackDao feedbackDao = new FeedbackDao(this);

        //피드백 목록이 있는지 없는지 확인
        //피드백 목록이 있는 경우(NONE을 반환하지 않은 경우)에만 진행 중인 피드백 목록 저장
        if (!feedbackDao.getFeedbackListValue(loginEmailKey).equals("NONE")) {
            ArrayList<FeedbackDto> onGoingFeedbackArrList = feedbackDao.readAllFeedbackInfo(loginEmailKey);

            //피드백 정보 불러와서 저장
            for(int i=0; i<onGoingFeedbackArrList.size(); i++) {
                if (onGoingFeedbackArrList.get(i).getFeedbackTag().equals("진행중")) {
                    feedbackArrList.add(onGoingFeedbackArrList.get(i));
                    copyArrList.add(onGoingFeedbackArrList.get(i));
                }
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.main_recyclerview); //recyclerview 생성
        layoutManager = new LinearLayoutManager(this);//recycler view 아이템을 linear 형태로 보여줌

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);//layout manager 설정
        mainAdapter = new MainAdapter(feedbackArrList);
        recyclerView.setAdapter(mainAdapter);

        //Recycler view의 아이템을 누르는 경우
        mainAdapter.setItemClick(new MainAdapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
                //아이템을 누르는 경우 게시판 화면 전환 메소드 호출
                launchMainToBoardScreen(position);
            }
        });

        //Recycler view의 아이템을 길게 누르는 경우
        mainAdapter.setItemLongClick(new MainAdapter.ItemLongClick() {
            @Override
            public void onLongClick(View view, final int position) {
                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                    //완료 확인 다이얼로그 호출
                    completeConfirmAlertDialog(position);
                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어있지 않아 피드백의 상태를 변경할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        });

        //SwipeController 관련 객체 초기화
        swipeController = new SwipeController(new SwipeControllerAction() {
            //왼쪽 클릭하면 편집
            @Override
            public void onLeftClicked(final int position) {
                //인터넷 연결 확인하고 연결이 안 되어있으면 생성 불가능하게 함
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 생성 다이얼로그를 염
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {

                    //만일 진행 중인 피드백 목록 탭을 선택한 경우
                    if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
                        //편집하는 메소드 호출
                        //수정 다이얼로그 객체 생성
                        MainDialog updateDialog = new MainDialog(MainActivity.this, "update", loginEmailKey,
                                feedbackArrList.get(position).getFeedbackTitle(), feedbackArrList.get(position).getAdviserEmail(),
                                feedbackArrList.get(position).getAdviserNickName(), feedbackArrList.get(position).getAdviserPhotoPath());

                        //다이얼로그 레이아웃 지정
                        updateDialog.setContentView(R.layout.main_dialog);
                        updateDialog.setCancelable(false); //화면 외부 클릭해도 종료되지 않게 함

                        //다이얼로그를 보여주기 전 리스너 등록
                        updateDialog.setMainDialogListener(new MainDialog.MainDialogListener() {
                            //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
                            @Override
                            public void onClicked(String feedbackTitle, String adviserEmail, String adviserNickName, String adviserPhotoPath) {
                                //피드백 수정 메소드 호출
                                updateItemInRecyclerView(position, feedbackTitle, adviserEmail, adviserNickName, adviserPhotoPath);
                            }
                        });

                        //다이얼로그 크기 조정
                        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                        int width = displayMetrics.widthPixels;//디바이스 화면 너비
                        int height = displayMetrics.heightPixels;//디바이스 화면 높이

                        WindowManager.LayoutParams wm = updateDialog.getWindow().getAttributes();
                        wm.width = width;//화면의 가로 넓이
                        wm.height = (height / 7) * 3;//화면의 세로 넓이
                        updateDialog.getWindow().setAttributes(wm);

                        //게시판 수정 다이얼로그 호출
                        updateDialog.show();
                    }
                    //만일 완료한 피드백 목록 탭을 선택한 경우
                    else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
                        //메세지를 띄움
                        Toast.makeText(getApplicationContext(), "완료한 피드백 목록에서는 피드백을 수정할 수 없습니다!", Toast.LENGTH_SHORT).show();
                    }
                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어있지 않아 피드백을 수정할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

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
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {

                    //삭제 확인하는 alert dialog 불러옴
                    delConfirmAlertDialog(position);
                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어있지 않아 피드백을 삭제할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

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

        //toolbar 관련 객체 초기화
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar); //SupportActionBar 사용

        //화면 버튼 관련 객체 초기화
        fbtnCreate = (FloatingActionButton) findViewById(R.id.main_fbtn_create);

        //drawerLayout 관련 객체 초기화
        drawerLayout = (DrawerLayout) findViewById(R.id.main_layout_drawer);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        //내비게이션 뷰 관련 객체 초기화
        navigationView = (NavigationView) findViewById(R.id.main_navview);
        navigationView.setNavigationItemSelectedListener(this);

        //drawerLayout profile 설정 메소드 호출
        setNavheaderProfile();
    }

    //Activity를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //메인 화면이므로 마이페이지/로그아웃/게시판 생성/게시판에 들어가기 등을 할 수 있음
    @Override
    protected void onResume() {
        super.onResume();

        //게시판 생성(+) 버튼을 누르는 경우
        fbtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //인터넷 연결 확인하고 연결이 안 되어있으면 생성 불가능하게 함
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 생성 다이얼로그를 염
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {

                    //다이얼로그 객체 생성
                    MainDialog createDialog = new MainDialog(MainActivity.this, "create", loginEmailKey);

                    //다이얼로그 레이아웃 지정
                    createDialog.setContentView(R.layout.main_dialog);
                    createDialog.setCancelable(false); //화면 외부 클릭해도 종료되지 않게 함

                    //다이얼로그를 보여주기 전 리스너 등록
                    createDialog.setMainDialogListener(new MainDialog.MainDialogListener() {
                        //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
                        @Override
                        public void onClicked(String feedbackTitle, String adviserEmail, String adviserNickName, String adviserPhotoPath) {
                            createItemInRecyclerView(feedbackTitle, adviserEmail, adviserNickName, adviserPhotoPath);//피드백 메소드 호출
                        }
                    });

                    //다이얼로그 크기 조정
                    DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                    int width = displayMetrics.widthPixels;//디바이스 화면 너비
                    int height = displayMetrics.heightPixels;//디바이스 화면 높이

                    WindowManager.LayoutParams wm = createDialog.getWindow().getAttributes();
                    wm.width = width;//화면의 가로 넓이
                    wm.height = (height / 7) * 3;//화면의 세로 넓이
                    createDialog.getWindow().setAttributes(wm);

                    //게시판 생성 다이얼로그 호출
                    createDialog.show();
                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어있지 않아 피드백을 생성 할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    //Tab layout을 클릭했을 때 탭 화면의 내용을 변경하는 메소드
    private void changeTabView(int position) {
        //내 피드백 목록을 보여줌
        FeedbackDao feedbackDao = new FeedbackDao(this);

        switch (position) {
            //진행 중인 피드백 목록을 선택한 경우
            case 0:
                ONGOING_FEEDBACKLIST = "TRUE"; //진행 중 피드백 플래그를 true로 설정
                COMPLETE_FEEDBACKLIST = "FALSE"; //완료 피드백 플래그를 false로 설정

                //피드백 목록에 있는 내용 싹 다 삭제
                feedbackArrList.clear();
                copyArrList.clear();

                //피드백 목록이 있는지 없는지 확인
                //피드백 목록이 있는 경우(NONE을 반환하지 않은 경우)에만 친구목록 저장
                if (!feedbackDao.getFeedbackListValue(loginEmailKey).equals("NONE")) {
                    ArrayList<FeedbackDto> onGoingFeedbackArrList = feedbackDao.readAllFeedbackInfo(loginEmailKey);

                    //피드백 정보 불러와서 저장
                    for(int i=0; i<onGoingFeedbackArrList.size(); i++) {
                        if (onGoingFeedbackArrList.get(i).getFeedbackTag().equals("진행중")) {
                            feedbackArrList.add(onGoingFeedbackArrList.get(i));
                            copyArrList.add(onGoingFeedbackArrList.get(i));
                        }
                    }

                    //데이터 변경을 알림
                    mainAdapter.notifyDataSetChanged();
                }
                //피드백 목록이 없는 경우
                else {
                    //피드백 목록에 있는 내용 싹 다 삭제
                    feedbackArrList.clear();
                    copyArrList.clear();
                }
                fbtnCreate.show(); //새 피드백 추가 버튼 보이게 함

                break;
            //완료한 피드백 목록을 선택한 경우
            case 1:
                ONGOING_FEEDBACKLIST = "FALSE"; //진행중 피드백 플래그를 false로 설정
                COMPLETE_FEEDBACKLIST = "TRUE"; //완료 피드백 플래그를 true로 설정

                //피드백 목록에 있는 내용 싹 다 삭제
                feedbackArrList.clear();
                copyArrList.clear();

                //피드백 목록이 있는지 없는지 확인
                //피드백 목록이 있는 경우(NONE을 반환하지 않은 경우)에만 친구목록 저장
                if (!feedbackDao.getFeedbackListValue(loginEmailKey).equals("NONE")) {
                    ArrayList<FeedbackDto> completeFeedbackArrList = feedbackDao.readAllFeedbackInfo(loginEmailKey);

                    //피드백 정보 불러와서 저장
                    for(int i=0; i<completeFeedbackArrList.size(); i++) {
                        if (completeFeedbackArrList.get(i).getFeedbackTag().equals("완료")) {
                            feedbackArrList.add(completeFeedbackArrList.get(i));
                            copyArrList.add(completeFeedbackArrList.get(i));
                        }
                    }

                    //데이터 변경을 알림
                    mainAdapter.notifyDataSetChanged();
                }
                //피드백 목록이 없는 경우
                else {
                    //피드백 목록에 있는 내용 싹 다 삭제
                    feedbackArrList.clear();
                    copyArrList.clear();
                }
                fbtnCreate.hide(); //새 피드백 추가 버튼 사라지게 함

                break;
        }
    }

    //drawer layout 프로필 설정 메소드
    private void setNavheaderProfile() {
        View rootView = (View) navigationView.getHeaderView(0);

        TextView txtNickName = rootView.findViewById(R.id.main_navheader_txt_nickname);
        TextView txtEmail = rootView.findViewById(R.id.main_navheader_txt_email);

        //MemberDao에서 이메일, 닉네임, 프로필 사진 경로, 프로필 상태 메세지(안보임) 가져옴
        MemberDao memberDao = new MemberDao(this);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey); //로그인 당시 이메일 키를 통해 회원 정보 가져옴

        txtNickName.setText(memberDto.getMemberNickName());
        txtEmail.setText(memberDto.getMemberEmail());
    }

    //optionManu를 생성하는 메소드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //만일 아이템이 존재한다면 액션 바에 메뉴에 올림
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //검색 버튼을 클릭했을 때 searchview 길이 꽉 차게 늘림
        SearchView searchView = (SearchView) menu.findItem(R.id.main_action_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        //검색 버튼을 클릭했을 때 searchview에 힌트 추가
        searchView.setQueryHint("피드백 제목으로 검색합니다.");

        //검색 리스너 구현
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //검색어 입력 시 이벤트 제어
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //검색어 입력 완료 시 이벤트 제어
            @Override
            public boolean onQueryTextChange(String newText) {
                //대문자를 소문자로 바꿔서 저장
                String charText = newText.toLowerCase(Locale.getDefault());

                search(charText); //검색을 수행하는 메소드 호출
                return false;
            }
        });

        return true;
    }

    //액션 바에서 아이템을 클릭했을 때 호출되는 메소드
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //액션 바에서 아이템 클릭했을 때 정의
        switch (item.getItemId()) {
            //메뉴에서 search를 선택한 경우
            case R.id.main_action_search:
                Toast.makeText(this, "검색을 시작합니다.", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        //선택한 값의 결과 리턴
        return super.onOptionsItemSelected(item);
    }

    //네비게이션 바에서 아이템을 클릭했을 때 호출되는 메소드
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // 네비게이션 뷰 다룸
        int id = item.getItemId();

        //친구 목록을 누른 경우
        if (id == R.id.main_navdrawer_friendlist) {
            //친구 목록 화면 전환 메소드 호출
            launchMainToFriendListScreen();
        }

        //마이페이지를 누른 경우
        if (id == R.id.main_navdrawer_mypage) {
            //마이페이지 화면 전환 메소드 호출
            launchMainToMyPageScreen();
        }

        //로그아웃을 누른 경우
        else if (id == R.id.main_navdrawer_logout) {
            //자동로그인 체크했으면 해제
            ExtraDao extraDao = new ExtraDao(MainActivity.this);
            ExtraDto extraDto = extraDao.readAllExtraInfo();
            if (extraDto.getAutoLoginConfirmKey().equals("TRUE")) {
                extraDto.setAutoLoginConfirmKey("FALSE"); //자동 로그인 키를 false로 변경
                extraDto.setAutoLoginEmail("FALSE"); //자동로그인 이메일 값 삭제
                extraDto.setAutoLoginPassword("FALSE");//자동로그인 비밀번호 값 삭제
                extraDao.updateExtraInfo(extraDto); //수정 메소드 호출
            }
            //새로 로그인 액티비티 띄움
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); //메인화면 종료
        }

        //뭔가를 선택했으면 Drawer를 닫음
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //검색 메소드
    public void search(String charText) {
        //문자 입력할 때마다 리스트를 지우고 새로 뿌린다.
        feedbackArrList.clear();

        //문자 입력이 없을 때는 모든 데이터를 보여준다.
        if (charText.length() == 0) {
            feedbackArrList.addAll(copyArrList);
        }
        //문자 입력을 하는 경우
        else {
            for (int i = 0; i < copyArrList.size(); i++) {
                //arraylist의 모든 데이터에 입력받은 단어가 포함되어 있으면 true 반환
                if (copyArrList.get(i).getFeedbackTitle().toLowerCase().contains(charText)) {
                    //검색된 데이터를 리스트에 추가
                    feedbackArrList.add(copyArrList.get(i));
                }
            }
        }
        //리스트의 데이터가 변경되었기 때문에 adapter를 갱신해서 검색된 데이터를 화면에 보여줌
        mainAdapter.notifyDataSetChanged();
    }

    //recycler view 아이템 완료 확인하는 dialog 띄우는 메소드
    private void completeConfirmAlertDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("피드백 완료"); //다이얼로그 제목

        //내 피드백 목록 탭을 선택한 경우
        if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
            alertDialogBuilder.setMessage("정말 내가 작성한 피드백을 완료하시겠습니까?");
        }
        //친구의 피드백 목록 탭을 선택한 경우
        else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
            alertDialogBuilder.setMessage("완료한 피드백을 다시 진행 중으로 변경하시겠습니까?");
        }

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
                //진행중 피드백 목록 탭을 선택한 경우
                if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
                    //피드백 상태 변경 메소드 호출
                    changeStateFromItemInRecyclerView(position);
                }
                //친구의 피드백 목록 탭을 선택한 경우
                else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
                    //피드백 상태변경 메소드 호출
                    changeStateFromItemInRecyclerView(position);
                }
            }
        });
        alertDialogBuilder.show();//alert dialog 보여줌
    }

    //피드백을 완료하는 메소드
    private void changeStateFromItemInRecyclerView(int position) {
        //새로운 FeedbackDao, FeedbackDto 객체 생성
        FeedbackDao feedbackDao = new FeedbackDao(this);
        FeedbackDto feedbackDto = new FeedbackDto();

        //내 피드백 목록 탭을 선택한 경우
        if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
            //수정할 피드백 정보 가져와서 피드백 상태 수정
            feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackArrList.get(position).getFeedbackKey());
            feedbackDto.setFeedbackTag("완료"); //진행중 -> 완료로 상태 변경

            //피드백 업데이트 메소드 호출
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto);
            //firebase에 상태 변경 메소드 호출
            feedbackDao.updateOneFeedbackInfoToFirebase(loginEmailKey, feedbackDto);

            //아이템 수정
            feedbackArrList.set(position, feedbackDto);
            copyArrList.set(position, feedbackDto);

            //데이터 변경을 알림
            mainAdapter.notifyDataSetChanged();

            //다시 피드백 화면 변경
            changeTabView(0);

            //아이템이 추가되었다는 메세지 화면에 띄우기
            Toast.makeText(getBaseContext(), "피드백이 완료되었습니다.", Toast.LENGTH_LONG).show();
        }
        //완료한 피드백 목록 탭을 선택한 경우
        else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
            //수정할 피드백 정보 가져와서 피드백 상태 수정
            feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackArrList.get(position).getFeedbackKey());
            feedbackDto.setFeedbackTag("진행중"); //완료 -> 진행중으로 상태 변경

            //피드백 업데이트 메소드 호출
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto);
            //firebase에 상태 변경 메소드 호출
            feedbackDao.updateOneFeedbackInfoToFirebase(loginEmailKey, feedbackDto);

            //아이템 수정
            feedbackArrList.set(position, feedbackDto);
            copyArrList.set(position, feedbackDto);

            //데이터 변경을 알림
            mainAdapter.notifyDataSetChanged();

            //다시 피드백 화면 변경
            changeTabView(1);

            //아이템이 추가되었다는 메세지 화면에 띄우기
            Toast.makeText(getBaseContext(), "완료한 피드백을 진행 중으로 변경하였습니다.", Toast.LENGTH_LONG).show();
        }
    }

    //recycler view 아이템 삭제 확인하는 dialog 띄우는 메소드
    private void delConfirmAlertDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("피드백 삭제");
        alertDialogBuilder.setMessage("정말 내가 작성한 피드백을 삭제하시겠습니까?");

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
    private void createItemInRecyclerView(String feedbackTitle, String adviserEmail, String adviserNickName, String adviserPhotoPath) {
        //새로운 FeedbackDao, FeedbackDto 객체 생성
        FeedbackDao feedbackDao = new FeedbackDao(this);
        FeedbackDto feedbackDto = new FeedbackDto();

        String feedbackDate = setFeedbackDate();

        //날짜를 포함한 문자열을 키 값으로 줌
        String tempFeedbackKey = loginEmailKey.replace("@", "_");
        String accountFeedbackKey = tempFeedbackKey.replace(".", "_");
        String feedbackKey = "feedbackKey_" + accountFeedbackKey + "_" + feedbackDate;

        //arrayList에 저장할 값을 friendDto에 집어넣음
        feedbackDto.setFeedbackItem(feedbackKey, "진행중", feedbackTitle, feedbackDate, "myFeedback",
                adviserEmail, adviserNickName, adviserPhotoPath, "NONE", "NONE", "NONE", "NONE");

        //shared preference에 피드백 추가 메소드 호출
        feedbackDao.insertFeedbackInfo(loginEmailKey, feedbackDto); //shaed preference에 피드백 추가

        //firebase에 피드백 추가 메소드 호출
        feedbackDao.insertOneFeedbackInfoToFirebase(loginEmailKey, feedbackDto);

        //글쓰기 제목과 내용을 넣음
        feedbackArrList.add(feedbackDto);
        copyArrList.add(feedbackDto);

        //데이터 변경을 알림
        mainAdapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(getBaseContext(), "피드백이 추가되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 수정하는 메소드
    private void updateItemInRecyclerView(int position, String feedbackTitle,
                                          String adviserEmail, String adviserNickName, String adviserPhotoPath) {
        //새로운 FeedbackDao, FeedbackDto 객체 생성
        FeedbackDao feedbackDao = new FeedbackDao(this);
        FeedbackDto feedbackDto = new FeedbackDto();

        //arrayList에 저장할 값을 friendDto에 집어넣음
        feedbackDto.setFeedbackItem(feedbackArrList.get(position).getFeedbackKey(), feedbackArrList.get(position).getFeedbackTag(),
                feedbackTitle, feedbackArrList.get(position).getFeedbackDate(), feedbackArrList.get(position).getFeedbackBackground(),
                adviserEmail, adviserNickName, adviserPhotoPath,
                feedbackArrList.get(position).getMemoInfo(), feedbackArrList.get(position).getPhotoInfo(),
                feedbackArrList.get(position).getVoiceInfo(), feedbackArrList.get(position).getVideoInfo());

        //MemberDao에서 피드백 수정 메소드 호출
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //shaed preference에 저장된 피드백 수정

        //firebase에 피드백 수정 메소드 호출
        feedbackDao.updateOneFeedbackInfoToFirebase(loginEmailKey, feedbackDto);

        //아이템 수정
        feedbackArrList.set(position, feedbackDto);
        copyArrList.set(position, feedbackDto);

        //데이터 변경을 알림
        mainAdapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(getBaseContext(), "피드백이 수정되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 삭제하는 메소드
    private void deleteItemInRecyclerView(int position) {
        //FeedbackDao에서 데이터 삭제 메소드 호출
        FeedbackDao feedbackDao = new FeedbackDao(this);

        //firebase에서 피드백 삭제
        feedbackDao.deleteOneFeedbackInfoToFirebase(loginEmailKey, feedbackArrList.get(position));
        //shared preference에서 피드백 삭제
        feedbackDao.deleteOneFeedbackInfo(loginEmailKey, feedbackArrList.get(position).getFeedbackKey());

        //아이템 삭제
        feedbackArrList.remove(position); //배열에서 아이템 삭제
        copyArrList.remove(position); //배열에서 아이템 삭제

        //데이터 변경을 알림
        mainAdapter.notifyItemRemoved(position); //아이템 삭제 알림
        mainAdapter.notifyItemRangeChanged(position, mainAdapter.getItemCount()); //길이 달라졌음을 알림
    }

    //피드백 게시판 화면 전환 메소드
    //게시판의 값을 다음 화면으로 넘김
    private void launchMainToBoardScreen(int position) {
        Intent intent = new Intent(MainActivity.this, BoardActivity.class);
        //뒤로가기 버튼을 누르면 다시 게시판 화면이 나타날 수 있도록 finish 실행하지 않음
        //피드백 제목을 intent에 담음

        //로그인 한 사람의 이메일이 loginEmailKey가 됨
        intent.putExtra("loginEmailKey", loginEmailKey);
        intent.putExtra("feedbackKey", feedbackArrList.get(position).getFeedbackKey());
        intent.putExtra("feedbackTag", feedbackArrList.get(position).getFeedbackTag());
        intent.putExtra("ONGOING_FEEDBACKLIST", ONGOING_FEEDBACKLIST);
        intent.putExtra("COMPLETE_FEEDBACKLIST", COMPLETE_FEEDBACKLIST);
        intent.putExtra("boardPosition", feedbackArrList.get(position).getFeedbackTitle());
        startActivity(intent); //액티비티 전환
    }

    //친구목록 화면 전환 메소드
    private void launchMainToFriendListScreen() {
        Intent intent = new Intent(MainActivity.this, FriendListActivity.class); //새로운 인텐트 객체 생성
        intent.putExtra("loginEmailValue", loginEmailKey); //이메일 값을 intent로 전달
        startActivity(intent); //액티비티 전환
    }

    //마이페이지 화면 전환 메소드
    private void launchMainToMyPageScreen() {
        Intent intent = new Intent(MainActivity.this, MyPageActivity.class); //새로운 인텐트 객체 생성
        intent.putExtra("loginEmailValue", loginEmailKey); //이메일 값을 intent로 전달
        intent.putExtra("friendEmailValue", "NONE"); //친구 이메일 값(실제로는 공백)을 intent로 전달
        startActivityForResult(intent, MYPAGE_INFO_REQUEST);
    }

    //뒤로가기 버튼을 누른 경우 호출하는 메소드
    @Override
    public void onBackPressed() {
        //drawerLayout이 펼쳐진 상태일 때 뒤로가기 버튼을 누르면 닫히게 함
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //호출한 액티비티로부터 결과값을 받아와 그에 따른 이벤트를 수행하는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //요청 코드가 닉네임 수정인 경우
        if (requestCode == MYPAGE_INFO_REQUEST) {
            //이름(별명) 수정을 성공적으로 수행한 경우
            if (resultCode == RESULT_OK) {
                setNavheaderProfile(); //header profile 업데이트
            }
            //요청 코드가 회원 탈퇴인 경우
            if(resultCode == RESULT_FIRST_USER){
                //새로 로그인 액티비티 띄움
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); //액티비티 종료
            }
        }
    }

    //현재 날짜를 저장하는 메소드
    private String setFeedbackDate() {
        long nowTime = System.currentTimeMillis(); //현재 시간을 시스템에서 가져옴
        Date date = new Date(nowTime); //Date 생성하기
        SimpleDateFormat timeSDF = new SimpleDateFormat("yyyyMMdd_hhmmss");//yyyy-MM-dd 형식으로 가져옴
        return timeSDF.format(date);
    }
}
