package com.remindfeedbackproject.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.remindfeedbackproject.R;

//앱을 설명하는 IntroActivity를 띄우는 클래스
public class IntroActivity extends AppCompatActivity {
    String loginEmailKey;

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;

    //사용자 입력이 있을 widget을 선언
    private Button btnSkip;//Skip 버튼을 저장하는 변수
    private Button btnNext;//Next 버튼을 저장하는 변수

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_activity);

        loginEmailKey = getIntent().getStringExtra("loginEmailValue"); //intent로 받아온 값 저장

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.introLayoutDots);

        // 인트로 레이아웃 전부 배열에 추가
        layouts = new int[]{
                R.layout.intro_slide1,
                R.layout.intro_slide2,
                R.layout.intro_slide3,
                R.layout.intro_slide4,
                R.layout.intro_slide5,
                R.layout.intro_slide6};

        // 하단 점 추가
        addBottomDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        //해당하는 widget의 아이디를 연결
        btnSkip = (Button) findViewById(R.id.intro_btn_skip);
        btnNext = (Button) findViewById(R.id.intro_btn_next);
    }

    //Activity를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //최초 설명 화면이므로 스킵/계속 등을 할 수 있음
    @Override
    protected void onResume(){
        super.onResume();
        //Skip 버튼을 누르는 경우
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchIntroToMainScreen(); //메인 화면으로 이동함
            }
        });

        //Next 버튼을 누르는 경우
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 페이지를 넘김
                int current = getItem(+1); //페이지 숫자 증가
                //전체 레이아웃 길이보다 현재 페이지 숫자가 작은 경우
                if (current < layouts.length) {
                    // 다음 화면으로 넘어감
                    viewPager.setCurrentItem(current);
                }
                //만일 마지막 페이지인 경우
                else {
                    //메인 화면으로 전환하는 메소드 호출
                    launchIntroToMainScreen();
                }
            }
        });
    }

    //ViewPager Adapter 클래스
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    //  뷰페이저 변화 리스너
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        //페이지가 선택되었을 때
        public void onPageSelected(int position) {
            //하단에 점 추가
            addBottomDots(position);

            // NEXT 버튼의 텍스트를 Next에서 GOT IT으로 바꿈
            if (position == layouts.length - 1) {

                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };


    //아래쪽에 점 추가 메소드
    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length]; // 레이아웃의 길이만큼 생성

        int colorsActive = Color.parseColor("#2962ff");
        int colorsInactive = Color.parseColor("#616161");

        //뷰를 제거함
        dotsLayout.removeAllViews();
        //점을 추가
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive);
            dotsLayout.addView(dots[i]); //완성된 점을 추가
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    //메인 화면 호출 메소드
    private void launchIntroToMainScreen() {
        Intent intent = new Intent(IntroActivity.this, MainActivity.class); //새로운 인텐트 객체 생성
        intent.putExtra("loginEmailValue", loginEmailKey); //이메일 값을 intent로 전달
        startActivity(intent); //다른 액티비티로 화면 변경
        finish(); //액티비티 종료
    }
}