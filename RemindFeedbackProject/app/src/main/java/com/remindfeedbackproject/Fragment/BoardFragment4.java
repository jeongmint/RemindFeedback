package com.remindfeedbackproject.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.remindfeedbackproject.Etc.NetworkStatus;
import com.remindfeedbackproject.R;
import com.remindfeedbackproject.Activity.VideoPlayActivity;
import com.remindfeedbackproject.Activity.VideoRecordActivity;
import com.remindfeedbackproject.Adapter.Fragment4Adapter;
import com.remindfeedbackproject.Dao.VideoDao;
import com.remindfeedbackproject.Dialog.TitleDialog;
import com.remindfeedbackproject.Dto.VideoDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class BoardFragment4 extends Fragment {
    //bundle에서 받아올 사용자 로그인 키
    String loginEmailKey;
    String feedbackKey;
    String feedbackTag;
    String ONGOING_FEEDBACKLIST;
    String COMPLETE_FEEDBACKLIST;
    int feedbackPosition;

    private Context context; //Toast 메세지를 전달하기 위한 context 선언
    View view; //fragment 뷰 선언

    private final int VIDEO_RECORD_CODE = 170; //영상녹화 코드

    //사용자 입력이 있을 변수 선언
    //Recycler View 관련 객체 변수 선언
    ArrayList<VideoDto> videoArrList; //recyclerview를 담을 빈 데이터 리스트 변수
    RecyclerView recyclerView; //recyclerview 객체 변수
    Fragment4Adapter fragment4Adapter;//recycler view와 연결할 Adapter 객체
    RecyclerView.LayoutManager layoutManager; //recyclerview 레이아웃매니저 객체 변수

    FloatingActionButton fbtnCreate; //추가 버튼 객체

    //Fragment에 쓰일 View를 초기화하는 메소드
    //LayoutInflater를 인자로 받아서 layout으로 설정한 XML을 연결하거나 bundle에 의한 작업을 함
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.board_fragment4, container, false);

        //Bundle에서 값 받아옴
        loginEmailKey = getArguments().getString("loginEmailKey");
        feedbackKey = getArguments().getString("feedbackKey");
        feedbackTag = getArguments().getString("feedbackTag");
        ONGOING_FEEDBACKLIST = getArguments().getString( "ONGOING_FEEDBACKLIST");
        COMPLETE_FEEDBACKLIST = getArguments().getString("COMPLETE_FEEDBACKLIST");
        feedbackPosition = getArguments().getInt("feedbackPosition");

        //Toast 메세지를 보여주기 위한 context 선언
        context = container.getContext();

        //사용자 입력이 있을 객체를 초기화
        //Recycler View 관련 객체 초기화
        videoArrList = new ArrayList<VideoDto>(); //피드백 목록을 담을 빈 데이터 리스트 생성

        //VoiceDao에서 친구 목록 불러와서 저장
        VideoDao videoDao = new VideoDao(getContext());
        //친구 목록이 있는지 없는지 확인
        //친구목록이 있는 경우(NONE을 반환하지 않은 경우)에만 친구목록 저장
        if(!videoDao.getVideoListValue(loginEmailKey, feedbackKey).equals("NONE")) {
            videoArrList = videoDao.readAllVideoInfo(loginEmailKey, feedbackKey);
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.fragment4_recyclerview); //recyclerview 생성
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(context, 2);//recycler view 아이템을 linear 형태로 보여줌
        recyclerView.setLayoutManager(layoutManager);//layout manager 설정
        fragment4Adapter = new Fragment4Adapter(videoArrList);
        recyclerView.setAdapter(fragment4Adapter);

        //화면 버튼 관련 객체 초기화
        fbtnCreate = (FloatingActionButton) view.findViewById(R.id.fragment4_fbtn_create);

        //만일 진행 중 피드백 목록 탭을 선택한 경우
        if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
            fbtnCreate.show();
        }
        //만일 완료한 피드백 목록 탭을 선택한 경우
        else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
            fbtnCreate.hide();
        }

        return view;
    }

    //View를 화면에 완전히 불러온 후 실행하는 메소드
    //사용자가 화면과 상호작용을 할 때 일어나는 일에 대한 작업을 함
    //오늘의 피드백 화면이므로 오늘 작성한 피드백 확인/새로운 피드백 작성 등을 할 수 있음
    @Override
    public void onResume() {
        super.onResume();

        //Recycler view의 아이템을 누르는 경우
        fragment4Adapter.setItemClick(new Fragment4Adapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                String[] videoData = new String[3];
                videoData[0] = videoArrList.get(position).getVideoTitle();
                videoData[1] = videoArrList.get(position).getVideoLength();
                videoData[2] = videoArrList.get(position).getVideoPath();
                intent.putExtra("intentData", videoData);
                startActivity(intent);
            }
        });

        //Recycler view의 아이템을 길게 누르는 경우
        fragment4Adapter.setItemLongClick(new Fragment4Adapter.ItemLongClick() {
            @Override
            public void onLongClick(View view, final int position) {
                //수정 혹은 삭제 커스텀 다이얼로그를 띄움
                CharSequence info[] = new CharSequence[]{"수정", "삭제"};

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("제목");
                builder.setItems(info, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int status = NetworkStatus.getConnectivityStatus(context.getApplicationContext());

                        switch (which) {
                            case 0: // 수정
                                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                                //인터넷 연결 상태 확인
                                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                                    //만일 진행중 피드백 목록 탭을 선택한 경우
                                    if (ONGOING_FEEDBACKLIST.equals("TRUE") && COMPLETE_FEEDBACKLIST.equals("FALSE")) {
                                        Toast.makeText(context.getApplicationContext(), "수정", Toast.LENGTH_SHORT).show();
                                        //게시글 수정 다이얼로그 메소드 호출
                                        //다이얼로그 객체 생성
                                        TitleDialog updateDialog = new TitleDialog(context, "update", videoArrList.get(position).getVideoTitle());
                                        //다이얼로그 레이아웃 지정
                                        updateDialog.setContentView(R.layout.title_dialog);

                                        //다이얼로그를 보여주기 전 리스너 등록
                                        updateDialog.setTitleDialogListener(new TitleDialog.TitleDialogListener() {
                                            //생성을 클릭했을 때 제목과 피드백 제공자의 이름을 가져옴
                                            @Override
                                            public void onClicked(String state, String fileTitle) {
                                                updateItemInRecyclerView(position, fileTitle);
                                            }
                                        });

                                        //다이얼로그 크기 조정
                                        DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();//디바이스 화면 크기 구함
                                        int width = displayMetrics.widthPixels;//디바이스 화면 너비
                                        int height = displayMetrics.heightPixels;//디바이스 화면 높이

                                        WindowManager.LayoutParams wm = updateDialog.getWindow().getAttributes();
                                        wm.width = width;//화면의 가로 넓이
                                        wm.height = height / 3;//화면의 세로 넓이
                                        updateDialog.getWindow().setAttributes(wm);
                                        //게시판 생성 다이얼로그 호출
                                        updateDialog.show();
                                    }
                                    //만일 완료한 피드백 목록을 선택한 경우
                                    else if (ONGOING_FEEDBACKLIST.equals("FALSE") && COMPLETE_FEEDBACKLIST.equals("TRUE")) {
                                        //메세지를 띄움
                                        Toast.makeText(context.getApplicationContext(), "완료한 피드백의 게시글은 수정할 수 없습니다!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                //인터넷이 연결되어 있지 않은 경우
                                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                                else {
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                                    builder.setTitle("인터넷 연결 확인");
                                    builder.setMessage("인터넷이 연결되어있지 않아 게시글을 생성할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    builder.show();
                                }
                                break;
                            case 1:
                                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                                //인터넷 연결 상태 확인
                                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                                    // 삭제
                                    //만일 내 피드백 목록 탭을 선택한 경우
                                    //삭제 확인하는 alert dialog 불러옴
                                    delConfirmAlertDialog(position);
                                }
                                //인터넷이 연결되어 있지 않은 경우
                                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                                else {
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                                    builder.setTitle("인터넷 연결 확인");
                                    builder.setMessage("인터넷이 연결되어있지 않아 게시글을 생성할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    builder.show();
                                }
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        //새 글 추가 버튼을 누르는 경우
        //게시글 추가 버튼 이벤트를 받는 리스너
        fbtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //인터넷 연결 확인하고 연결이 안 되어있으면 삭제 불가능하게 함
                //인터넷 연결 상태 확인
                int status = NetworkStatus.getConnectivityStatus(context.getApplicationContext());
                //모바일 네트워크(LTE) 혹은 무선랜(WIFI)로 연결된 경우
                //이메일과 비밀번호가 일치하는지 확인하고 일치하면 피드백 삭제 다이얼로그를 염
                if (status == NetworkStatus.TYPE_MOBILE | status == NetworkStatus.TYPE_WIFI) {
                    //Activity 호출
                    Intent intent = new Intent(getActivity(), VideoRecordActivity.class);
                    intent.putExtra("loginEmailKey", loginEmailKey);
                    startActivityForResult(intent, VIDEO_RECORD_CODE);

                }
                //인터넷이 연결되어 있지 않은 경우
                //인터넷 연결 설정을 확인해달라는 dialog를 띄움
                else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                    builder.setTitle("인터넷 연결 확인");
                    builder.setMessage("인터넷이 연결되어있지 않아 게시글을 생성할 수 없습니다. 인터넷 연결 설정을 체크해 주십시오.");

                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }

            }
        });
    }

    //recycler view 아이템 삭제 확인하는 dialog 띄우는 메소드
    private void delConfirmAlertDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("게시글 삭제");
        alertDialogBuilder.setMessage("정말 게시글을 삭제하시겠습니까?");

        //아니오를 선택한 경우 alert dialog 끔
        alertDialogBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        //예를 선택한 경우 삭제 메소드 호출
        alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItemInRecyclerView(position);
            }
        });
        alertDialogBuilder.show();//alert dialog 보여줌
    }

    //recycler view에 아이템을 추가하는 메소드
    private void createItemInRecyclerView(String videoTitle, String videoPath, String videoDate) {
        //새로운 VideoDao, VideoDto 객체 생성
        VideoDao videoDao = new VideoDao(getContext());
        VideoDto videoDto = new VideoDto();

        //비디오 길이 값 가져옴
        String videoLength = setVideoLength(videoPath);
        //비트맵 썸네일 생성
        Bitmap thumbnailBitmap = getVideoThumbnailImage(videoPath);
        //만든 비트맵 이미지를 PNG 파일로 저장
        String videoThumnailTitle = SaveBitmapToPNG(videoPath, thumbnailBitmap);

        //날짜를 포함한 문자열을 키 값으로 줌
        String tempVideoKey = loginEmailKey.replace("@", "_");
        String accountVideoKey = tempVideoKey.replace(".", "_");
        String videoKey = "videoKey_" + accountVideoKey + "_" + videoDate;

        //arrayList에 저장할 값을 friendDto에 집어넣음
        videoDto.setVideoItem(videoKey, videoTitle, videoLength, videoPath, videoDate, videoThumnailTitle);

        //shared preference에 영상 정보 추가 메소드 호출
        videoDao.insertVideoInfo(loginEmailKey, feedbackKey, videoDto); //shaed preference에 메모 추가

        //firebase에 영상 추가 메소드 호출
        videoDao.insertOneVideoInfoToFirebase(loginEmailKey, feedbackKey, videoDto);

        //글쓰기 제목과 내용을 넣음
        videoArrList.add(videoDto);

        //데이터 변경을 알림
        fragment4Adapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(context, "게시글이 추가되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 수정하는 메소드
    private void updateItemInRecyclerView(int position, String videoTitle) {
        //새로운 VideoDao, VideoDto 객체 생성
        VideoDao videoDao = new VideoDao(getContext());
        VideoDto videoDto = new VideoDto();

        //게시글 이름 변경
        //arrayList에 저장할 값을 friendDto에 집어넣음
        videoDto.setVideoItem(videoArrList.get(position).getVideoKey(), videoTitle, videoArrList.get(position).getVideoLength(),
                videoArrList.get(position).getVideoPath(), videoArrList.get(position).getVideoDate(),
                videoArrList.get(position).getVideoThumbnailPath());

        //PhotoDao에서 사진 수정 메소드 호출
        videoDao.updateOneVideoInfo(loginEmailKey, feedbackKey, videoDto); //shaed preference에 저장된 피드백 수정

        //firebase에 저장된 정보 수정
        videoDao.updateOneVideoInfoToFirebase(loginEmailKey, feedbackKey, videoArrList.get(position));

        //아이템 수정
        videoArrList.set(position, videoDto);

        //데이터 변경을 알림
        fragment4Adapter.notifyDataSetChanged();

        //아이템이 추가되었다는 메세지 화면에 띄우기
        Toast.makeText(context, "게시글이 수정되었습니다.", Toast.LENGTH_LONG).show();
    }

    //recycler view에 아이템을 삭제하는 메소드
    private void deleteItemInRecyclerView(int position) {
        VideoDao videoDao = new VideoDao(getContext());

        //shared preference에 저장된 정보 삭제
        videoDao.deleteOneVideoInfo(loginEmailKey, feedbackKey, videoArrList.get(position).getVideoKey());

        //firebase에 저장된 정보 삭제
        videoDao.deleteOneVideoInfoToFirebase(loginEmailKey, feedbackKey, videoArrList.get(position));

//        //내 내부 저장소에서 파일 및 영상 삭제
//        String videoFilePath = videoArrList.get(position).getVideoPath(); //영상파일 경로를 가져옴
//        String videoThumbnailPath = videoArrList.get(position).getVideoThumbnailPath(); //영상파일 썸네일 경로를 가져옴
//        File videoFile = new File(videoFilePath);
//        File thumbnailFile = new File(videoThumbnailPath);
//        videoFile.delete();//해당하는 영상파일 삭제
//        thumbnailFile.delete(); //해당하는 영상파일 썸네일 삭제

        videoArrList.remove(position); //배열에서 아이템 삭제

        fragment4Adapter.notifyItemRemoved(position); //아이템 삭제 알림
        fragment4Adapter.notifyItemRangeChanged(position, fragment4Adapter.getItemCount()); //길이 달라졌음을 알림
    }

    //activity에서 값을 받아오는 메소드
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {

        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case VIDEO_RECORD_CODE:
                    String[] getIntentData = intent.getStringArrayExtra("intentData");
                    createItemInRecyclerView(getIntentData[0], getIntentData[1], getIntentData[2]); //게시글 생성 메소드 호출
                    break;
                default:
                    break;
            }
        }
    }

    //비디오 파일 길이를 저장하는 메소드
    private String setVideoLength(String videoPath) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoPath);
        String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time);
        long duration = timeInmillisec / 1000;
        long hours = duration / 3600;
        long minutes = (duration - hours * 3600) / 60;
        long seconds = duration - (hours * 3600 + minutes * 60);
        String videoLength = hours + ":" + minutes + ":" + seconds;

        return videoLength;
    }

    //비디오 썸네일 이미지와 경로를 저장하는 메소드
    private Bitmap getVideoThumbnailImage(String videoFilePath) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoFilePath);

        Bitmap thumbnailBitmap = mediaMetadataRetriever.getFrameAtTime(1000); //1초의 장면을 가져옴

        return thumbnailBitmap;
    }

    //비트맵을 PNG 파일로 저장하고 저장한 파일의 경로를 반환하는 메소드
    private String SaveBitmapToPNG(String videoPath, Bitmap thumbnailBitmap) {

        //.을 기준으로 비디오 제목 문자열을 잘라 확장자를 제거
        int index = videoPath.indexOf("."); //.의 인덱스를 찾는다
        String strResult = videoPath.substring(0, index); //인덱스 앞부분 추출
        String videoThumbnailPath = strResult + ".png";
        File file = new File(videoThumbnailPath);
        OutputStream outputStream = null;

        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file);

            thumbnailBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
        return videoThumbnailPath;
    }
}