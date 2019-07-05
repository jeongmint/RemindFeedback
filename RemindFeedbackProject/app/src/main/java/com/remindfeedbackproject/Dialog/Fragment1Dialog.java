package com.remindfeedbackproject.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.remindfeedbackproject.R;

public class Fragment1Dialog extends Dialog implements View.OnClickListener {
//    //화면에 그릴 레이아웃 객체 변수를 선언
//    private static final int LAYOUT = R.layout.fragment1_dialog;

    //atvitiy에 넘겨줄 입력값을 가지는 인터페이스 선언
    public interface Fragment1DialogListener{
        public void onCreateClicked(String etxtTitle, String etxtContent);
    }
    private Fragment1DialogListener fragment1DialogListener;//인터페이스를 멤버 변수로 선언

    //activity에서 받아온 제목, 내용과 피드백 제공자를 저장할 변수 선언
    private String updateTitle = "";
    private String updateContent = "";
    private int updatePosition = -1;

    //activity로 넘겨줄 제목과 내용을 저장할 변수 선언
    private String newTitle;
    private String newContent;
    private Context context;

    //버튼 혹은 텍스트 객체 변수 선언
    private TextView txtName;
    private EditText etxtTitle;
    private EditText etxtContent;
    private Button btnCancel;
    private Button btnCreate;

    //Fragment1Dialog 생성자
    public Fragment1Dialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    //받아올 값이 있을 때의 MainDialog 생성자
    public Fragment1Dialog(Context context, String updateTitle, String updateContent, int updatePosition){
        super(context);
        this.context = context;
        this.updateTitle = updateTitle;
        this.updateContent = updateContent;
        this.updatePosition = updatePosition;
    }

    //Fragment1DialogListener를 초기화하는 메소드
    public void setFragment1DialogListener(Fragment1DialogListener fragment1DialogListener){
        this.fragment1DialogListener=fragment1DialogListener;
    }

    //Dialog를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(LAYOUT);

        //사용자 입력이 있을 객체를 초기화
        //버튼과 텍스트 객체를 초기화
        txtName = (TextView) findViewById(R.id.fragment1_dialog_txt_name);
        etxtTitle = (EditText) findViewById(R.id.fragment1_dialog_etxt_title);
        etxtContent = (EditText) findViewById(R.id.fragment1_dialog_etxt_content);
        btnCancel = (Button) findViewById(R.id.fragment1_dialog_btn_cancel);
        btnCreate = (Button) findViewById(R.id.fragment1_dialog_btn_create);

        //버튼 클릭 시 리스너 설정
        btnCancel.setOnClickListener(this);
        btnCreate.setOnClickListener(this);

        //피드백 목록을 수정하는 경우
        //제목과 피드백 제공자 값을 가져왔는지 확인하고 집어넣음
        if(!updateTitle.equals(null) && !updateContent.equals(null) && updatePosition != -1){
            txtName.setText("게시글 수정"); //다이얼로그 이름 변경
            etxtTitle.setText(updateTitle); //기존의 제목 집어넣음
            etxtContent.setText(updateContent);//기존의 내용 집어넣음
            btnCreate.setText("수정");//추가 버튼의 텍스트를 "수정"으로 변경
        }
    }

    //취소 혹은 생성 버튼을 누른 경우
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //취소 버튼을 누른 경우
            case R.id.fragment1_dialog_btn_cancel:
                dismiss();//다이얼로그를 종료
                break;
            //생성 버튼을 누른 경우
            case R.id.fragment1_dialog_btn_create:
                //게시글에 공백이 있으면 액티비티를 완료하지 못하게 함
                //제목이 공백인 경우
                if (etxtTitle.getText().toString().equals("")) {
                    Toast.makeText(context, "게시글 제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    etxtTitle.requestFocus(); //제목에 포커스를 맞춤
                    return;
                }
                //내용이 공백인 경우
                if (etxtContent.getText().toString().equals("")) {
                    Toast.makeText(context, "게시글 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    etxtContent.requestFocus(); //제목에 포커스를 맞춤
                    return;
                }

                //액티비티로 제목과 피드백 제공자의 이름을 전달
                newTitle = etxtTitle.getText().toString(); //제목 저장
                newContent = etxtContent.getText().toString();//내용 저장

                //인터페이스 메소드 호출
                fragment1DialogListener.onCreateClicked(newTitle, newContent);

                //작성했던 제목과 내용 데이터를 다이얼로그에서 삭제
                etxtTitle.setText(null);
                etxtContent.setText(null);
                 dismiss();
                break;
        }
    }
}
