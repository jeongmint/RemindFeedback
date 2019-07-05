package com.remindfeedbackproject.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Dao.MemberDao;
import com.remindfeedbackproject.Dialog.TitleDialog;
import com.remindfeedbackproject.Dto.FolderDto;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class VideoRecordActivity extends AppCompatActivity {
    //intent 객체 선언
    String loginEmailKey;

    private Size previewSize;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;

    private TextureView textureView;
    private TextView txtWatch;
    private ImageButton btnRecord;

    //초시계 관련 변수 선언
    private long startTime; //타이머 시작 시간 선언

    private MediaRecorder mediaRecorder;

    private String[] resultIntent; //결과값을 반환할 문자열 배열
    private String newPreVideoFileTitle; //변경하기 전 기본 파일 이름
    private String newPostVideoFileTitle; //변경한 후 기본 파일 이름
    private String newVideoFileLength; //비디오 파일 길이
    private String newVideoFilePath; //비디오 파일 경로
    public String newThumbnailTitle; //녹화한 영상 썸네일 제목
    public Bitmap newThumbnailBitmap; //녹화한 영상 썸네일 사진
    private String time; //녹화 파일 이름을 정하기 위한 현재시간 변수

    //Activity를 최초 화면에 불러올 때 실행하는 메소드
    //여기서 사용할 변수들을 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videorecord_activity);

        //intent값 저장
        loginEmailKey = getIntent().getStringExtra("loginEmailKey");

        textureView = (TextureView) findViewById(R.id.videorecord_textureview);
        txtWatch = (TextView) findViewById(R.id.videorecord_txt_watch);
        btnRecord = (ImageButton) findViewById(R.id.videorecord_btn_record);

        resultIntent = new String[5];
    }

    //Activity를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //게시판 화면이므로 오늘의 피드백/피드백목록/사진/녹음/영상 등을 할 수 있음
    @Override
    protected void onResume() {
        super.onResume();

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording()) {
                    stopRecording(true);
                } else {
                    startRecording();
                }
            }
        });

        startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording(false);
    }

    private void startPreview() {
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    private void stopPreview() {
        if (previewSession != null) {
            previewSession.close();
            previewSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            String backCameraId = null;

            try {
                for (final String cameraId : manager.getCameraIdList()) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                    int cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);

                    if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                        backCameraId = cameraId;
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            if (backCameraId == null) {
                return;
            }

            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(backCameraId);

            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            manager.openCamera(backCameraId, deviceStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void showPreview() {
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        Surface surface = new Surface(surfaceTexture);

        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), captureStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        try {
            previewSession.setRepeatingRequest(previewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean isRecording() {
        return mediaRecorder != null;
    }

    private void startRecording() {
//        //버튼 보이고 감추기
//        btnPlay.setVisibility(View.GONE); //재생버튼은 감춤
//        btnStop.setVisibility(View.VISIBLE);//정지 버튼 보이게 함

        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }

        String recordFilePath = getOutputMediaFile().getAbsolutePath();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        // 현재 앱이 실행되는 카메라가 지원하는
        // 최대 화질의 설정 프로필을 구한다.
        // 안드로이드 기기에 탑재되는 카메라에 따라
        // 결정되는 녹화 사이즈 등은 모두 달라진다.
        CamcorderProfile camcorderProfile
                = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);

        // 일부 기종에서 최대 화질 프로필 과
        // 카메라 하드웨어 가 지원하는 최대 사이즈가 다른 문제가 발생한다.
        // 실제 카메라 하드웨어로 부터 구해진 previewSize 와
        // 최대 화질 프로필 API 를 통해 구해진 최대 지원 사이즈를 비교해서
        // 더 작은 쪽으로 설정을 바꾼다.
        if (camcorderProfile.videoFrameWidth > previewSize.getWidth()
                || camcorderProfile.videoFrameHeight > previewSize.getHeight()) {
            camcorderProfile.videoFrameWidth = previewSize.getWidth();
            camcorderProfile.videoFrameHeight = previewSize.getHeight();
        }

        mediaRecorder.setProfile(camcorderProfile);
        mediaRecorder.setOutputFile(recordFilePath);
        mediaRecorder.setOrientationHint(90);

        // 넥서스 5x 의 OS 버전 8.1.0 에서는
        // 다른 기종들과는 반대로 화면을 회전해야 하는 문제가 발생한다.
        // 테스트폰이 넥서스 5x 라면 다음 줄을 주석 해제 한다.
//        mediaRecorder.setOrientationHint(270);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<Surface> surfaces = new ArrayList<>();

        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        Surface previewSurface = new Surface(surfaceTexture);
        surfaces.add(previewSurface);

        Surface mediaRecorderSurface = mediaRecorder.getSurface();
        surfaces.add(mediaRecorderSurface);

        try {
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            previewBuilder.addTarget(previewSurface);
            previewBuilder.addTarget(mediaRecorderSurface);

            cameraDevice.createCaptureSession(surfaces, captureStateCallback, null);

            mediaRecorder.start();

            //시간 체크 시작
            startTime = SystemClock.elapsedRealtime();
            timer.sendEmptyMessage(0); //핸들러에 빈 메세지를 보내서 호출
            btnRecord.setImageResource(R.drawable.anim_vector_stop_to_record);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording(boolean showPreview) {
        stopPreview();

        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }

//        if (showPreview) {
//            startPreview();
//        }

        //시간 체크 종료
        timer.removeMessages(0); //삭제 메세지 호출
        newVideoFileLength = txtWatch.getText().toString(); //화면에 표시된 길이 가져와 저장
        btnRecord.setImageResource(R.drawable.anim_vector_record_to_stop);

        //제목을 변경하는 다이얼로그 호출
        setRenameDialog();
    }

    private File getOutputMediaFile() {
        MemberDao memberDao = new MemberDao(VideoRecordActivity.this);
        FolderDto folderDto = memberDao.readMemberDataPathInfo(loginEmailKey);

        setVideoDate(); //녹화 파일 이름을 결정하기 위해 시간을 정함
        String videoPath = folderDto.getVideoDirectoryPath();
        newPreVideoFileTitle = "MyVideo" + "_" + time + ".mp4";
        newVideoFilePath = videoPath + "/" + newPreVideoFileTitle;

        File video = new File(videoPath + "/", newPreVideoFileTitle);

        return video;
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            showPreview();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
            stopRecording(false);
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    private CameraCaptureSession.StateCallback captureStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            previewSession = session;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    //현재 날짜를 저장하는 메소드
    private void setVideoDate() {
        long nowTime = System.currentTimeMillis(); //현재 시간을 시스템에서 가져옴
        Date date = new Date(nowTime); //Date 생성하기
        SimpleDateFormat timeSDF = new SimpleDateFormat("yyyyMMdd_hhmmss");//yyyy.MM.dd a hh:mm:ss 형식으로 가져옴

        time = timeSDF.format(date); //시간을 time에 저장
    }

    //파일 제목을 변경하는 다이얼로그 호출 메소드
    private void setRenameDialog() {
        //피드백 작성 다이얼로그 메소드 호출
        //다이얼로그 객체 생성
        TitleDialog renameDialog = new TitleDialog(this, "create", newPreVideoFileTitle);
        //다이얼로그 레이아웃 지정
        renameDialog.setContentView(R.layout.title_dialog);

        //다이얼로그를 보여주기 전 리스너 등록
        renameDialog.setTitleDialogListener(new TitleDialog.TitleDialogListener() {
            //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
            @Override
            public void onClicked(String state, String fileTitle) {
                //취소 버튼을 누른 경우
                if (state.equals("cancel")) {
                    //저장한 파일을 삭제(아직 사진 파일은 생성하지 않음)
                    File file = new File(newVideoFilePath); //파일 경로를 가져옴
                    file.delete();//해당하는 파일 삭제

                    //녹화를 취소했으므로 아무것도 넘겨주지 않음
                    setResult(RESULT_CANCELED);
                    finish();
                }

                //생성 버튼을 누른 경우
                else if (state.equals("create")) {
                    newPostVideoFileTitle = fileTitle;

//                    //파일 이름 변경
//                    File preFile = new File(newVideoFilePath, newPreVideoFileTitle);
//                    File postFile = new File(newVideoFilePath, newPostVideoFileTitle);
//                    preFile.renameTo(postFile); //파일 이름 변경

                    resultIntent[0] = newPostVideoFileTitle;
                    resultIntent[1] = newVideoFilePath;
                    resultIntent[2] = time;

                    //제목, 길이, 경로를 인텐트로 넘겨줌
                    Intent intent = new Intent();
                    intent.putExtra("intentData", resultIntent);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        //다이얼로그 크기 조정
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
        int width = displayMetrics.widthPixels;//디바이스 화면 너비
        int height = displayMetrics.heightPixels;//디바이스 화면 높이

        WindowManager.LayoutParams wm = renameDialog.getWindow().getAttributes();
        wm.width = width;//화면의 가로 넓이
        wm.height = height / 3;//화면의 세로 넓이
        renameDialog.getWindow().setAttributes(wm);
        //게시판 생성 다이얼로그 호출
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
        String currentTime = String.format("%02d:%02d:%02d",(betweenTime/1000/60/60), (betweenTime / 1000/60)%60, (betweenTime / 1000) % 60);
        return currentTime;
    }

//    //비디오 파일 길이를 저장하는 메소드
//    private void setVideoLength(String videoTitle){
//        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//        mediaMetadataRetriever.setDataSource(newVideoFilePath + "/" + videoTitle);
//        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        long timeInmillisec = Long.parseLong(time);
//        long duration = timeInmillisec/1000;
//        long hours = duration /3600;
//        long minutes = (duration - hours*3600)/60;
//        long seconds = duration - (hours*3600 + minutes*60);
//        newVideoFileLength = hours + ":" + minutes + ":" + seconds;
//    }
//
//    //비디오 썸네일 이미지를 가져오는 메소드
//    private void getVideoThumbnailImage(String videoTitle){
//        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//        mediaMetadataRetriever.setDataSource(newVideoFilePath + "/" + videoTitle);
//
//        newThumbnailBitmap = mediaMetadataRetriever.getFrameAtTime(1000); //1초의 장면을 가져옴
//
//        SaveBitmapToPNG(videoTitle); //만든 비트맵 이미지를 PNG 파일로 저장
//    }
//
//    //비트맵을 JPG 파일로 저장하는 메소드
//    private void SaveBitmapToPNG(String videoTitle){
//
//        //.을 기준으로 비디오 제목 문자열을 잘라 확장자를 제거
//        int index = videoTitle.indexOf("."); //.의 인덱스를 찾는다
//        String titleResult = videoTitle.substring(0, index); //인덱스 앞부분 추출
//        newThumbnailTitle = titleResult + ".png";
//        File file = new File(newVideoFilePath + "/" + newThumbnailTitle);
//        OutputStream outputStream = null;
//
//        try{
//            file.createNewFile();
//            outputStream = new FileOutputStream(file);
//
//            newThumbnailBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            try{
//                outputStream.close();
//            }catch (IOException e){
//            }
//        }
//    }
}