package com.remindfeedbackproject.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dto.FolderDto;
import com.remindfeedbackproject.Dto.MemberDto;
import com.remindfeedbackproject.Etc.NetworkStatus;
import com.remindfeedbackproject.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
TODO : 닉네임 정규식 적용
 */
public class RegisterActivity extends AppCompatActivity {
    //이메일 중복을 확인하기 위해 사용하는 arrayList
    ArrayList<String> emailKeyArrList =  new ArrayList<String>();

    private final long FINISH_INTERVAL_TIME = 2000; //2초의 시간 간격을 둠
    private long backPressedTime = 0; //뒤로가기 버튼을 두 번 누르면 종료

    //이메일 및 비밀번호 정규식
    // 이메일 정규식
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    //비밀번호 정규식
    public static final Pattern VALID_PASSWOLD_REGEX_ALPHA_NUM =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).{4,10}$", Pattern.CASE_INSENSITIVE); // 4~10자리

    //사용자가 입력 란의 길이를 저장할 int[]
    int[] editTextLength;

    //사용자 입력이 있을 변수 선언
    TextView etxtEmail;//회원 이메일을 저장하는 변수
    TextView etxtNickName;//회원 이름을 저장하는 변수
    TextView etxtPw;//회원 비밀번호를 저장하는 변수
    TextView etxtPwConfirm;//회원 비밀번호 확인을 저장하는 변수
    Button btnEmailConfirm; //회원 이메일 중복 확인하는 변수
    Button btnRegister;//회원가입 버튼을 저장하는 변수
    CheckBox cboxProvision; //회원 약관 확인하는 체크 박스

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        //사용자 입력 란의 길이를 저장할 int[] 초기화
        editTextLength = new int[4];

        //사용자 입력이 있을 객체를 초기화
        //화면 텍스트 뷰, 버튼 등의 객체 초기화
        etxtEmail = (TextView) findViewById(R.id.register_etxt_email);
        etxtNickName = (TextView) findViewById(R.id.register_etxt_nickname);
        etxtPw = (TextView) findViewById(R.id.register_etxt_pw);
        etxtPwConfirm = (TextView) findViewById(R.id.register_etxt_pwconfirm);
        btnEmailConfirm = (Button) findViewById(R.id.register_btn_emailconfirm);
        btnRegister = (Button) findViewById(R.id.register_btn_register);
        cboxProvision = (CheckBox) findViewById(R.id.register_cbox_provision);

        //이메일 중복 확인 버튼은 제대로 된 이메일 형식을 입력할 때까지 비활성화
        btnEmailConfirm.setEnabled(false);
        //로그인 버튼은 이메일 중복 확인해서 중복이 아닐 때까지 비활성화
        btnRegister.setEnabled(false);
        btnRegister.setBackgroundColor(Color.rgb(234, 234, 234));
        //비밀번호 확인 란은 비밀번호를 정규식에 맞게 입력할 때까지 비활성화
        etxtPwConfirm.setEnabled(false);
    }

    //Activity를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //회원가입 화면이므로 회원가입을 하거나 안할 수 있음
    @Override
    protected void onResume() {
        super.onResume();

        //이메일 중복 확인 버튼을 누르는 경우
        btnEmailConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일 중복검사 함
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                    //firebase에서 데이터 가져옴
                    readMemberInfoToFirebase();
                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어 있지 않아 이메일 중복 검사를 할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        });

        //회원가입 버튼을 누르는 경우
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextLength[0] = etxtEmail.getText().toString().length();
                editTextLength[1] = etxtNickName.getText().toString().length();
                editTextLength[2] = etxtPw.getText().toString().length();
                editTextLength[3] = etxtPwConfirm.getText().toString().length();

                //회원 정보 입력 란을 한 칸이라도 비워둔 경우
                if (editTextLength[0] == 0 | editTextLength[1] == 0 | editTextLength[2] == 0 | editTextLength[3] == 0) {
                    Toast.makeText(RegisterActivity.this, "아직 입력하지 않은 공백 란이 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //회원 정보를 모두 다 입력한 경우
                //이메일이 정규식과 일치하는지 확인
                if (validateEmail(etxtEmail.getText().toString()) == false) {
                    //적색인 경우 정규식과 일치하지 않음
                    Toast.makeText(RegisterActivity.this, "이메일 형식에 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (validatePassword(etxtPw.getText().toString()) == false) {
                    //적색인 경우 정규식과 일치하지 않음
                    Toast.makeText(RegisterActivity.this, "비밀번호 형식에 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //비밀번호가 정규식과 일치하는지 확인
                //비밀번호가 맞는지 틀린지 확인함
                if (!etxtPw.getText().toString().equals(etxtPwConfirm.getText().toString())) {
                    //비밀번호가 다르다는 Toast 메세지 출력
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    etxtPwConfirm.requestFocus(); //비밀번호 확인에 포커스를 맞춤
                    return;
                }

                //회원 약관 체크박스가 체크되지 않은 경우
                if(!cboxProvision.isChecked()){
                    //약관 동의 안내 Toast 메세지 출력
                    Toast.makeText(RegisterActivity.this, "약관에 동의하지 않으면 회원 가입을 완료할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //모든 정보를 입력하고 비밀번호도 일치하는 경우
                //로그인 화면 전환 메소드 호출
                launchRegisterToLoginScreen();
            }
        });

        //이메일이 입력될 때마다 유효성 확인을 하는 리스너
        etxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean validateValue = validateEmail(etxtEmail.getText().toString());
                //유효성 검사를 했을 때 false가 나온 경우
                if (validateValue == false) {
                    //이메일 텍스트 색깔을 적색으로 변경
                    etxtEmail.setTextColor(Color.RED);
                    btnEmailConfirm.setEnabled(false); //이메일 중복확인 버튼 비활성화
                    btnRegister.setEnabled(false); //로그인 버튼 비활성화
                    btnRegister.setBackgroundColor(Color.rgb(234, 234, 234));
                }
                //유효성 검사를 했을 때 true가 나온 경우
                else {
                    //이메일 텍스트 색깔을 검정색으로 변경
                    etxtEmail.setTextColor(Color.BLACK);
                    btnEmailConfirm.setEnabled(true); //중복확인 버튼 활성화
                }
            }
        });

        //비밀번호가 입력될 때마다 유효성 확인을 하는 리스너
        etxtPw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean validateValue = validatePassword(etxtPw.getText().toString());
                //유효성 검사를 했을 때 false가 나온 경우
                if (validateValue == false) {
                    //비밀번호 색깔을 적색으로 변경
                    etxtPw.setTextColor(Color.RED);
                } else {
                    etxtPw.setTextColor(Color.BLACK);
                    etxtPwConfirm.setEnabled(true);
                }
            }
        });

        //비밀번호 확인이 입력될 때마다 텍스트 색상을 변화시키는 리스너
        etxtPwConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            //텍스트가 변경될 때 텍스트 색깔 변경
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pw = etxtPw.getText().toString();
                String pwConfirm = etxtPwConfirm.getText().toString();

                //비밀번호와 비밀번호 확인이 일치하면 파란 색으로 변경
                if (pw.equals(pwConfirm)) {
                    etxtPw.setTextColor(Color.BLUE);
                    etxtPwConfirm.setTextColor(Color.BLUE);
                }
                //일치하지 않는 경우 적색으로 변경
                else {
                    etxtPw.setTextColor(Color.RED);
                    etxtPwConfirm.setTextColor(Color.RED);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 이메일 정규식 검사 메소드
    public static boolean validateEmail(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

    // 비밀번호 정규식 검사 메소드
    public static boolean validatePassword(String password) {
        Matcher matcher = VALID_PASSWOLD_REGEX_ALPHA_NUM.matcher(password);
        return matcher.find();
    }

    //로그인 화면 전환 메소드
    private void launchRegisterToLoginScreen() {
        //인터넷 연결 상태 확인
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
        //회원가입 후 로그인 화면으로 넘어감
        if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
            //DTO, DAO 객체 생성
            MemberDto memberDto = new MemberDto();
            MemberDao memberDao = new MemberDao(this);
            Gson gson = new Gson();

            //회원 정보가 저장될 폴더 객체 및 경로 선언
            FolderDto folderDto = new FolderDto(); //folder dto 객체 생성
            folderDto.createAccountFolder(etxtEmail.getText().toString()); //회원 정보 폴더 생성
            String folderDtoValue = gson.toJson(folderDto);

            //DTO 객체에 값 집어넣음
            //이메일, 이름, 비밀번호, 날짜, 프로필 사진 경로, 상태 메세지, 인트로 스킵, 폴더 경로 정보, 친구 정보, 피드백 정보 저장
            memberDto.setMemberItem(etxtEmail.getText().toString(), etxtNickName.getText().toString(),
                    etxtPw.getText().toString(), setRegisterDate(), "NONE",
                    "상태 메세지를 입력해주세요.", "FALSE", "NONE",
                    folderDtoValue, "NONE", "NONE");

            //shared preference에 DTO 객체를 넘겨 회원 정보 저장
            memberDao.insertOneMemberInfo(memberDto);

            //firebase에 DTO 객체를 넘겨 회원 정보 저장
            memberDao.insertOneMemberInfoToFirebase(memberDto);

            //회원가입 버튼을 누르고 로그인 화면으로 넘어가는 경우 그에 걸맞은 작업이 필요함
            Intent intent = new Intent();
            //이메일 값을 intent로 전달
            intent.putExtra("regEmailData", etxtEmail.getText().toString());
            //제대로 작성하였으므로 OK를 보냄
            setResult(RESULT_OK, intent);

            finish();
        }
        //인터넷이 연결되어 있지 않은 경우
        //인터넷 연결 설정을 확인해달라는 dialog를 띄움
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("인터넷 연결 확인");
            builder.setMessage("인터넷이 연결되어 있지 않아 회원가입을 완료할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
    }

    //뒤로가기 버튼을 눌렀을 시 호출되는 메소드
    @Override
    public void onBackPressed() {
        //현재 시간을 long 타입 변수에 저장
        long currentTime = System.currentTimeMillis();
        //시간 간격을 long 타입 변수에 저장
        //시간 간격 = 현재 시간 - 뒤로가기 버튼을 눌렀을 때의 시간
        long intervalTime = currentTime - backPressedTime;

        //시간 간격이 0보다 크고 2000과 같거나 작을 때
        //즉 2초 사이일 때 한번 더 뒤로가기 버튼을 누른 경우
        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            //작성을 취소하였으므로 아무것도 보내지 않음
            setResult(RESULT_CANCELED);
            //현재 화면을 onDestroy 상태로 만듦
            super.onBackPressed();
        }
        //현재시간 - 뒤로가기 버튼을 누른 시간이 2초보다 큰 경우
        //즉 처음 뒤로가기 버튼을 눌렀거나, 뒤로가기 버튼을 누르고 2초 이상의 시간이 경과한 경우
        else {
            //뒤로가기 버튼을 눌렀을 때의 현재 시간을 저장
            backPressedTime = currentTime;
            Toast.makeText(this, "회원가입을 취소하시겠습니까?", Toast.LENGTH_SHORT).show();
        }
    }

    //현재 날짜를 저장하는 메소드
    private String setRegisterDate(){
        long nowTime = System.currentTimeMillis(); //현재 시간을 시스템에서 가져옴
        Date date = new Date(nowTime); //Date 생성하기
        SimpleDateFormat timeSDF = new SimpleDateFormat("yyyy-MM-dd");//yyyy-MM-dd 형식으로 가져옴
        return timeSDF.format(date);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 데이터 검색하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readMemberInfoToFirebase() {
        FirebaseDatabase.getInstance().getReference("memberInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //이메일 중복 확인 arraylist 초기화
                emailKeyArrList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    MemberDto memberDto = postSnapshot.getValue(MemberDto.class);
                    emailKeyArrList.add(memberDto.getMemberEmail()); //arraylist에 email값 추가
                }

                //false를 반환하면 중복이 아닌 거고, true를 반환하면 중복
                //이메일이 중복 아닌 경우
                if (!emailKeyArrList.contains(etxtEmail.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "사용할 수 있는 이메일입니다.", Toast.LENGTH_SHORT).show();
                    btnRegister.setEnabled(true); //로그인 버튼 활성화
                    btnRegister.setBackgroundColor(Color.rgb(41, 98, 255));
                }
                //이메일이 중복인 경우
                else {
                    Toast.makeText(getApplicationContext(), "사용할 수 없는 이메일입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase", "[Register]loadPostionCanceled", databaseError.toException());
            }
        });
    }
}
