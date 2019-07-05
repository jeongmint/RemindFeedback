package com.remindfeedbackproject.Dao;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.remindfeedbackproject.Dto.FeedbackDto;
import com.remindfeedbackproject.Dto.MemberDto;
import com.remindfeedbackproject.Dto.VideoDto;
import com.remindfeedbackproject.Dto.VoiceDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoDao {
    Context context;

    //firebase 객체 선언
    FirebaseStorage firebaseStorage;

    public VideoDao(Context context) {
        this.context = context; //액티비티 내부가 아니라 context를 받아와야 함
    }

    ///////////////////////////////////////////////////////////////////////////
    //녹화 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertVideoInfo(String loginEmailKey, String feedbackKey, VideoDto videoDto) {
        String videoListValue = getVideoListValue(loginEmailKey, feedbackKey);
        Gson gson = new Gson();

        //녹화 한 개도 없는 경우
        if (videoListValue.equals("NONE")) {
            JSONObject videoObject = new JSONObject(); //녹화 정보를 저장할 json object 객체 생성
            JSONArray videoArray = new JSONArray(); //녹화 정보 json object를 저장할 json array 객체 생성
            try {
                videoObject.put("videoKey", videoDto.getVideoKey()); //녹화 고유 키 값 저장
                videoObject.put("videoTitle", videoDto.getVideoTitle()); //녹화 제목 저장
                videoObject.put("videoLength", videoDto.getVideoLength()); //녹화 길이 저장
                videoObject.put("videoPath", videoDto.getVideoPath()); //녹화 경로 저장
                videoObject.put("videoDate", videoDto.getVideoDate()); //녹화 날짜 저장
                videoObject.put("videoThumbnailPath", videoDto.getVideoThumbnailPath()); //녹화 썸네일 경로 저장

                videoArray.put(videoObject); //피드백 정보를 json array로 만들어서 저장
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //변경된 voiceInfo 값을 shared preference에 반영하는 쿼리 생성
            FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
            FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
            feedbackDto.setVideoInfo(videoArray.toString());
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
        }
        //녹화 최소한 한 개 이상 있는 경우
        else {
            //모든 녹화 목록을 불러와서 저장
            ArrayList<VideoDto> videoArrList = readAllVideoInfo(loginEmailKey, feedbackKey);
            videoArrList.add(videoDto); //arrayList에 새로운 녹화 추가

            //수정된 ArrayList를 gson 활용해서 JSON으로 변경
            String jsonObject = gson.toJson(videoArrList);

            //변경된 voiceInfo 값을 shared preference에 반영하는 쿼리 생성
            FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
            FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
            feedbackDto.setVideoInfo(jsonObject);
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 녹화 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneVideoInfo(String loginEmailKey, String feedbackKey, VideoDto updateVideoDto) {
        String videoListValue = getVideoListValue(loginEmailKey, feedbackKey);
        ArrayList<VideoDto> videoArrList = new ArrayList<VideoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(videoListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 정보를 변경
            for (int i = 0; i < jsonArray.length(); i++) {
                String videoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                VideoDto videoDto = gson.fromJson(videoInfoLine, VideoDto.class);
                videoArrList.add(videoDto); //arraylist에 값을 집어넣음

                //내가 선택한 정보의 키 값과 shared preference에 저장된 정보의 키 값이 일치하는 경우
                if (updateVideoDto.getVideoKey().equals(videoDto.getVideoKey())) {
                    videoArrList.set(i, updateVideoDto); //데이터 수정
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(videoArrList);

        //변경된 voiceInfo 값을 shared preference에 반영하는 쿼리 생성
        FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
        feedbackDto.setVideoInfo(jsonObject);
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 녹화 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneVideoInfo(String loginEmailKey, String feedbackKey, String photoKey) {
        String videoListValue = getVideoListValue(loginEmailKey, feedbackKey);
        ArrayList<VideoDto> videoArrList = new ArrayList<VideoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(videoListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 정보를 삭제
            for (int i = 0; i < jsonArray.length(); i++) {
                String videoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                VideoDto videoDto = gson.fromJson(videoInfoLine, VideoDto.class);
                videoArrList.add(videoDto); //arraylist에 값을 집어넣음

                //내가 선택한 정보의 키 값과 shared preference에 저장된 정보의 키 값이 일치하는 경우
                if (photoKey.equals(videoDto.getVideoKey())) {
                    videoArrList.remove(i); //데이터 삭제

                    String videoFilePath = videoDto.getVideoPath(); //영상파일 경로를 가져옴
                    String videoThumbnailPath = videoDto.getVideoThumbnailPath(); //영상파일 썸네일 경로를 가져옴
                    File videoFile = new File(videoFilePath);
                    File thumbnailFile = new File(videoThumbnailPath);
                    videoFile.delete();//해당하는 영상파일 삭제
                    thumbnailFile.delete(); //해당하는 영상파일 썸네일 삭제
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(videoArrList);

        //변경된 voiceInfo 값을 shared preference에 반영하는 쿼리 생성
        FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
        feedbackDto.setVideoInfo(jsonObject);
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //녹화 목록 크기를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public String getVideoListValue(String loginEmailKey, String feedbackKey) {
        FeedbackDao feedbackDao = new FeedbackDao(context);
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);

        String videoListValue = feedbackDto.getVideoInfo();

        return videoListValue;
    }

    ///////////////////////////////////////////////////////////////////////////
    //녹화 목록에서 모든 녹화 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllVideoInfo(String loginEmailKey, String feedbackKey) {
        String videoListValue = getVideoListValue(loginEmailKey, feedbackKey);
        ArrayList<VideoDto> videoArrList = new ArrayList<VideoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(videoListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String videoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                VideoDto videoDto = gson.fromJson(videoInfoLine, VideoDto.class);
                videoArrList.add(videoDto); //arraylist에 값을 집어넣음
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return videoArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 녹화 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneVideoInfoToFirebase(String loginEmailKey, String feedbackKey, VideoDto videoDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> videoHashMap = new HashMap<>();
        Map<String, Object> videoValue = videoDto.setVideoHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        videoHashMap.put("/videoInfo/" + accountUserKey + "/" + feedbackKey + "/" + videoDto.getVideoKey(), videoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(videoHashMap);

        //firebase storage에 사진 추가
        //일단 영상 path를 uri로 변경
        Uri videoUri = Uri.fromFile(new File(videoDto.getVideoPath()));

        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

        StorageReference uploadVideoIntoFirebase = storageReference.child("videoInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + videoDto.getVideoKey() + "/" + videoUri.getLastPathSegment());

        UploadTask uploadTaskOne = uploadVideoIntoFirebase.putFile(videoUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTaskOne.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            }
        });

        //썸네일도 업로드
        Uri thumbnailUri = Uri.fromFile(new File(videoDto.getVideoThumbnailPath()));

        StorageReference uploadThumbnailIntoFirebase = storageReference.child("videoInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + videoDto.getVideoKey() + "/" + thumbnailUri.getLastPathSegment());

        UploadTask uploadTaskTwo = uploadThumbnailIntoFirebase.putFile(thumbnailUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTaskTwo.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 영상 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneVideoInfoToFirebase(String loginEmailKey, String feedbackKey, VideoDto updateVideoDto) {
        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> videoHashMap = new HashMap<>();
        Map<String, Object> videoValue = updateVideoDto.setVideoHashMap();

        videoHashMap.put("/videoInfo/" + accountUserKey + "/" + feedbackKey + "/" + updateVideoDto.getVideoKey(), videoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(videoHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 영상 정보를 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneVideoInfoToFirebase(String loginEmailKey, String feedbackKey, VideoDto videoDto) {
        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> videoHashMap = new HashMap<>();
        Map<String, Object> videoValue = null;

        videoHashMap.put("/videoInfo/" + accountUserKey + "/" + feedbackKey + "/" + videoDto.getVideoKey(), videoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(videoHashMap);

        //firebase storage에 녹화 삭제
        //일단 사진 path를 uri로 변경
        Uri uri = Uri.fromFile(new File(videoDto.getVideoPath()));

        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

        StorageReference uploadPhotoIntoFirebase = storageReference.child("videoInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + videoDto.getVideoKey() + "/" + uri.getLastPathSegment());

        Task uploadTask = uploadPhotoIntoFirebase.delete();

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            }
        });

        //썸네일도 업로드
        Uri thumbnailUri = Uri.fromFile(new File(videoDto.getVideoThumbnailPath()));

        StorageReference uploadThumbnailIntoFirebase = storageReference.child("videoInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + videoDto.getVideoKey() + "/" + thumbnailUri.getLastPathSegment());

        Task uploadTaskTwo = uploadThumbnailIntoFirebase.delete();

        // Register observers to listen for when the download is done or if it fails
        uploadTaskTwo.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
            }
        });

        //내 내부 저장소에서 파일 및 영상 삭제
        String videoFilePath = videoDto.getVideoPath(); //영상파일 경로를 가져옴
        String videoThumbnailPath = videoDto.getVideoThumbnailPath(); //영상파일 썸네일 경로를 가져옴
        File videoFile = new File(videoFilePath);
        File thumbnailFile = new File(videoThumbnailPath);
        videoFile.delete();//해당하는 영상파일 삭제
        thumbnailFile.delete(); //해당하는 영상파일 썸네일 삭제
    }
}
