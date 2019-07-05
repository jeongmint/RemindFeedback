package com.remindfeedbackproject.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.remindfeedbackproject.Dao.FeedbackDao;
import com.remindfeedbackproject.Dao.FriendDao;
import com.remindfeedbackproject.Dao.MemoDao;
import com.remindfeedbackproject.Dao.PhotoDao;
import com.remindfeedbackproject.Dao.VideoDao;
import com.remindfeedbackproject.Dao.VoiceDao;
import com.remindfeedbackproject.Dto.FeedbackDto;
import com.remindfeedbackproject.Dto.FriendDto;
import com.remindfeedbackproject.Dto.MemoDto;
import com.remindfeedbackproject.Dto.PhotoDto;
import com.remindfeedbackproject.Dto.VideoDto;
import com.remindfeedbackproject.Dto.VoiceDto;
import com.remindfeedbackproject.Etc.NetworkStatus;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dao.ExtraDao;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dialog.TitleDialog;
import com.remindfeedbackproject.Dto.ExtraDto;
import com.remindfeedbackproject.Dto.FolderDto;
import com.remindfeedbackproject.Dto.MemberDto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
TODO : 앞으로 로그인 화면에서 구현해야할 목록
TODO : 중복 확인한 이메일이 있으면 발급한 임시 비밀번호 전송(지금은 화면에서 보여줌)
 */
//로그인 화면 클래스
//여기서 외부 저장소 접근 권한을 받아옴
public class LoginActivity extends AppCompatActivity {
    //로딩 창 띄울 progress dialog
    ProgressDialog progressDialog;
    boolean loadingMemberFlag = false;
    boolean loadingFriendFlag = false;
    boolean loadingFeedbackFlag = false;
    boolean loadingMemoFlag = false;
    boolean loadingPhotoFlag = false;
    boolean loadingVoiceFlag = false;
    boolean loadingVideoFlag = false;
    boolean allDataSavedFlag = false;

    //파이어베이스 인스턴스
    FirebaseDatabase saveFirebaseDatabase;

    //로그인 정보를 확인하기 위해 사용하는 arrayList
    ArrayList<MemberDto> firebaseMemberArrList = new ArrayList<MemberDto>();

    static final int REGISTER_REQUEST = 100; //회원가입 확인
    private boolean registerFlag = false; //회원가입 플래그 설정
    private Boolean loginFlag = false; //로그인 플래그 설정

    //사용자 입력이 있을 변수 선언
    private TextView etxtEmail; //로그인 화면에서 사용자가 입력하는 이메일
    private TextView etxtPw; //로그인 화면에서 사용자가 입력하는 비밀번호
    private Button btnFindPw; //로그인 화면에서 사용자가 누르는 '비밀번호 찾기'
    private Button btnRegister; //로그인 화면에서 사용자가 누르는 '회원가입'
    private Button btnLogin; //로그인 화면에서 사용자가 누르는 '로그인'
    private Button btnGoogleLogin; //로그인 화면에서 사용자가 누르는 '구글 로그인'
    private CheckBox cboxAutoLogin; //자동 로그인 체크박스

    // 사용자 접근 권한 지정
    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, //기기, 사진, 미디어, 파일 엑세스 권한
            Manifest.permission.CAMERA, //카메라 파일 액세스 권한
            Manifest.permission.RECORD_AUDIO //오디오 파일 액세스 권한
    };
    private static final int MULTIPLE_PERMISSIONS = 101; // 권한 구분 코드 지정

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // 안드로이드 6.0 이상일 경우 권한 체크
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions(); //권한 체크 함수 호출
        }

        //dialog 초기화
        progressDialog = new ProgressDialog(this);

        //firebaseInstance 초기화
        saveFirebaseDatabase = FirebaseDatabase.getInstance();

        //사용자 입력이 있을 객체를 초기화
        //화면 텍스트 뷰, 버튼 등의 객체 초기화
        etxtEmail = (TextView) findViewById(R.id.login_etxt_email);
        etxtPw = (TextView) findViewById(R.id.login_etxt_pw);

        btnFindPw = (Button) findViewById(R.id.login_btn_findpw);
        btnRegister = (Button) findViewById(R.id.login_btn_register);
        btnLogin = (Button) findViewById(R.id.login_btn_login);
        cboxAutoLogin = (CheckBox) findViewById(R.id.login_cbox_autologin);

        //기타 정보에서 자동 로그인 확인 키를 가지고 옴
        ExtraDao extraDao = new ExtraDao(this);
        ExtraDto extraDto = extraDao.readAllExtraInfo();

        if (extraDto.getAutoLoginConfirmKey().equals("TRUE")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class); //새로운 인텐트 객체 생성
            intent.putExtra("loginEmailValue", extraDto.getAutoLoginEmail()); //이메일 값을 intent로 전달
            startActivity(intent);
            finish();
        }

        //인터넷 연결 상태 확인
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
        if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
            //firebase에서 데이터 가져옴
            readMemberInfoForLogin();
        }
    }

    //Activity를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //로그인 화면이므로 (구글)로그인/비밀번호 찾기/회원가입 등을 할 수 있음
    @Override
    protected void onResume() {
        super.onResume();

        //비밀번호 찾기 버튼을 누르는 경우
        btnFindPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //firebase에서 데이터 가져옴
                readMemberInfoForLogin();

                //비밀번호 찾기 메소드 호출
                launchFindPwDialog();
            }
        });

        //회원가입 버튼을 누르는 경우
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //회원가입 화면 전환 메소드 호출
                launchLoginToRegisterScreen();
            }
        });

        //일반 로그인 버튼을 누르는 경우
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //firebase에서 데이터 가져옴
                readMemberInfoForLogin();

                //로그인 화면 전환 메소드 호출
                launchLoginToMainScreen();
            }
        });
    }

    //비밀번호 찾기 메소드
    private void launchFindPwDialog() {
        //이메일 주소를 보내기 위해 CustomDialog 띄움
        //피드백 작성 다이얼로그 메소드 호출
        //다이얼로그 객체 생성
        TitleDialog findpwDialog = new TitleDialog(this, "findpw", "");
        //다이얼로그 레이아웃 지정
        findpwDialog.setContentView(R.layout.title_dialog);

        //다이얼로그를 보여주기 전 리스너 등록
        findpwDialog.setTitleDialogListener(new TitleDialog.TitleDialogListener() {
            //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
            @Override
            public void onClicked(String state, String inputEmailKey) {
                sendEmail(inputEmailKey); //이메일을 보내는 메소드 호출
            }
        });

        //다이얼로그 크기 조정
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
        int width = displayMetrics.widthPixels;//디바이스 화면 너비
        int height = displayMetrics.heightPixels;//디바이스 화면 높이

        WindowManager.LayoutParams wm = findpwDialog.getWindow().getAttributes();
        wm.width = width;//화면의 가로 넓이
        wm.height = height / 3;//화면의 세로 넓이
        findpwDialog.getWindow().setAttributes(wm);

        //비밀번호 찾기 다이얼로그 호출
        findpwDialog.show();
    }

    //이메일 보내는 메소드
    private void sendEmail(final String inputEmailKey) {
        //인터넷 연결 확인
        //firebase에 저장된 회원 정보와 대조하기 위해 인터넷 연결이 필요
        //인터넷 연결 상태 확인
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
        //이메일과 비밀번호가 일치하는지 확인하고 일치하면 인트로 화면으로 넘어감
        if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {

            boolean emailDuplicationConfirmFlag = emailDuplicationConfirm(inputEmailKey);

            //이메일이 중복되는 경우(즉 DB에 저장된 이메일을 입력한 경우)
            if (emailDuplicationConfirmFlag == true) {
                //임시 비밀번호 발급
                String uuid = ""; //임시 비밀번호를 담을 문자열 변수
                uuid = UUID.randomUUID().toString().replaceAll("-", ""); // -를 제거
                uuid = uuid.substring(0, 6); //uuid를 앞에서부터 6자리 자름
                final String updatePassword = uuid; //내부 메소드에 접근하기 위해 final 변수 생성

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("계정 비밀번호");
                builder.setMessage(inputEmailKey + " 계정의 임시 비밀번호는 [" + uuid + "] 입니다.");

                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                MemberDao memberDao = new MemberDao(LoginActivity.this);
                                MemberDto memberDto = readOneEmailInfoFromFirebase(inputEmailKey);
                                memberDto.setMemberPassword(updatePassword);

                                //shared preference에 변경된 값 저장
                                memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
                                //firebase에 변경된 정보 저장
                                memberDao.updateOneMemberInfoToFirebase(memberDto);
                            }
                        });
                builder.show();
            }
            //이메일이 중복되지 않는 경우
            else{
                Toast.makeText(getApplicationContext(), "이메일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        //인터넷이 연결되어 있지 않은 경우
        //인터넷 연결 설정을 확인해달라는 dialog를 띄움
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("인터넷 연결 확인");
            builder.setMessage("인터넷이 연결되어 있지 않아 임시 비밀번호를 발급받을 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //로그인 화면에서 메인 화면으로 넘어가는 메소드
    ///////////////////////////////////////////////////////////////////////////
    private void launchLoginToMainScreen() {
        //로그인 버튼을 눌렀으므로 loginFlag를 true로 바꿈
        loginFlag = true;

        //자동로그인 체크박스에 체크했으면 그에 맞게 shared preference에서 처리
        ExtraDao extraDao = new ExtraDao(LoginActivity.this);
        ExtraDto extraDto = extraDao.readAllExtraInfo();

        //자동로그인 체크박스에 체크 표시가 된 경우
        if (cboxAutoLogin.isChecked() == true) {
            //자동 로그인 키가 없는 경우
            if (extraDto.getAutoLoginConfirmKey().equals("FALSE")) {
                //firebase에 저장된 회원 정보와 대조하기 위해 인터넷 연결이 필요
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 인트로 화면으로 넘어감
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                    //이메일과 비밀번호 일치 여부를 확인할 boolean 배열 객체 선언 후 값 저장
                    boolean[] loginConfirmFlag = loginDuplicationConfirm();

                    //이메일이 일치하고 비밀번호도 일치하는 경우
                    if (loginConfirmFlag[0] == true && loginConfirmFlag[1] == true) {
                        //키 값, 이메일, 비밀번호 설정 후 shared preference의 수정 메소드 호출
                        extraDto.setAutoLoginConfirmKey("TRUE"); //키 값 설정
                        extraDto.setAutoLoginEmail(etxtEmail.getText().toString()); //이메일 설정
                        extraDto.setAutoLoginPassword(etxtPw.getText().toString()); //비밀번호 설정
                        extraDao.updateExtraInfo(extraDto); //기타 정보 수정 메소드 호출

                        //회원 정보가 로컬에 저장되어 있는지 확인하고 없으면 새로 데이터 받아와서 저장
                        MemberDao memberDao = new MemberDao(LoginActivity.this);
                        if (memberDao.EmailDuplicationConfirm(etxtEmail.getText().toString()) == false) {
                            //firebase에서 로컬로 값 저장
                            readLoginMemberInfoFromFirebase();

                            progressDialog.setMessage("firebase에서 회원 데이터를 내려받는 중입니다...");
                            progressDialog.setCancelable(true);
                            progressDialog.show();

                            //핸들러에서 로컬에 데이터를 다 들고 올 때까지 기다림
                            saveLocalData.sendEmptyMessage(0); //핸들러에 빈 메세지를 보내서 호출
                        }
                        //shared preference 검색 결과 회원 정보가 있는 경우
                        else {
                            //회원 정보에서 인트로 화면 봤는지 확인하고 봤으면 스킵, 안봤으면 인트로 화면으로 넘어감
                            MemberDto memberDto = memberDao.readOneMemberInfo(etxtEmail.getText().toString());

                            //인트로 화면 스킵 변수가 TRUE인 경우
                            if (!memberDto.getMemberIntroSkip().equals("FALSE")) {
                                //메인 화면으로 바로 넘어감
                                //로그인 화면에서 인트로 화면으로 넘어감
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class); //새로운 인텐트 객체 생성
                                intent.putExtra("loginEmailValue", etxtEmail.getText().toString()); //이메일 값을 intent로 전달
                                startActivity(intent);
                                finish();
                            }
                            //인트로 화면 스킵 변수가 FALSE인 경우
                            else {
                                memberDto.setMemberIntroSkip("TRUE"); //TRUE로 설정
                                //shared preference에 변경된 정보 저장
                                memberDao.updateOneMemberInfo(memberDto); //회원 정보 업데이트 메소드 호출
                                //firebase에 변경된 정보 저장
                                memberDao.updateOneMemberInfoToFirebase(memberDto);

                                //로그인 화면에서 인트로 화면으로 넘어감
                                Intent intent = new Intent(LoginActivity.this, IntroActivity.class); //새로운 인텐트 객체 생성
                                intent.putExtra("loginEmailValue", etxtEmail.getText().toString()); //이메일 값을 intent로 전달
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                    //이메일은 일치하지 않고 비밀번호는 일치하거나, 둘 다 일치하지 않는 경우
                    else if ((loginConfirmFlag[0] == false && loginConfirmFlag[1] == true) | (loginConfirmFlag[0] == false && loginConfirmFlag[1] == false)) {
                        //이메일을 찾을 수 없다는 toast 메세지 띄움
                        Toast.makeText(getApplicationContext(), "이메일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    //이메일은 일치하고 비밀번호는 일치하지 않는 경우
                    else if (loginConfirmFlag[0] == true && loginConfirmFlag[1] == false) {
                        //비밀번호가 일치하지 않는다는 toast 메세지 띄움
                        Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어 있지 않아 로그인을 할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        }

        //자동 로그인 체크 박스에 표시가 되지 않은 경우
        else {
            //firebase에 저장된 회원 정보와 대조하기 위해 인터넷 연결이 필요
            //인터넷 연결 상태 확인
            int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
            //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
            //이메일과 비밀번호가 일치하는지 확인하고 일치하면 인트로 화면으로 넘어감
            if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {

                //이메일과 비밀번호 일치 여부를 확인할 boolean 배열 객체 선언 후 값 저장
                boolean[] loginConfirmFlag = loginDuplicationConfirm();

                //이메일이 일치하고 비밀번호도 일치하는 경우
                if (loginConfirmFlag[0] == true && loginConfirmFlag[1] == true) {
                    //회원 정보가 로컬에 저장되어 있는지 확인하고 없으면 새로 데이터 받아와서 저장
                    MemberDao memberDao = new MemberDao(LoginActivity.this);

                    //shared preference 검색 결과 회원 정보에 저장되어있지 않은 경우
                    if (memberDao.EmailDuplicationConfirm(etxtEmail.getText().toString()) == false) {
                        //firebase에서 로컬로 값 저장
                        readLoginMemberInfoFromFirebase();

                        progressDialog.setMessage("firebase에서 회원 데이터를 내려받는 중입니다...");
                        progressDialog.setCancelable(true);
                        progressDialog.show();

                        //핸들러에서 로컬에 데이터를 다 들고 올 때까지 기다림
                        saveLocalData.sendEmptyMessage(0); //핸들러에 빈 메세지를 보내서 호출
                    }
                    //shared preference 검색 결과 정보가 있는 경우
                    else {
                        //회원 정보에서 인트로 화면 봤는지 확인하고 봤으면 스킵, 안봤으면 인트로 화면으로 넘어감
                        MemberDto memberDto = memberDao.readOneMemberInfo(etxtEmail.getText().toString());

                        //인트로 화면 스킵 변수가 TRUE인 경우
                        if (!memberDto.getMemberIntroSkip().equals("FALSE")) {

                            //메인 화면으로 바로 넘어감
                            //로그인 화면에서 인트로 화면으로 넘어감
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class); //새로운 인텐트 객체 생성
                            intent.putExtra("loginEmailValue", etxtEmail.getText().toString()); //이메일 값을 intent로 전달
                            startActivity(intent);
                            finish();
                        }
                        //인트로 화면 스킵 변수가 FALSE인 경우
                        else {
                            memberDto.setMemberIntroSkip("TRUE"); //TRUE로 설정
                            //shared preference에 변경된 정보 저장
                            memberDao.updateOneMemberInfo(memberDto); //회원 정보 업데이트 메소드 호출
                            //firebase에 변경된 정보 저장
                            memberDao.updateOneMemberInfoToFirebase(memberDto);

                            //로그인 화면에서 인트로 화면으로 넘어감
                            Intent intent = new Intent(LoginActivity.this, IntroActivity.class); //새로운 인텐트 객체 생성
                            intent.putExtra("loginEmailValue", etxtEmail.getText().toString()); //이메일 값을 intent로 전달
                            startActivity(intent);
                            finish();
                        }
                    }
                }
                //이메일은 일치하지 않고 비밀번호는 일치하거나, 둘 다 일치하지 않는 경우
                else if ((loginConfirmFlag[0] == false && loginConfirmFlag[1] == true) | (loginConfirmFlag[0] == false && loginConfirmFlag[1] == false)) {
                    //이메일을 찾을 수 없다는 toast 메세지 띄움
                    Toast.makeText(getApplicationContext(), "이메일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                //이메일은 일치하고 비밀번호는 일치하지 않는 경우
                else if (loginConfirmFlag[0] == true && loginConfirmFlag[1] == false) {
                    //비밀번호가 일치하지 않는다는 toast 메세지 띄움
                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            //인터넷이 연결되어 있지 않은 경우
            //인터넷 연결 설정을 확인해달라는 dialog를 띄움
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("인터넷 연결 확인");
                builder.setMessage("인터넷이 연결되어 있지 않아 로그인을 할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //로그인 화면에서 회원가입 화면으로 넘어가는 메소드
    ///////////////////////////////////////////////////////////////////////////
    private void launchLoginToRegisterScreen() {
        //인터넷 연결 상태 확인
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
        //회원가입 화면으로 넘어감
        if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
            //회원가입 버튼을 눌렀으므로 registerFlag를 true로 바꿈
            registerFlag = true;

            //LoginActivity 화면에서 RegisterActivity 화면으로 넘어가기 위해 Intent 객체 생성
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);

            //피드백 작성 화면으로 전환
            startActivityForResult(intent, REGISTER_REQUEST);
        }
        //인터넷이 연결되어 있지 않은 경우
        //인터넷 연결 설정을 확인해달라는 dialog를 띄움
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("인터넷 연결 확인");
            builder.setMessage("인터넷이 연결되어 있지 않아 회원가입 화면으로 넘어갈 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //만일 회원가입 버튼을 누른 경우
        if (registerFlag == true | loginFlag == true) {
            //로그인 화면에서 이메일 및 비밀번호에 적은 정보는 삭제되도록 함
            //login layout에서 이메일과 비밀번호 입력 뷰 정보를 가져옴

            //입력 정보를 null로 바꿈
            etxtPw.setText(null);
            etxtEmail.setText(null);

            //작업을 마쳤으므로 다시 flag를 false로 바꿈
            registerFlag = false;

            //TODO : 리스너 해제해야 함
        }
    }

    //데이터베이스에서 로컬로 데이터 가져오는 핸들러
    Handler saveLocalData = new Handler() {
        public void handleMessage(Message msg) {
            //데이터를 가져오는 동안 플래그 실행 안 되도록 함
            if (loadingMemberFlag == true && loadingFriendFlag == true && loadingFeedbackFlag == true &&
                    loadingMemoFlag == true && loadingPhotoFlag == true && loadingVoiceFlag == true && loadingVideoFlag == true) {
                //progress dialog 종료
                progressDialog.dismiss();
                allDataSavedFlag = true;//불린 값 true로 만듦

                //회원 정보에서 인트로 화면 봤는지 확인하고 봤으면 스킵, 안봤으면 인트로 화면으로 넘어감
                MemberDao memberDao = new MemberDao(LoginActivity.this);
                MemberDto memberDto = memberDao.readOneMemberInfo(etxtEmail.getText().toString());

                //데이터 저장 후 확인
                System.out.println("데이터 저장 후 가져온 변수의 feedbackInfo는 " + memberDto.getMemberFeedbackInfo());

                //인트로 화면 스킵 변수가 TRUE인 경우
                if (!memberDto.getMemberIntroSkip().equals("FALSE")) {

                    //메인 화면으로 바로 넘어감
                    //로그인 화면에서 인트로 화면으로 넘어감
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class); //새로운 인텐트 객체 생성
                    intent.putExtra("loginEmailValue", etxtEmail.getText().toString()); //이메일 값을 intent로 전달
                    startActivity(intent);
                    finish();
                }
                //인트로 화면 스킵 변수가 FALSE인 경우
                else {
                    memberDto.setMemberIntroSkip("TRUE"); //TRUE로 설정
                    //shared preference에 변경된 정보 저장
                    memberDao.updateOneMemberInfo(memberDto); //회원 정보 업데이트 메소드 호출
                    //firebase에 변경된 정보 저장
                    memberDao.updateOneMemberInfoToFirebase(memberDto);

                    //로그인 화면에서 인트로 화면으로 넘어감
                    Intent intent = new Intent(LoginActivity.this, IntroActivity.class); //새로운 인텐트 객체 생성
                    intent.putExtra("loginEmailValue", etxtEmail.getText().toString()); //이메일 값을 intent로 전달
                    startActivity(intent);
                    finish();
                }

                saveLocalData.removeMessages(0); //제거 메세지 전송
            }
            //데이터를 가져오는 경우
            else {
                //비어있는 메세지를 핸들러에 전송
                saveLocalData.sendEmptyMessage(0);
            }
        }
    };

    //요청한 데이터를 피드백 작성 화면에서 받아온 후 리스트에 저장하는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REGISTER_REQUEST) {
            if (resultCode == RESULT_OK) {
                //firebase에서 데이터 가져옴
                readMemberInfoForLogin();
                //회원이 작성한 이메일을 이메일 작성 란에 미리 입력
                etxtEmail.setText(intent.getStringExtra("regEmailData"));
                //Intent 내용 삭제
                getIntent().removeExtra("regEmailData");
            } else if (resultCode == RESULT_CANCELED) {
                //회원가입을 취소했기 때문에 아무 작업도 하지 않음
                //firebase에서 데이터 가져옴
                readMemberInfoForLogin();
            }
        }
    }

    //외부 저장소 접근 권한을 체크하는 메소드
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    //사용자의 선택에 따라 결과값을 반환하는 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }
                    //최초 실행일 경우 폴더 생성
                    FolderDto folderDto = new FolderDto();
                    folderDto.createRootFolder(); //루트 폴더 생성
                } else {
                    showToast_PermissionDeny();
                }
                return;
            }
        }
    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish(); //액티비티 종료
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 회원 정보 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readLoginMemberInfoFromFirebase() {
        saveFirebaseDatabase.getReference().child("memberInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("순서검사 : memberInfo");

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final MemberDto memberDto = postSnapshot.getValue(MemberDto.class);
                    if (etxtEmail.getText().toString().equals(memberDto.getMemberEmail())) {
                        //회원가입 회원 말고 다른 기계에서 로그인 한 회원의 경우 회원 정보를 가져와서 shared preference에 저장
                        //회원 정보가 저장될 폴더 객체 및 경로 선언
                        MemberDao memberDao = new MemberDao(LoginActivity.this); //회원 정보 업데이트 할 객체

                        //만일 회원 정보가 shared preference에 저장되지 않은 경우
                        if (memberDao.EmailDuplicationConfirm(etxtEmail.getText().toString()) == false) {
                            Gson gson = new Gson();

                            //새로 회원 폴더 생성
                            FolderDto folderDto = new FolderDto(); //folder dto 객체 생성
                            folderDto.createAccountFolder(etxtEmail.getText().toString()); //회원 정보 폴더 생성
                            String folderDtoValue = gson.toJson(folderDto);
                            memberDto.setMemberDataPathInfo(folderDtoValue); //경로 정보 업데이트
                            memberDto.setMemberAdviserInfo("NONE");
                            memberDto.setMemberFriendInfo("NONE");
                            memberDto.setMemberFeedbackInfo("NONE");

                            //shared preference에 회원 정보 업데이트
                            memberDao.insertOneMemberInfo(memberDto);
                            //혹시 저장경로가 바뀌는 경우 firebase에 회원 정보 업데이트
                            memberDao.updateOneMemberInfoToFirebase(memberDto);

                            //회원 프로필 사진이 있을 경우 가져와 저장
                            //파일이 존재하는 경우(회원의 프로필 사진 값이 "NONE"이 아닌 경우
                            if (!memberDto.getMemberProfilePhotoPath().equals("NONE")) {
                                //저장소에서 회원 프로필 사진 가져와 저장
                                //회원 친구 고유 키 생성
                                String tempMemberKey = etxtEmail.getText().toString().replace("@", "_");
                                String accountMemberKey = "MemberKey_" + tempMemberKey.replace(".", "_");
                                String profileTitle = tempMemberKey.replace(".", "_") + ".png";

                                //프로필 사진 저장 경로 지정
                                final String memberProfilePath = folderDto.getProfileDirectoryPath();//파일 경로 지정
                                final File memberProfileFile = new File(memberProfilePath, profileTitle); //파일 객체 생성
                                try {
                                    //새로 사진 파일 생성
                                    memberProfileFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //생성된 FirebaseStorage를 참조하는 storage 생성
                                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance("gs://remindfeedback.appspot.com");
                                StorageReference storageReference = firebaseStorage.getReference();

                                //Storage 내부의 profile 폴더 안의 profile.png 파일명을 가리키는 참조 생성
                                StorageReference pathReference = storageReference.child("profileInfo/" + accountMemberKey + "/" + profileTitle);

                                //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                                FileDownloadTask fileDownloadTask = pathReference.getFile(memberProfileFile);
                                fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        //다운로드 성공 후 할 일
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //다운로드 실패 후 할 일
                                    }
                                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    //진행상태 표시
                                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    }
                                });
                            }

                            //firebase에서 회원의 친구 정보, 피드백 정보 가져오는 메소드 호출
                            readLoginFriendInfoToFirebase();
                            readLoginFeedbackInfoToFirebase();
                        }
                        break; //반복문 탈출
                    }
                }
                loadingMemberFlag = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase", "[Login]loadPost:onCanceled", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 로그인 한 회원의 친구 정보를 로컬에 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readLoginFriendInfoToFirebase() {
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = firebaseDatabase.getReference("friendInfo");

        //회원 고유 키 생성
        final String tempUserKey = etxtEmail.getText().toString().replace("@", "_");
        String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        // firebase database에서 정보를 읽어옴
        saveFirebaseDatabase.getReference().child("friendInfo").child(accountMemberKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("순서검사 : friendInfo");

                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    //값이 있는지 확인하고 있을 경우에만 shared preference에 저장
                    if (fileSnapshot.exists() == true) {
                        FriendDto friendDto = fileSnapshot.getValue(FriendDto.class);
                        FriendDao friendDao = new FriendDao(LoginActivity.this);
                        friendDao.insertOneFriendInfo(etxtEmail.getText().toString(), friendDto);
                    }
                }
                loadingFriendFlag = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("[LoginActivity] TAG: ", "Failed to read friend value", databaseError.toException());

            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 로그인 한 회원의 피드백 정보를 로컬에 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readLoginFeedbackInfoToFirebase() {
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = firebaseDatabase.getReference("feedbackInfo");

        //회원 고유 키 생성
        final String tempUserKey = etxtEmail.getText().toString().replace("@", "_");
        final String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        // firebase database에서 정보를 읽어옴
        saveFirebaseDatabase.getReference().child("feedbackInfo").child(accountMemberKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("순서검사 : feedbackInfo");

                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    //값이 있는지 확인하고 있을 경우에만 shared preference에 저장
                    if (fileSnapshot.exists() == true) {
                        final FeedbackDto feedbackDto = fileSnapshot.getValue(FeedbackDto.class);
                        feedbackDto.setMemoInfo("NONE");
                        feedbackDto.setPhotoInfo("NONE");
                        feedbackDto.setVoiceInfo("NONE");
                        feedbackDto.setVideoInfo("NONE");
                        FeedbackDao feedbackDao = new FeedbackDao(LoginActivity.this);
                        feedbackDao.insertFeedbackInfo(etxtEmail.getText().toString(), feedbackDto);

                        readLoginMemoInfoToFirebase(feedbackDto.getFeedbackKey());
                        readLoginPhotoInfoToFirebase(feedbackDto.getFeedbackKey());
                        readLoginVoiceInfoToFirebase(feedbackDto.getFeedbackKey());
                        readLoginVideoInfoToFirebase(feedbackDto.getFeedbackKey());
                    }
                }
                loadingFeedbackFlag = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("[LoginActivity] TAG: ", "Failed to read friend value", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 로그인 한 회원의 메모 정보를 로컬에 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readLoginMemoInfoToFirebase(final String feedbackKey) {
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = firebaseDatabase.getReference("memoInfo");

        //회원 고유 키 생성
        final String tempUserKey = etxtEmail.getText().toString().replace("@", "_");
        String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        // firebase database에서 정보를 읽어옴
        saveFirebaseDatabase.getReference().child("memoInfo").child(accountMemberKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("순서검사 : memoInfo");
                System.out.println("feedbackKey : " + feedbackKey);

                for (DataSnapshot fileSnapshot : dataSnapshot.child(feedbackKey).getChildren()) {
                    //값이 있는지 확인하고 있을 경우에만 shared preference에 저장
                    if (fileSnapshot.exists() == true) {
                        MemoDto memoDto = fileSnapshot.getValue(MemoDto.class);
                        MemoDao memoDao = new MemoDao(LoginActivity.this);
                        memoDao.insertOneMemoInfo(etxtEmail.getText().toString(), feedbackKey, memoDto);
                    }
                }
                loadingMemoFlag = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("[LoginActivity] TAG: ", "Failed to read memo value", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 로그인 한 회원의 사진 정보를 로컬에 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readLoginPhotoInfoToFirebase(final String feedbackKey) {
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = firebaseDatabase.getReference("photoInfo");

        //회원 고유 키 생성
        final String tempUserKey = etxtEmail.getText().toString().replace("@", "_");
        final String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        // firebase database에서 정보를 읽어옴
        saveFirebaseDatabase.getReference().child("photoInfo").child(accountMemberKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("순서검사 : photoInfo");

                for (DataSnapshot fileSnapshot : dataSnapshot.child(feedbackKey).getChildren()) {
                    //값이 있는지 확인하고 있을 경우에만 shared preference에 저장
                    if (fileSnapshot.exists() == true) {
                        PhotoDto photoDto = fileSnapshot.getValue(PhotoDto.class);
                        PhotoDao photoDao = new PhotoDao(LoginActivity.this);
                        photoDao.insertPhotoInfo(etxtEmail.getText().toString(), feedbackKey, photoDto);

                        //저장소에서 사진 가져와 저장
                        //일단 사진 path를 uri로 변경
                        Uri uri = Uri.fromFile(new File(photoDto.getPhotoPath()));

                        //사진 저장 경로 가져옴
                        MemberDao memberDao = new MemberDao(LoginActivity.this);
                        FolderDto folderDto = memberDao.readMemberDataPathInfo(etxtEmail.getText().toString());

                        //경로 지정
                        String photoPath = folderDto.getPhotoDirectoryPath();//파일 경로 지정
                        File photoFile = new File(photoPath, uri.getLastPathSegment()); //파일 객체 생성
                        try {
                            //새로 사진 파일 생성
                            photoFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //생성된 FirebaseStorage를 참조하는 storage 생성
                        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance("gs://remindfeedback.appspot.com");
                        StorageReference storageReference = firebaseStorage.getReference();

                        //Storage 내부의 images 폴더 안의 image.jpg 파일명을 가리키는 참조 생성
                        StorageReference pathReference = storageReference.child("photoInfo/" + accountMemberKey + "/" +
                                feedbackKey + "/" + photoDto.getPhotoKey() + "/" + uri.getLastPathSegment());

                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        FileDownloadTask fileDownloadTask = pathReference.getFile(photoFile);
                        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                //다운로드 성공 후 할 일
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            //진행상태 표시
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            }
                        });
                    }
                }
                loadingPhotoFlag = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("[LoginActivity] TAG: ", "Failed to read photo value", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 로그인 한 회원의 녹음 정보를 로컬에 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readLoginVoiceInfoToFirebase(final String feedbackKey) {
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = firebaseDatabase.getReference("voiceInfo");

        //회원 고유 키 생성
        final String tempUserKey = etxtEmail.getText().toString().replace("@", "_");
        final String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        // firebase database에서 정보를 읽어옴
        saveFirebaseDatabase.getReference().child("voiceInfo").child(accountMemberKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("순서검사 : voiceInfo");

                for (DataSnapshot fileSnapshot : dataSnapshot.child(feedbackKey).getChildren()) {
                    //값이 있는지 확인하고 있을 경우에만 shared preference에 저장
                    if (fileSnapshot.exists() == true) {
                        VoiceDto voiceDto = fileSnapshot.getValue(VoiceDto.class);
                        VoiceDao voiceDao = new VoiceDao(LoginActivity.this);
                        voiceDao.insertVoiceInfo(etxtEmail.getText().toString(), feedbackKey, voiceDto);

                        //저장소에서 사진 가져와 저장
                        //일단 사진 path를 uri로 변경
                        Uri uri = Uri.fromFile(new File(voiceDto.getVoicePath()));

                        //사진 저장 경로 가져옴
                        MemberDao memberDao = new MemberDao(LoginActivity.this);
                        FolderDto folderDto = memberDao.readMemberDataPathInfo(etxtEmail.getText().toString());

                        //경로 지정
                        String voicePath = folderDto.getVoiceDirectoryPath();//파일 경로 지정
                        File voiceFile = new File(voicePath, uri.getLastPathSegment()); //파일 객체 생성
                        try {
                            //새로 사진 파일 생성
                            voiceFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //생성된 FirebaseStorage를 참조하는 storage 생성
                        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance("gs://remindfeedback.appspot.com");
                        StorageReference storageReference = firebaseStorage.getReference();

                        //Storage 내부의 images 폴더 안의 image.jpg 파일명을 가리키는 참조 생성
                        StorageReference pathReference = storageReference.child("voiceInfo/" + accountMemberKey + "/" +
                                feedbackKey + "/" + voiceDto.getVoiceKey() + "/" + uri.getLastPathSegment());


                        Log.e("[LoginActivity]", "pathReference : " + pathReference.getPath());

                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        FileDownloadTask fileDownloadTask = pathReference.getFile(voiceFile);
                        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                //다운로드 성공 후 할 일
                                //여기에 걸리냐 아니면
                                Log.e("[LoginActivity]", "다운로드 성공");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                                //여기에 걸리냐
                                Log.e("[LoginActivity]", "다운로드 실패");
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            //진행상태 표시
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            }
                        });
                    }
                }
                loadingVoiceFlag = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("[LoginActivity] TAG: ", "Failed to voice friend value", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 로그인 한 회원의 영상 정보를 로컬에 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readLoginVideoInfoToFirebase(final String feedbackKey) {
//        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = firebaseDatabase.getReference("videoInfo");

        //회원 고유 키 생성
        final String tempUserKey = etxtEmail.getText().toString().replace("@", "_");
        final String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        // firebase database에서 정보를 읽어옴
        saveFirebaseDatabase.getReference().child("videoInfo").child(accountMemberKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("순서검사 : videoInfo");

                for (DataSnapshot fileSnapshot : dataSnapshot.child(feedbackKey).getChildren()) {
                    //값이 있는지 확인하고 있을 경우에만 shared preference에 저장
                    if (fileSnapshot.exists() == true) {
                        VideoDto videoDto = fileSnapshot.getValue(VideoDto.class);
                        VideoDao videoDao = new VideoDao(LoginActivity.this);
                        videoDao.insertVideoInfo(etxtEmail.getText().toString(), feedbackKey, videoDto);

                        //storage에서 영상 파일 가져와 저장
                        //일단 영상 path를 uri로 변경
                        Uri videoUri = Uri.fromFile(new File(videoDto.getVideoPath()));

                        //사진 저장 경로 가져옴
                        MemberDao memberDao = new MemberDao(LoginActivity.this);
                        FolderDto folderDto = memberDao.readMemberDataPathInfo(etxtEmail.getText().toString());

                        //경로 지정
                        String videoPath = folderDto.getVideoDirectoryPath();//파일 경로 지정
                        File videoFile = new File(videoPath, videoUri.getLastPathSegment()); //파일 객체 생성
                        try {
                            //새로 영상 파일 생성
                            videoFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //생성된 FirebaseStorage를 참조하는 storage 생성
                        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance("gs://remindfeedback.appspot.com");
                        StorageReference storageReference = firebaseStorage.getReference();

                        //Storage 내부의 images 폴더 안의 image.jpg 파일명을 가리키는 참조 생성
                        StorageReference pathReferenceOne = storageReference.child("videoInfo/" + accountMemberKey + "/" +
                                feedbackKey + "/" + videoDto.getVideoKey() + "/" + videoUri.getLastPathSegment());

                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        FileDownloadTask fileDownloadTaskOne = pathReferenceOne.getFile(videoFile);
                        fileDownloadTaskOne.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                //다운로드 성공 후 할 일
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            //진행상태 표시
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            }
                        });

                        //일단 사진 path를 uri로 변경
                        Uri thumbnailUri = Uri.fromFile(new File(videoDto.getVideoThumbnailPath()));

                        //경로 지정
                        String thumbnailPath = folderDto.getVideoDirectoryPath();//파일 경로 지정
                        File thumbnailFile = new File(thumbnailPath, thumbnailUri.getLastPathSegment()); //파일 객체 생성
                        try {
                            //새로 영상 썸네일 파일 생성
                            thumbnailFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //Storage 내부의 videoInfo 폴더 안의 비디오 썸네일 파일명을 가리키는 참조 생성
                        StorageReference pathReferenceTwo = storageReference.child("videoInfo/" + accountMemberKey + "/" +
                                feedbackKey + "/" + videoDto.getVideoKey() + "/" + thumbnailUri.getLastPathSegment());

                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        FileDownloadTask fileDownloadTaskTwo = pathReferenceTwo.getFile(thumbnailFile);
                        fileDownloadTaskTwo.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                //다운로드 성공 후 할 일
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            //진행상태 표시
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            }
                        });
                    }
                }
                //회원 정보 다 가져왔으면 flag 값을 true로 만듦
                loadingVideoFlag = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("[LoginActivity] TAG: ", "Failed to read video value", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 회원 정보 검색하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readMemberInfoForLogin() {
        FirebaseDatabase.getInstance().getReference("memberInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //이메일 중복 확인 arraylist 초기화
                firebaseMemberArrList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    MemberDto memberDto = postSnapshot.getValue(MemberDto.class);
                    firebaseMemberArrList.add(memberDto); //arraylist에 memberDto 값 추가
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase", "[Login]loadPost:onCanceled", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 일반 로그인 확인하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public boolean[] loginDuplicationConfirm() {
        boolean[] loginConfirmFlag = new boolean[2];
        boolean emailConfirmFlag = false;
        boolean passwordConfirmFlag = false;

        for(int i=0; i<firebaseMemberArrList.size(); i++){
            //이메일이 중복되는 경우
            if(firebaseMemberArrList.get(i).getMemberEmail().equals(etxtEmail.getText().toString())){
                emailConfirmFlag = true;

                //비밀번호가 중복되는 경우
                if(firebaseMemberArrList.get(i).getMemberPassword().equals(etxtPw.getText().toString())){
                    passwordConfirmFlag = true;
                    break;
                }
                //비밀번호가 중복되지 않는 경우
                else{
                    passwordConfirmFlag = false;
                    break;
                }
            }
            //이메일이 중복되지 않는 경우
            else{
                continue;
            }
        }

        loginConfirmFlag[0] = emailConfirmFlag;
        loginConfirmFlag[1] = passwordConfirmFlag;

        //중복이 아닌 경우 false, 중복인 경우 true
        return loginConfirmFlag;
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 이메일 중복 로그인 확인하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public boolean emailDuplicationConfirm(String inputEmail) {
        boolean emailConfirmFlag = false;

        for(int i=0; i<firebaseMemberArrList.size(); i++){
            //이메일이 중복되는 경우 flag를 true로 바꿈
            if(firebaseMemberArrList.get(i).getMemberEmail().equals(inputEmail)){
                emailConfirmFlag = true;
                break;
            }
        }

        //중복이 아닌 경우 false, 중복인 경우 true
        return emailConfirmFlag;
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 중복되는 이메일의 회원 정보를 읽어오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public MemberDto readOneEmailInfoFromFirebase(String inputEmail) {
        MemberDto memberDto = new MemberDto();

        for(int i=0; i<firebaseMemberArrList.size(); i++){
            //이메일이 중복되는 경우 flag를 true로 바꿈
            if(firebaseMemberArrList.get(i).getMemberEmail().equals(inputEmail)){
                memberDto = firebaseMemberArrList.get(i);
                break;
            }
        }

        //중복이 아닌 경우 false, 중복인 경우 true
        return memberDto;
    }
}