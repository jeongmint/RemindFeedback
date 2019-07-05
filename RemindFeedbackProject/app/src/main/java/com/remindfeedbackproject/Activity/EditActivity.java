package com.remindfeedbackproject.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.remindfeedbackproject.Dao.FriendDao;
import com.remindfeedbackproject.Etc.NetworkStatus;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dto.FriendDto;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dto.MemberDto;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditActivity extends AppCompatActivity {
    //로그인 정보를 확인하기 위해 사용하는 arrayList
    ArrayList<String> emailKeyArrList = new ArrayList<String>();
    ArrayList<MemberDto> firebaseMemberArrList = new ArrayList<MemberDto>();

    //intent 값 선언
    String loginEmailKey;
    String state;

    //사용자 입력이 있을 변수 선언
    TextView txtBarTitle; //Actionbar 대신에 띄울 바 이름 객체
    Button btnEditInfo; //입력한 데이터를 전송할 버튼 객체
    TextView txtEtxtHint; //edit text를 설명할 이름 객체
    EditText etxtEmptySpace; //원하는 데이터를 입력할 빈 텍스트 객체

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);

        //intent 값 저장
        loginEmailKey = getIntent().getStringExtra("loginEmailValue");
        //intent 값 저장
        state = getIntent().getStringExtra("editState");

        //사용자 입력이 있을 객체를 초기화
        txtBarTitle = (TextView) findViewById(R.id.edit_txt_bartitle);
        btnEditInfo = (Button) findViewById(R.id.edit_btn_editInfo);
        txtEtxtHint = (TextView) findViewById(R.id.edit_txt_etxthint);
        etxtEmptySpace = (EditText) findViewById(R.id.edit_etxt_emptyspace);

        //친구 찾기를 위해 EditActivity로 넘어온 경우
        if (state.equals("friendSearch")) {
            //bar title과 btn edit info, etxt hint를 변경
            txtBarTitle.setText("이메일로 친구 추가");
            btnEditInfo.setText("찾기");
            txtEtxtHint.setText("친구 이메일 주소");
            //30글자 제한 걸어놓음
            etxtEmptySpace.setFilters(new InputFilter[] { new InputFilter.LengthFilter(30) });

            //인터넷 연결 상태 확인
            int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
            //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
            if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {

                //firebase에서 데이터 가져옴
                readMemberInfoToFirebase();
            }
        }

        //프로필 수정을 위해 EditActivity로 넘어온 경우
        //이름을 변경하는 경우
        if (state.equals("nicknameUpdate")) {
            //bar title과 btn edit info, etxt hint를 변경
            txtBarTitle.setText("이름(별명) 변경");
            btnEditInfo.setText("확인");
            txtEtxtHint.setText("이름(별명)");
            etxtEmptySpace.setHint(getIntent().getStringExtra("nickname"));
            //5글자 제한 걸어놓음
            etxtEmptySpace.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });
            getIntent().removeExtra("nickname"); //적용했으면 intent 값 삭제
        }

        //프로필 상태 메세지를 변경하는 경우
        if (state.equals("messageUpdate")) {
            //bar title과 btn edit info, etxt hint를 변경
            txtBarTitle.setText("상태 메세지 변경");
            btnEditInfo.setText("확인");
            txtEtxtHint.setText("상태 메세지");
            //10글자 제한 걸어놓음
            etxtEmptySpace.setFilters(new InputFilter[] { new InputFilter.LengthFilter(10) });
            etxtEmptySpace.setHint(getIntent().getStringExtra("message"));
            getIntent().removeExtra("message"); //적용했으면 intent 값 삭제

        }
    }

    //View를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //편집 화면이므로 이메일로 친구 찾기, 이름/상태 메세지 수정 등을 할 수 있음
    @Override
    public void onResume() {
        super.onResume();

        //친구 검색을 위해 버튼을 누르는 경우
        //버튼 이벤트를 받는 리스너
        btnEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //친구 검색 버튼을 누르는 경우
                if (state.equals("friendSearch")) {
                    //Database에서 일치하는 친구가 있으면 dialog를 띄워서 물어본 뒤 친구 정보에 저장
                    //현재 edit text에 입력한 값을 MemberDAO로 전달해서 검색하는 메소드 호출
                    if(etxtEmptySpace.getText().toString().equals("")){
                        Toast.makeText(getApplicationContext(), "찾고 싶은 친구의 이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //firebase에서 데이터 가져옴
                    readMemberInfoToFirebase();

                    //친구 찾기 함수 호출
                    friendSearch();
                }
                //프로필 수정을 위한 버튼을 누르는 경우
                if (state.equals("nicknameUpdate")) {
                    //현재 edit text에 입력한 값을 MemberDAO로 전달해서 저장하는 메소드 호출
                    if(etxtEmptySpace.getText().toString().equals("")){
                        Toast.makeText(getApplicationContext(), "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateNickName();
                }
                //상태 메세지 수정을 위한 버튼을 누르는 경우
                if (state.equals("messageUpdate")) {
                    //현재 edit text에 입력한 값을 MemberDAO로 전달해서 저장하는 메소드 호출
                    if(etxtEmptySpace.getText().toString().equals("")){
                        Toast.makeText(getApplicationContext(), "상태 메세지를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateProfileMessage();
                }
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        myRef.addValueEventListener(valueEventListener);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        myRef.removeEventListener(valueEventListener);
//    }

    //이메일 값으로 친구를 검색하는 메소드
    private void friendSearch(){
        //TODO : 이메일 형식 제한 && 정규식 적용해야 함
        //firebase에 저장된 회원 정보와 대조하기 위해 인터넷 연결이 필요
        //인터넷 연결 상태 확인
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
        //이메일 값으로 친구를 검색하고 있으면 추가
        if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
            final int position = emailKeyArrList.indexOf(etxtEmptySpace.getText().toString());
            //TODO : 내 친구 정보에 없는 이메일이어야 함
            //MemberDao 검색 결과 이메일이 중복이고 나 자신이 아닌 경우
            if (emailDuplicationConfirm() == true && !loginEmailKey.equals(firebaseMemberArrList.get(position).getMemberEmail())) {
                //만일 내 친구 정보에 있는 이메일이면 이미 친구로 추가되었다고 메세지 보냄
                FriendDao friendDao = new FriendDao(EditActivity.this);
                boolean friendEmailDuplicationFlag = friendDao.friendEmailDuplicationConfirm(loginEmailKey, etxtEmptySpace.getText().toString());
                //만일 이미 친구 추가를 한 이메일인 경우
                if(friendEmailDuplicationFlag == true){
                    Toast.makeText(getApplicationContext(), "이미 등록되어있는 이메일입니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //친구 정보를 dialog에 띄워서 친구 추가할 건지 묻고, 추가한다고 하면 Friend Database에 추가
                //취소를 누르면 아무런 작업도 하지 않음
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("친구 정보");
                builder.setMessage(etxtEmptySpace.getText().toString() + "로 검색한 친구 [" +
                        firebaseMemberArrList.get(position).getMemberNickName() + "] 를 추가하시겠습니까?");

                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //취소 메세지를 알림
                                Toast.makeText(getApplicationContext(), "친구 추가를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String[] friendData = new String[5];
                                friendData[0] = firebaseMemberArrList.get(position).getMemberEmail(); //이메일 저장
                                friendData[1] = firebaseMemberArrList.get(position).getMemberNickName(); //별명 저장
                                friendData[2] = firebaseMemberArrList.get(position).getMemberProfilePhotoPath(); //사진 경로
                                friendData[3] = firebaseMemberArrList.get(position).getMemberProfileMessage(); //상태 메세지

                                Intent intent = new Intent();
                                intent.putExtra("intentData", friendData);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                builder.show();

            }
            //MemberDao 검색 결과 중복이 아닌 경우
            else {
                //친구를 찾지 못했으므로 다시 한 번 입력해달라는 Toast 메세지를 띄움
                Toast.makeText(getApplicationContext(), "친구를 찾을 수 없습니다. 이메일을 다시 입력해 주십시오.", Toast.LENGTH_SHORT).show();
            }
        }
        //인터넷이 연결되어 있지 않은 경우
        //인터넷 연결 설정을 확인해달라는 dialog를 띄움
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("인터넷 연결 확인");
            builder.setMessage("인터넷이 연결되어 있지 않아 친구를 찾을 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }
    }

    //닉네임을 수정하는 메소드
    private void updateNickName() {
        //TODO : 닉네임 5글자 제한&&정규식 적용해야 함
        String updateNickName = etxtEmptySpace.getText().toString();

        //변경된 값을 shared preference에 반영하는 쿼리 생성
        MemberDao memberDao = new MemberDao(this);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
        memberDto.setMemberNickName(updateNickName);
        memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행

        //변경된 값을 firebase에 반영하는 쿼리 생성
        memberDao.updateOneMemberInfoToFirebase(memberDto);

        //intent 날리고 종료
        Intent intent = new Intent();
        intent.putExtra("updateNickName", updateNickName);
        setResult(RESULT_OK, intent);
        finish();
    }

    //상태 메세지를 수정하는 메소드
    private void updateProfileMessage() {
        //TODO : 상태메세지도 글자 제한&&정규식 적용해야 함
        String updateProfileMessage = etxtEmptySpace.getText().toString();

        //변경된 값을 shared preference에 반영하는 쿼리 생성
        MemberDao memberDao = new MemberDao(this);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
        memberDto.setMemberProfileMessage(updateProfileMessage);
        memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행

        //변경된 값을 firebase에 반영하는 쿼리 생성
        memberDao.updateOneMemberInfoToFirebase(memberDto);

        //intent 날리고 종료
        Intent intent = new Intent();
        intent.putExtra("updateProfileMessage", updateProfileMessage);
        setResult(RESULT_OK, intent);
        finish();
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] realtime DB 데이터 검색하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void readMemberInfoToFirebase() {
        FirebaseDatabase.getInstance().getReference("memberInfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //이메일 중복 확인 arraylist 초기화
                firebaseMemberArrList.clear();
                emailKeyArrList.clear();

                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    MemberDto memberDto = fileSnapshot.getValue(MemberDto.class);
                    emailKeyArrList.add(memberDto.getMemberEmail()); //arraylist에 email값 추가
                    firebaseMemberArrList.add(memberDto); //arraylist에 memberDto 추가
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase", "[Friend]loadPost:onCanceled", databaseError.toException());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 이메일 중복 확인하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public boolean emailDuplicationConfirm() {
        boolean emailDuplicationConfirmFlag = emailKeyArrList.contains(etxtEmptySpace.getText().toString());

        //중복이 아닌 경우 false, 중복인 경우 true
        return emailDuplicationConfirmFlag;
    }
}