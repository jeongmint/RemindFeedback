package com.remindfeedbackproject.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.remindfeedbackproject.R;

import java.io.IOException;

public class RecordInfoDialog extends Dialog {
    private Context context; //Fragment에서 받아올 Context

    //녹음 파일 재생을 위한 미디어 객체
    private MediaPlayer mediaPlayer;
    boolean isPlaying = false; //미디어 플레이어 재생 플래그
    boolean initMediaPlayerFlag = true; //미디어 플레이어 상태 확인 플래그

    //fragment에서 받아온 item 내용을 저장할 변수 선언
    private String infoTitle;
    private String infoRecordLength;
    private String infoRecordPath;

    //사용자와 상호작용하는 레이아웃 변수 선언
    TextView txtTitle;
    SeekBar seekBar;
    TextView txtWatch;
    TextView txtRecordLength;
    ImageButton btnPlay;
    ImageButton btnStop;
    ImageButton btnClose;

    //생성자
    public RecordInfoDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    //받아올 값이 있을 때의 생성자
    public RecordInfoDialog(Context context, String infoTitle, String infoRecordLength, String infoRecordPath) {
        super(context);
        this.context = context;
        this.infoTitle = infoTitle;
        this.infoRecordLength = infoRecordLength;
        this.infoRecordPath = infoRecordPath;
    }

    //Dialog를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        txtTitle = (TextView) findViewById(R.id.fragment3_record_info_title);
        seekBar = (SeekBar) findViewById(R.id.fragment3_record_info_seekbar);
        txtWatch = (TextView) findViewById(R.id.fragment3_record_info_txtWatch);
        txtRecordLength = (TextView) findViewById(R.id.fragment3_record_info_txt_recordlength);
        btnPlay = (ImageButton) findViewById(R.id.fragment3_record_info_btn_play);
        btnStop = (ImageButton) findViewById(R.id.fragment3_record_info_btn_stop);
        btnClose = (ImageButton) findViewById(R.id.fragment3_record_info_btn_close);

        txtTitle.setText(infoTitle); //fragment에서 가져온 녹음 파일 이름으로 설정
        txtRecordLength.setText(infoRecordLength); //fragment에서 가져온 녹음파일 길이로 설정

        //버튼 클릭 시 리스너 설정
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int userSelectPosition = seekBar.getProgress(); //사용자가 seekbar를 바꿈
                mediaPlayer.seekTo(userSelectPosition); //재생 위치 변경
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seekBarProgress = seekBar.getProgress();
                String currentTime = String.format("%02d:%02d.%02d", seekBarProgress/1000/60%60, (seekBarProgress / 1000) % 60,
                        (seekBarProgress%1000)/10);
                txtWatch.setText(currentTime);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preparePlaying();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlaying(); //일시 정지 메소드 호출
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = false;
                dismiss();
            }
        });
    }

    private void preparePlaying() {
        try {
            isPlaying = false; //기존의 AsyncTask 종료
            if (initMediaPlayerFlag == true) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(infoRecordPath);
                mediaPlayer.prepare();
                seekBar.setMax(mediaPlayer.getDuration()); //seekbar의 최대 범위를 노래의 재생시간으로 설정
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        initMediaPlayerFlag = false;
                        startPlaying(mediaPlayer);
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        System.out.println("STOP Playing 실행 됩니까?");
                        stopPlaying(mediaPlayer);
                    }
                });
            } else {
                startPlaying(mediaPlayer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startPlaying(MediaPlayer mediaPlayer) {
        //음성 재생 시작
        isPlaying = true;

        btnPlay.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);

        //asynctask 객체 생성
        SeekBarTask seekBarTask = new SeekBarTask();
        seekBarTask.execute(1); //execute로 실행(매개변수는 아무 관련 없음)
        mediaPlayer.start();
    }

    private void pausePlaying() {
        //음성 재생 종료
        isPlaying = false;

        btnPlay.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);

        mediaPlayer.pause();
    }

    private void stopPlaying(MediaPlayer mediaPlayer) {
        System.out.println("미디어 플레이어 종료");
        //음성 재생 종료
        isPlaying = false;
        initMediaPlayerFlag = true; //미디어 플레이어 초기화 선언
        btnPlay.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);

        mediaPlayer.stop();
        mediaPlayer.release();

        seekBar.setProgress(0); //seekbar 초기화
        txtWatch.setText("00:00.00");//시간 설정
    }

    //새로운 TASK정의 (AsyncTask)
    // < >안에 들은 자료형은 순서대로 doInBackground, onProgressUpdate, onPostExecute의 매개변수 자료형을 뜻한다.
    // (내가 사용할 매개변수타입을 설정하면된다)
    public class SeekBarTask extends AsyncTask<Integer, Integer, Integer> {
        //AsyncTask 초기화
        protected void onPreExecute() {
        }

        //스레드의 작업 구현
        //여기서 매개변수 Intger ... values란 values란 이름의 Integer배열이라 생각하면된다.
        //배열이라 여러개를 받을 수 도 있다. ex) excute(100, 10, 20, 30); 이런식으로 전달 받으면 된다.
        protected Integer doInBackground(Integer... values) {
            //isCancelled()=> Task가 취소되었을때 즉 cancel당할때까지 반복
            while (true) {
                //seekBar의 위치가 녹음 파일의 최대 시간에 도달한 경우
                if (isPlaying == false) {
                    System.out.println("멈춤!");
                    break;
                } else {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            }

            return 1;
        }

        //이 Task에서(즉 이 스레드에서) 수행되던 작업이 종료되었을 때 호출됨
        protected void onPostExecute(Integer result) {
        }
    }
}
