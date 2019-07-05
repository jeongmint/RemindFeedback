package com.remindfeedbackproject.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.remindfeedbackproject.R;

public class TitleDialog extends Dialog implements View.OnClickListener {
    //atvitiy에 넘겨줄 입력값을 가지는 인터페이스 선언
    public interface TitleDialogListener {
        //파일의 이름을 넘겨줌
        void onClicked(String dialogState, String fileTitle);
    }

    private TitleDialogListener titleDialogListener;//인터페이스를 멤버 변수로 선언
    private Context context; //Fragment에서 받아올 Context

    //생성인지 수정인지 다이얼로그가 열린 형태를 결정하는 변수 선언
    private String state;

    //activity에서 받아온 파일의 제목을 저장할 변수 선언
    private String newFileTitle;

    //activity로 넘겨줄 제목과 재생 시간을 저장할 변수 선언
    private String updateFileTitle;

    //버튼 혹은 텍스트 객체 변수 선언
    //생성 및 수정할 때
    private TextView txtGuideTitle;
    private TextView txtGuideContent;
    private EditText etxtTitle;
    private Button btnCancel;
    private Button btnCreate;

    //Fragment1Dialog 생성자
    public TitleDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        this.state = "create"; //기본 state는 생성
    }

    //받아올 값이 있을 때의 생성자
    public TitleDialog(Context context, String state, String updateFileTitle) {
        super(context);
        this.context = context;
        this.state = state;
        this.updateFileTitle = updateFileTitle;
    }

    //Fragment3DialogListener를 초기화하는 메소드
    public void setTitleDialogListener(TitleDialogListener titleDialogListener) {
        this.titleDialogListener = titleDialogListener;
    }

    //Dialog를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        txtGuideTitle = (TextView) findViewById(R.id.title_dialog_txt_guidetitle);
        txtGuideContent = (TextView) findViewById(R.id.title_dialog_txt_guidecontent);
        etxtTitle = (EditText) findViewById(R.id.title_dialog_etxt_title);
        btnCancel = (Button) findViewById(R.id.title_dialog_btn_cancel);
        btnCreate = (Button) findViewById(R.id.title_dialog_btn_create);

        //사용자 입력이 있을 객체를 초기화
        //버튼과 텍스트 객체를 초기화
        //게시글 생성을 위해 호출한 다이얼로그인 경우
        etxtTitle.setText(updateFileTitle); //기존의 제목을 이름으로 넣음

        //게시글 수정을 위해 호출한 다이얼로그인 경우
        if (state.equals("update")) {
            txtGuideTitle.setText("게시글 이름 수정");
            btnCreate.setText("수정");
        }

        //친구 이름 수정을 위해 호출한 다이얼로그인 경우
        if (state.equals("updateName")) {
            txtGuideTitle.setText("친구 이름 수정");
            btnCreate.setText("수정");
        }

        //비밀번호를 찾기 위해 호출한 다이얼로그인 경우
        if (state.equals("findpw")) {
            txtGuideTitle.setText("비밀번호 찾기");
            txtGuideContent.setText("회원 가입 시 사용했던 이메일을 입력해주세요.");
            btnCreate.setText("찾기");
        }

        //회원 탈퇴를 위해 호출한 다이얼로그인 경우
        if (state.equals("deleteAccount")) {
            txtGuideTitle.setText("회원 탈퇴");
            txtGuideContent.setText("회원 탈퇴를 위해 계정의 비밀번호를 입력해주세요.");
            etxtTitle.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnCreate.setText("확인");
        }

        //버튼 클릭 시 리스너 설정
        btnCancel.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
    }

    //버튼 입력 이벤트가 일어난 경우
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //취소 버튼을 누른 경우
            case R.id.title_dialog_btn_cancel:
                //액티비티로 제목과 피드백 제공자의 이름을 전달
                newFileTitle = etxtTitle.getText().toString(); //제목 저장

                //인터페이스 메소드 호출
                titleDialogListener.onClicked("cancel", newFileTitle);
                dismiss();//다이얼로그를 종료
                break;
            //생성 버튼을 누른 경우
            case R.id.title_dialog_btn_create:
                //게시글에 공백이 있으면 다이얼로그를 완료하지 못하게 함
                //제목이 공백인 경우
                if (etxtTitle.getText().toString().equals("")) {
                    //게시글 수정을 위해 호출한 다이얼로그인 경우
                    if (state.equals("update")) {
                        Toast.makeText(context, "게시글 제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        etxtTitle.requestFocus(); //제목에 포커스를 맞춤
                        return;
                    }
                    //친구 이름 수정을 위해 호출한 다이얼로그인 경우
                    else if (state.equals("updateName")) {
                        Toast.makeText(context, "친구 이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        etxtTitle.requestFocus(); //제목에 포커스를 맞춤
                        return;
                    }
                    //비밀번호를 찾기 위해 호출한 다이얼로그인 경우
                    else if (state.equals("findpw")) {
                        Toast.makeText(context, "계정 이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        etxtTitle.requestFocus(); //제목에 포커스를 맞춤
                        return;
                    }
                    //회원 탈퇴를 위해 호출한 다이얼로그인 경우
                    else if (state.equals("deleteAccount")) {
                        Toast.makeText(context, "계정 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        etxtTitle.requestFocus(); //제목에 포커스를 맞춤
                        return;
                    }
                }

                //액티비티로 제목과 피드백 제공자의 이름을 전달
                newFileTitle = etxtTitle.getText().toString(); //제목 저장

                //인터페이스 메소드 호출
                titleDialogListener.onClicked("create", newFileTitle);

                dismiss();//다이얼로그를 종료
                //작성했던 제목과 내용 데이터를 다이얼로그에서 삭제
                etxtTitle.setText(null);
                break;
        }
    }
}
