package com.remindfeedbackproject.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dao.FriendDao;
import com.remindfeedbackproject.Dto.FriendDto;

import java.util.ArrayList;

public class MainDialog extends Dialog implements View.OnClickListener {
    private Context context; //Activity에서 받아올 Context 객체 선언
//    private static final int LAYOUT = R.layout.main_dialog; //화면에 그릴 레이아웃 변수 선언

    //atvitiy에 넘겨줄 입력값을 가지는 인터페이스 선언
    public interface MainDialogListener {
        public void onClicked(String feedbackTitle, String adviserEmail, String adviserNickName, String adviserPhotoPath);
    }

    private MainDialogListener mainDialogListener;//인터페이스를 멤버 변수로 선언

    //activity에서 받아올 값에 대한 변수 선언
    private String dialogState;
    private String loginEmailKey;
    private String feedbackTitle;
    private String adviserEmail;
    private String adviserNickName;
    private String adviserPhotoPath;

    //사용자 입력이 있을 변수 선언
    //Spinner(친구목록) 관련 객체 변수 선언
    ArrayList<FriendDto> friendArrList;//친구목록을 담을 빈 데이터 리스트 변수
    ArrayAdapter<String> arrayAdapter;//spinner 객체와 친구목록 arrayList를 연결할 Adapter 객체
    Spinner spinner; //스피너 객체 변수

    //버튼 혹은 텍스트 객체 변수 선언
    private TextView txtName;
    private EditText etxtTitle;
    private Button btnCancel;
    private Button btnCreate;

    //MainDialog 생성자
    public MainDialog(@NonNull Context context, String dialogState, String loginEmailKey) {
        super(context);
        this.context = context;
        this.dialogState = dialogState;
        this.loginEmailKey = loginEmailKey;
        this.feedbackTitle = "NONE";
        this.adviserEmail = "NONE";
        this.adviserNickName = "NONE";
        this.adviserPhotoPath = "NONE";
    }

    //받아올 값이 있을 때의 MainDialog 생성자
    public MainDialog(Context context, String dialogState, String loginEmailKey, String feedbackTitle,
                      String adviserEmail, String adviserNickName, String adviserPhotoPath) {

        super(context);
        this.context = context;
        this.dialogState = dialogState;
        this.loginEmailKey = loginEmailKey;
        this.feedbackTitle = feedbackTitle;
        this.adviserEmail = adviserEmail;
        this.adviserNickName = adviserNickName;
        this.adviserPhotoPath = adviserPhotoPath;
    }

    //MainDialogListener를 초기화하는 메소드
    public void setMainDialogListener(MainDialogListener mainDialogListener) {
        this.mainDialogListener = mainDialogListener;
    }

    //Dialog를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(LAYOUT);

        //친구 목록을 가져올 객체 선언
        friendArrList = new ArrayList<FriendDto>(); //친구 목록을 담을 빈 데이터 리스트 생성
        final FriendDao friendDao = new FriendDao(context); //친구 목록을 가져올 FriendDao 객체 생성
        //친구 정보에서 이름만 빼내서 저장할 arrayList 객체
        ArrayList<String> friendNickNameArrList = new ArrayList<String>();

        //등록한 친구가 없는 경우
        if (friendDao.getFriendListValue(loginEmailKey).equals("NONE")) {
            friendNickNameArrList.add("없음"); //"없음" 선택지 추가
        }
        //등록한 친구가 있는 경우
        else {
            friendNickNameArrList.add("없음"); //"없음" 선택지 추가

            //FriendDao 객체 생성해서 데이터를 array list에 넣음
            //회원 탈퇴 정보를 제외한 모든 친구 정보를 가지고 옴
            friendArrList = friendDao.readAllAliveFriendInfo(loginEmailKey);

            //친구 목록에서 친구 이름 + 이메일만 friendNickNameArrList에 저장
            for (int i = 0; i < friendArrList.size(); i++) {
                //만일 친구와 내가 쌍방 관계인 경우
                if (friendArrList.get(i).getFriendRelationship().equals("true")) {
                    friendNickNameArrList.add(friendArrList.get(i).getFriendNickName() + "(" + friendArrList.get(i).getFriendEmail() + ")");
                }
            }
        }

        arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, friendNickNameArrList);
        spinner = (Spinner) findViewById(R.id.main_dialog_spin_adviser);//스피너 객체 초기화
        spinner.setAdapter(arrayAdapter);//spinner에 adapter를 넣음

        //버튼과 텍스트 객체를 초기화
        txtName = (TextView) findViewById(R.id.main_dialog_txt_name);
        etxtTitle = (EditText) findViewById(R.id.main_dialog_etxt_title);
        btnCancel = (Button) findViewById(R.id.main_dialog_btn_cancel);
        btnCreate = (Button) findViewById(R.id.main_dialog_btn_create);

        //스피너가 선택된 경우 실행되는 예제
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //등록한 친구가 없는 경우
                if (friendDao.getFriendListValue(loginEmailKey).equals("NONE")) {
                    adviserEmail = "NONE"; //이메일이 없으므로 "NONE"값 집어넣음
                    adviserNickName = "없음"; //이름이 없으므로 "없음"값 집어넣음
                    adviserPhotoPath = "NONE"; //사진이 없으므로 NONE값 집어넣음
                }
                //등록한 친구가 있는 경우
                else {
                    //spinner 객체에서 제일 첫 번째 값을 선택한 경우
                    if (position == 0) {
                        adviserEmail = "NONE"; //이메일이 없으므로 "NONE"값 집어넣음
                        adviserNickName = "없음"; //이름이 없으므로 "없음"값 집어넣음
                        adviserPhotoPath = "NONE"; //사진이 없으므로 NONE값 집어넣음
                    }
                    //spinner 객체에서 첫 번째 값이 아닌 값을 선택한 경우
                    if (position > 0) {
                        //선택한 피드백 제공자의 이메일, 이름(별명)과 프로필 사진을 저장
                        //friendNickNameArrList의 첫 번째 값을 제외해야 하므로 position 값 1 감소
                        adviserEmail = friendArrList.get(position - 1).getFriendEmail();
                        adviserNickName = friendArrList.get(position - 1).getFriendNickName();
                        adviserPhotoPath = friendArrList.get(position - 1).getFriendProfilePhotoPath();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //버튼 클릭 시 리스너 설정
        btnCancel.setOnClickListener(this);
        btnCreate.setOnClickListener(this);

        //피드백 목록을 수정하는 경우
        //제목과 피드백 제공자 값을 가져왔는지 확인하고 집어넣음
        if (dialogState.equals("update")) {
            txtName.setText("피드백 수정"); //다이얼로그 이름 변경
            etxtTitle.setText(feedbackTitle); //기존의 제목 집어넣음

            //adapter의 개수만큼 반복문을 실행해 adapter의 item 값과 NickName이 일치하면 해당 position 값 설정
            boolean nickNameConfirmFlag = false;
            for (int i = 0; i < arrayAdapter.getCount(); i++) {
                System.out.println("arrayAdapter의 값은 " + arrayAdapter.getItem(i) + " 입니다.");
                System.out.println("item의 값은 " + adviserNickName + "(" + adviserEmail + ")" + " 입니다.");
                //item 값을 저장
                String item = adviserNickName + "(" + adviserEmail + ")";

                //adapter의 item 값과 NickName이 일치하는 경우
                if (arrayAdapter.getItem(i).equals(item)) {
                    spinner.setSelection(i); //해당 position 값으로 spinner 설정
                    nickNameConfirmFlag = true; //flag를 true로 설정
                    break;
                }
            }
            //반복문을 다 돌았는데도 닉네임을 찾지 못한 경우
            if (nickNameConfirmFlag == false) {
                spinner.setSelection(0); //제일 첫 번째 값으로 설정
            }
            btnCreate.setText("수정");//추가 버튼의 텍스트를 "수정"으로 변경
        }
    }

    //취소 혹은 생성 버튼을 누른 경우
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //취소 버튼을 누른 경우
            case R.id.main_dialog_btn_cancel:
                dismiss();//다이얼로그를 종료
                break;
            //생성 버튼을 누른 경우
            case R.id.main_dialog_btn_create:
                //MainActivity로 제목과 피드백 제공자의 이름을 전달
                //피드백 제공자의 이름은 선택할 때 저장도 같이 하므로 제목만 저장함
                feedbackTitle = etxtTitle.getText().toString(); //제목 저장
                //인터페이스 메소드 호출
                mainDialogListener.onClicked(feedbackTitle, adviserEmail, adviserNickName, adviserPhotoPath);
                etxtTitle.setText(null);//내용을 지움
                spinner.setSelection(0);//골랐던 내용을 초기화
                dismiss();
                break;
        }
    }
}