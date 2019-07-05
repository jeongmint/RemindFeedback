package com.remindfeedbackproject.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dto.FolderDto;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Fragment3Dialog extends Dialog implements View.OnClickListener {
    //atvitiy에 넘겨줄 입력값을 가지는 인터페이스 선언
    public interface Fragment3DialogListener {
        public void onCreateClicked(String fileTitle, String fileLength, String filePath);
    }

    private Fragment3DialogListener fragment3DialogListener;//인터페이스를 멤버 변수로 선언
    private Context context;

    private long startTime; //타이머 시작 시간 선언

    //녹음 파일을 저장하기 위한 경로 지정
    private String time; //녹음 파일 이름을 정하기 위한 현재시간 변수
    private boolean recordFlag = false;//녹음 상태를 표시하는 플래그 변수

    //녹음을 하기 위한 변수 선언
    MediaRecorder mediaRecorder;

    //activity에서 받아온 변수 선언
    private String loginEmailKey; //로그인 이메일 키
    private String state; //다이얼로그 상태 결정 변수

    //activity로 넘겨줄 제목과 재생 시간을 저장할 변수 선언
    private String newPreRecordTitle = "";
    private String newPostRecordTitle = "";
    private String newRecordLength = "";
    private String newRecordPath = "";

    //버튼 혹은 텍스트 객체 변수 선언
    //생성할 때
    private TextView txtWatch;
    private Button btnPlay;
    private Button btnStop;
    private Button btnClose;

    //생성자
    public Fragment3Dialog(@NonNull Context context, String loginEmailKey, String state) {
        super(context);
        this.context = context;
        this.loginEmailKey = loginEmailKey;
        this.state = state;
    }

    //Fragment3DialogListener를 초기화하는 메소드
    public void setFragment3DialogListener(Fragment3DialogListener fragment3DialogListener) {
        this.fragment3DialogListener = fragment3DialogListener;
    }

    //Dialog를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //사용자 입력이 있을 객체를 초기화
//        chronometer = (Chronometer) findViewById(R.id.record_chronometer);
        txtWatch = (TextView) findViewById(R.id.record_txt_watch);
        btnPlay = (Button) findViewById(R.id.record_btn_play);
        btnStop = (Button) findViewById(R.id.record_btn_stop);
        btnClose = (Button) findViewById(R.id.record_btn_close);

        //버튼 클릭 시 리스너 설정
        btnPlay.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnClose.setOnClickListener(this);

    }

    //버튼 입력 이벤트가 일어난 경우
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //재생 버튼을 누르는 경우
            case R.id.record_btn_play:
                //녹음 메소드 호출
                recordPlay();
                break;
            //정지 버튼을 누르는 경우
            case R.id.record_btn_stop:
                //취소를 누를 경우 녹음했던 거 저장하지 않음
                recordStop();
                break;
            //정지 버튼을 누르는 경우
            case R.id.record_btn_close:
                dismiss();
                break;
        }
    }

    //녹음을 하는 메소드
    public void recordPlay() {
        //레코드 플래그 true 로 만듦
        recordFlag = true;

        //버튼 보이고 감추기
        btnPlay.setVisibility(View.GONE); //재생버튼은 감춤
        btnStop.setVisibility(View.VISIBLE);//정지 버튼 보이게 함
        btnClose.setVisibility(View.GONE); //닫기버튼은 감춤

        //새로 녹음을 하는 경우
        mediaRecorder = new MediaRecorder();
        //마이크의 소리를 녹음하고 저장할 포맷 지정
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //마이크에서 음성 데이터를 받음
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //압축 포맷을 지정
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        //경로 지정
        setTime();//파일 명을 정하기 위해 현재 시간 구함
        newPreRecordTitle = "MyRecord" + "_" + time + ".mp3"; //파일 이름 지정

        MemberDao memberDao = new MemberDao(context);
        FolderDto folderDto = memberDao.readMemberDataPathInfo(loginEmailKey);
        String voiceDirectoryPath = folderDto.getVoiceDirectoryPath();

        newRecordPath = voiceDirectoryPath + "/" + newPreRecordTitle; //파일 경로 지정
        mediaRecorder.setOutputFile(newRecordPath);
        try {
            Toast.makeText(context.getApplicationContext(), "녹음을 시작합니다.", Toast.LENGTH_LONG).show();
            mediaRecorder.prepare();
            mediaRecorder.start();
            //시간 체크 시작
            startTime = SystemClock.elapsedRealtime();
            timer.sendEmptyMessage(0); //핸들러에 빈 메세지를 보내서 호출
        } catch (IOException e) {
            Log.e("AudioRecorder", "Exception : ", e);
            e.printStackTrace();
        }
    }

    //녹음을 중지하는 메소드
    public void recordStop() {
        //레코드 플래그 true로 만듦
        recordFlag = false;

        //녹음을 하고 있지 않은 경우
        if (mediaRecorder == null) {
            return;
        }
        //녹음을 하고 있는 경우
        //버튼 보이고 감추기
        btnPlay.setVisibility(View.VISIBLE); //재생버튼 보이게 함
        btnStop.setVisibility(View.GONE);//정지 버튼 감춤
        btnClose.setVisibility(View.VISIBLE); //닫기버튼은 보이게 함

        //정지하고 recorder를 null로 초기화
        Toast.makeText(context.getApplicationContext(), "녹음을 중지합니다.", Toast.LENGTH_LONG).show();
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        //시간 체크 종료
        timer.removeMessages(0); //삭제 메세지 호출
        newRecordLength = txtWatch.getText().toString(); //화면에 표시된 길이 가져와 저장

        //피드백 작성 다이얼로그 메소드 호출
        //다이얼로그 객체 생성
        TitleDialog renameDialog = new TitleDialog(context, "create", newPreRecordTitle);
        //다이얼로그 레이아웃 지정
        renameDialog.setContentView(R.layout.title_dialog);

        //다이얼로그를 보여주기 전 리스너 등록
        renameDialog.setTitleDialogListener(new TitleDialog.TitleDialogListener() {
            //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
            @Override
            public void onClicked(String state, String fileTitle) {
                //취소 버튼을 누른 경우
                if (state.equals("cancel")) {
                    //저장한 파일을 삭제
                    File file = new File(newRecordPath); //파일 경로를 가져옴
                    file.delete();//해당하는 파일 삭제
                    dismiss();//다이얼로그를 종료
                }

                //생성 버튼을 누른 경우
                else if (state.equals("create")) {
                    newPostRecordTitle = fileTitle;

//                    //파일 이름 변경
//                    File preFile = new File(newRecordPath, newPreRecordTitle);
//                    File postFile = new File(newRecordPath, newPostRecordTitle);
//                    preFile.renameTo(postFile); //파일 이름 변경

                    //인터페이스 메소드 호출
                    fragment3DialogListener.onCreateClicked(newPostRecordTitle, newRecordLength, newRecordPath);
                    dismiss();
                }
            }
        });

        //다이얼로그 크기 조정
        DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
        int width = displayMetrics.widthPixels;//디바이스 화면 너비
        int height = displayMetrics.heightPixels;//디바이스 화면 높이

        WindowManager.LayoutParams wm = renameDialog.getWindow().getAttributes();
        wm.width = width;//화면의 가로 넓이
        wm.height = height / 3;//화면의 세로 넓이
        renameDialog.getWindow().setAttributes(wm);

        //게시글 이름 변경 다이얼로그 호출
        renameDialog.show();
    }

    //타이머 핸들러
    Handler timer = new Handler() {
        public void handleMessage(Message msg) {
            txtWatch.setText(setCurrentTimeAtScreen());

            //비어있는 메세지를 핸들러에 전송
            timer.sendEmptyMessage(0);
        }
    };

    //현재 시간을 계속 출력하는 메소드
    private String setCurrentTimeAtScreen() {
        long stopTime = SystemClock.elapsedRealtime(); //애플리케이션이 실행된 후 실제로 경과된 시간
        long betweenTime = stopTime - startTime;
        String currentTime = String.format("%02d:%02d.%02d", (betweenTime / 1000 / 60) % 60, (betweenTime / 1000) % 60, (betweenTime % 1000) / 10);
        return currentTime;
    }

    //현재 날짜를 저장하는 메소드
    private void setTime() {
        long nowTime = System.currentTimeMillis(); //현재 시간을 시스템에서 가져옴
        Date date = new Date(nowTime); //Date 생성하기
        SimpleDateFormat timeSDF = new SimpleDateFormat("yyyyMMdd_hhmmss");//yyyy.MM.dd a hh:mm:ss 형식으로 가져옴

        time = timeSDF.format(date); //시간을 time에 저장
    }
}