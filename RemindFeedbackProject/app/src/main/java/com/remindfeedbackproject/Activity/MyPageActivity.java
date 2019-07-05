package com.remindfeedbackproject.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.remindfeedbackproject.Dao.ExtraDao;
import com.remindfeedbackproject.Dialog.TitleDialog;
import com.remindfeedbackproject.Dto.ExtraDto;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dao.FriendDao;
import com.remindfeedbackproject.Dto.FriendDto;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dto.MemberDto;

public class MyPageActivity extends AppCompatActivity {
    //intent 객체 선언
    String loginEmailKey;
    String friendEmailKey;

    static final int EDIT_MYPAGE_REQUEST = 140; // The request code

    //사용자 입력이 있을 변수 선언
    ImageView imgProfilePhoto; //사용자 프로필 사진
    TextView txtNickName; //사용자 이름(별명)
    TextView txtEmail; //사용자 이메일
    TextView txtProfileMessage; //사용자 상태 메세지

    ImageButton ibtnEditProfile; //프로필 편집 버튼
    TextView txtEditBtnTitle; //프로필 편집 버튼 제목

    private Button btnDeleteAccount; //회원 탈퇴 버튼

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage_activity);

        //intent 값 저장
        loginEmailKey = getIntent().getStringExtra("loginEmailValue");
        friendEmailKey = getIntent().getStringExtra("friendEmailValue");

        //사용자 프로필 객체를 선언 및 초기화
        imgProfilePhoto = (ImageView) findViewById(R.id.mypage_img_profilephoto);
        txtNickName = (TextView) findViewById(R.id.mypage_txt_nickname);
        txtEmail = (TextView) findViewById(R.id.mypage_txt_email);
        txtProfileMessage = (TextView) findViewById(R.id.mypage_txt_profilemessage);

        ibtnEditProfile = (ImageButton) findViewById(R.id.mypage_ibtn_editprofile);
        txtEditBtnTitle = (TextView) findViewById(R.id.mypage_txt_editbtntitle);

        btnDeleteAccount = (Button) findViewById(R.id.setting_btn_deleteaccount);

        //만일 로그인 이메일 값만 가져온 경우
        if (friendEmailKey.equals("NONE")) {
            //Email 값을 기준으로 memberDao 값에서 회원 정보를 가져옴
            //MemberDao에서 이메일, 닉네임, 프로필 사진 경로, 프로필 상태 메세지(안보임) 가져옴
            MemberDao memberDao = new MemberDao(this);
            MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey); //로그인 당시 이메일 키를 통해 회원 정보 가져옴

            //기본 이미지가 없는 경우
            if (memberDto.getMemberProfilePhotoPath().equals("NONE")) {
                imgProfilePhoto.setImageResource(R.drawable.icon_account_48dp);
            }
            //기본 이미지가 있는 경우
            else {
                Bitmap bitmap = BitmapFactory.decodeFile(memberDto.getMemberProfilePhotoPath());
                imgProfilePhoto.setImageBitmap(bitmap);

                //이미지 버튼 둥글게 만들기
                imgProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                if (Build.VERSION.SDK_INT >= 21) {
                    imgProfilePhoto.setClipToOutline(true);
                }
            }

            txtNickName.setText(memberDto.getMemberNickName());
            txtEmail.setText(memberDto.getMemberEmail());
            txtProfileMessage.setText(memberDto.getMemberProfileMessage());

            //프로필 수정 버튼을 보이게 함
            ibtnEditProfile.setVisibility(View.VISIBLE);
            txtEditBtnTitle.setVisibility(View.VISIBLE);
        }
        //친구 이메일 값을 가져온 경우
        else {
            //FriendDao에서 선택한 친구 정보를 가지고 올 읽기 메소드 호출
            FriendDao friendDao = new FriendDao(this);
            FriendDto friendDto = friendDao.readOneFriendInfo(loginEmailKey, friendEmailKey);

            //기본 이미지가 없는 경우
            if (friendDto.getFriendProfilePhotoPath().equals("NONE")) {
                imgProfilePhoto.setImageResource(R.drawable.icon_account_48dp);
            }
            //기본 이미지가 있는 경우
            else {
                Bitmap bitmap = BitmapFactory.decodeFile(friendDto.getFriendProfilePhotoPath());
                imgProfilePhoto.setImageBitmap(bitmap);

                //이미지 버튼 둥글게 만들기
                imgProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                if (Build.VERSION.SDK_INT >= 21) {
                    imgProfilePhoto.setClipToOutline(true);
                }
            }

            txtNickName.setText(friendDto.getFriendNickName());
            txtEmail.setText(friendDto.getFriendEmail());
            txtProfileMessage.setText(friendDto.getFriendProfileMessage());
        }
    }

    //Activity를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //메인 화면이므로 마이페이지/로그아웃/게시판 생성/게시판에 들어가기 등을 할 수 있음
    @Override
    protected void onResume() {
        super.onResume();

        //프로필 수정을 위해 버튼을 누르는 경우
        //버튼 이벤트를 수행하는 리스너
        ibtnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //edit mypage activity로 화면 전환
                launchMyPageToEditMyPageScreen();
            }
        });

        //회원 탈퇴 버튼을 누르는 경우
        //버튼 이벤트를 받는 리스너
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //커스텀 다이얼로그를 띄워 회원 비밀번호를 다시 한 번 입력하게 함
                confirmPasswordCustomDialog();
            }
        });
    }

    //마이페이지 편집 화면 호출 메소드
    private void launchMyPageToEditMyPageScreen() {

        Intent intent = new Intent(MyPageActivity.this, EditMyPageActivity.class); //새로운 인텐트 객체 생성
        intent.putExtra("loginEmailValue", loginEmailKey); //이메일 값을 intent로 전달
        startActivityForResult(intent, EDIT_MYPAGE_REQUEST); //intent 호출
    }

    //호출한 액티비티로부터 결과값을 받아와 그에 따른 이벤트를 수행하는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //요청 코드가 닉네임 수정인 경우
        if (requestCode == EDIT_MYPAGE_REQUEST) {
            //이름(별명) 수정을 성공적으로 수행한 경우
            if (resultCode == RESULT_OK) {
                //프로필을 수정했기 때문에 수정한 프로필을 갱신
                //Email 값을 기준으로 memberDao 값에서 회원 정보를 가져옴
                //MemberDao에서 이메일, 닉네임, 프로필 사진 경로, 프로필 상태 메세지(안보임) 가져옴
                MemberDao memberDao = new MemberDao(this);
                MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);

                //기본 이미지가 없는 경우
                if (memberDto.getMemberProfilePhotoPath().equals("NONE")) {
                    imgProfilePhoto.setImageResource(R.drawable.icon_account_48dp);
                }
                //기본 이미지가 있는 경우
                else {
                    Bitmap bitmap = BitmapFactory.decodeFile(memberDto.getMemberProfilePhotoPath());
                    imgProfilePhoto.setImageBitmap(bitmap);

                    //이미지 버튼 둥글게 만들기
                    imgProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                    if (Build.VERSION.SDK_INT >= 21) {
                        imgProfilePhoto.setClipToOutline(true);
                    }
                }
                txtNickName.setText(memberDto.getMemberNickName());
                txtEmail.setText(memberDto.getMemberEmail());
                txtProfileMessage.setText(memberDto.getMemberProfileMessage());
            }
        }
    }

    //회원 탈퇴를 위해 비밀번호 입력 다이얼로그를 호출하는 메소드
    private void confirmPasswordCustomDialog() {
        //게시글 수정 다이얼로그 메소드 호출
        //다이얼로그 객체 생성
        final TitleDialog confirmPasswordDialog = new TitleDialog(MyPageActivity.this, "deleteAccount", "");
        //다이얼로그 레이아웃 지정
        confirmPasswordDialog.setContentView(R.layout.title_dialog);

        //다이얼로그를 보여주기 전 리스너 등록
        confirmPasswordDialog.setTitleDialogListener(new TitleDialog.TitleDialogListener() {
            //확인을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
            @Override
            public void onClicked(String state, String inputPassword) {
                //shared preference에서 사용자 비밀번호 가져옴
                MemberDao memberDao = new MemberDao(MyPageActivity.this);
                MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
                String memberPassword = memberDto.getMemberPassword();

                //사용자가 입력한 값이 회원 정보와 일치하는 경우
                if (memberPassword.equals(inputPassword)) {
                    confirmDeleteAccountDialog(); //다시 삭제 확인을 묻는 패스워드를 출력
                }
                //입력한 값이 회원 정보와 일치하지 않는 경우
                else {
                    //메세지 출력
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //다이얼로그 크기 조정
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
        int width = displayMetrics.widthPixels;//디바이스 화면 너비
        int height = displayMetrics.heightPixels;//디바이스 화면 높이

        WindowManager.LayoutParams wm = confirmPasswordDialog.getWindow().getAttributes();
        wm.width = width;//화면의 가로 넓이
        wm.height = height / 3;//화면의 세로 넓이
        confirmPasswordDialog.getWindow().setAttributes(wm);
        //게시판 생성 다이얼로그 호출
        confirmPasswordDialog.show();
    }

    //정말 회원에서 탈퇴할 건지 묻는 메소드
    private void confirmDeleteAccountDialog() {
        //친구 정보를 dialog에 띄워서 친구 추가할 건지 묻고, 추가한다고 하면 Friend Database에 추가
        //취소를 누르면 아무런 작업도 하지 않음
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("친구 정보");
        builder.setMessage("삭제된 회원 정보는 복구할 수 없습니다. 정말 회원에서 탈퇴하시겠습니까?");

        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //취소 메세지를 알림
                        Toast.makeText(getApplicationContext(), "회원 탈퇴를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //MemberDao에서 회원 탈퇴 메소드 호출
                        MemberDao memberDao = new MemberDao(MyPageActivity.this);
                        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);

                        //firebase에서 회원 정보 삭제
                        memberDao.deleteOneMemberInfoToFirebase(memberDto);
                        //shared preference에서 회원 정보 삭제
                        memberDao.deleteOneMemberInfo(loginEmailKey);

                        //자동로그인 체크했으면 해당 정보 모두 삭제
                        ExtraDao extraDao = new ExtraDao(MyPageActivity.this);
                        ExtraDto extraDto = extraDao.readAllExtraInfo();
                        extraDto.setAutoLoginConfirmKey("FALSE"); //자동 로그인 키를 false로 변경
                        extraDto.setAutoLoginEmail("FALSE"); //자동로그인 이메일 값 삭제
                        extraDto.setAutoLoginPassword("FALSE");//자동로그인 비밀번호 값 삭제
                        extraDao.updateExtraInfo(extraDto); //수정 메소드 호출

                        Toast.makeText(getApplicationContext(), "그동안 이용해주셔서 감사합니다.", Toast.LENGTH_SHORT).show();

                        //메인 화면으로 되돌아감
                        // intent 날리고 종료
                        Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
                        setResult(RESULT_FIRST_USER, intent);
                        startActivity(intent);
                        finish(); //설정 화면 종료
                    }
                });
        builder.show();
    }

    //뒤로가기 버튼을 누르는 경우
    @Override
    public void onBackPressed() {
        //intent 날리고 종료
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}
