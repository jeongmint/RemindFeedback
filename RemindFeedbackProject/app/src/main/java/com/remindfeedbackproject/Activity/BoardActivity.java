package com.remindfeedbackproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.remindfeedbackproject.Dao.FeedbackDao;
import com.remindfeedbackproject.Fragment.BoardFragment1;
import com.remindfeedbackproject.Fragment.BoardFragment2;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Fragment.BoardFragment3;
import com.remindfeedbackproject.Fragment.BoardFragment4;

public class BoardActivity extends AppCompatActivity {
    //intent 객체 선언
    String loginEmailKey;
    String feedbackKey;
    String feedbackTag;
    String ONGOING_FEEDBACKLIST;
    String COMPLETE_FEEDBACKLIST;

    //사용자 입력이 있을 widget을 변수
    Toolbar toolbar; //커스텀 툴바 변수
    BottomNavigationView btmNavView;//하단의 bottom navigation bar 변수

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_activity);

        //intent값 저장
        loginEmailKey = getIntent().getStringExtra("loginEmailKey");
        feedbackKey = getIntent().getStringExtra("feedbackKey");
        feedbackTag = getIntent().getStringExtra("feedbackTag");
        ONGOING_FEEDBACKLIST = getIntent().getStringExtra( "ONGOING_FEEDBACKLIST");
        COMPLETE_FEEDBACKLIST = getIntent().getStringExtra("COMPLETE_FEEDBACKLIST");


        //사용자 입력이 있을 widget을 선언
        //toolbar 관련 객체 선언
        toolbar = (Toolbar) findViewById(R.id.board_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //툴바에 홈버튼 활성화
        getSupportActionBar().setDisplayShowHomeEnabled(true); //뒤로가기 버튼 활성화


        //bottom navigation bar의 이벤트 리스너 선언
        BottomNavigationView.OnNavigationItemSelectedListener onNavItmSelListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    //오늘의 피드백 버튼을 선택한 경우
                    case R.id.btmnavview_itm_today:
                        //해당 화면을 갱신
                        displayView(0);
                        return true;
                    //피드백 사진을 선택하는 경우
                    case R.id.btmnavview_itm_img:
                        //해당 화면을 갱신
                        displayView(1);
                        return true;
                    //피드백 녹음을 선택하는 경우
                    case R.id.btmnavview_itm_record:
                        //해당 화면을 갱신
                        displayView(2);
                        return true;
                    //피드백 영상을 선택하는 경우
                    case R.id.btmnavview_itm_video:
                        //해당 화면을 갱신
                        displayView(3);
                        return true;
                }
                return false;
            }
        };

        //bottom navigation view 선언
        btmNavView = (BottomNavigationView) findViewById(R.id.board_btmnavview);
        btmNavView.setOnNavigationItemSelectedListener(onNavItmSelListener);

        //만일 saveInstanceState가 null인 경우
        if (savedInstanceState == null) {
            displayView(0);
        }

        //상단 액션 바의 이름을 선택한 게시판 이름으로 바꿈
        changeActionBarName(toolbar);
    }

    //Activity를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //게시판 화면이므로 오늘의 피드백/피드백목록/사진/녹음/영상 등을 할 수 있음
    @Override
    protected void onResume() {
        super.onResume();
    }

    //actionbar 이름을 변경하는 메소드
    private void changeActionBarName(Toolbar toolbar) {
        //이전 화면에서 건네받은 게시판 이름을 intent로 가져오기
        //게시판 이름 intent는 boardPosition
        Intent intent = getIntent(); //intent 선언
        //String 변수로 게시판 이름을 가져옴
        String boardTitle = intent.getStringExtra("boardPosition");

        //actionbar 이름 변경
        getSupportActionBar().setTitle(boardTitle);
    }

    //실제로 Fragment를 화면에 보여주는 역할을 하는 메소드
    private void displayView(int position) {
        Fragment fragment = null;
        Bundle bundle = new Bundle(1); //전달할 데이터 개수는 1
        bundle.putString("loginEmailKey", loginEmailKey); //key, value
        bundle.putString("feedbackKey", feedbackKey); //key, value
        bundle.putString("feedbackTag", feedbackTag); //key, value
        bundle.putString("ONGOING_FEEDBACKLIST", ONGOING_FEEDBACKLIST); //key, value
        bundle.putString("COMPLETE_FEEDBACKLIST", COMPLETE_FEEDBACKLIST); //key, value

        //feedback key 값으로 FeedbackDao에서 피드백 위치 가져옴
        FeedbackDao feedbackDao = new FeedbackDao(BoardActivity.this);
        int feedbackPosition = feedbackDao.readOneFeedbackPosition(loginEmailKey, feedbackKey);
        bundle.putInt("feedbackPosition", feedbackPosition); //key, value

        switch (position) {
            case 0:
                fragment = new BoardFragment1();
                fragment.setArguments(bundle);
                break;
            case 1:
                fragment = new BoardFragment2();
                fragment.setArguments(bundle);
                break;
            case 2:
                fragment = new BoardFragment3();
                fragment.setArguments(bundle);
                break;
            case 3:
                fragment = new BoardFragment4();
                fragment.setArguments(bundle);
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.board_layout_frame, fragment);
            ft.commit();
        }
    }

    //optionManu를 생성하는 메소드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //만일 아이템이 존재한다면 액션 바에 메뉴에 올림
        getMenuInflater().inflate(R.menu.board_menu, menu);
        return true;
    }

    //액션 바에서 아이템을 클릭했을 때 호출되는 메소드
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //액션 바에서 아이템 클릭했을 때 정의
        int id = item.getItemId();

        //홈버튼(뒤로가기를 클릭했을 때
        if(id == android.R.id.home){
            //액티비티 종료
            finish();
        }

        //setting이 아닌 값을 선택했으면 선택한 값에 따른 행동을 하도록 함
        return super.onOptionsItemSelected(item);
    }
}