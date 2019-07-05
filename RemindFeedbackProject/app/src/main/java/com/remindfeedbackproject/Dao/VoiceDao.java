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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.remindfeedbackproject.Dto.FeedbackDto;
import com.remindfeedbackproject.Dto.MemberDto;
import com.remindfeedbackproject.Dto.PhotoDto;
import com.remindfeedbackproject.Dto.VoiceDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VoiceDao {
    Context context;

    //firebase 객체 선언
    FirebaseStorage firebaseStorage;

    public VoiceDao(Context context) {
        this.context = context; //액티비티 내부가 아니라 context를 받아와야 함
    }

    ///////////////////////////////////////////////////////////////////////////
    //녹음 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertVoiceInfo(String loginEmailKey, String feedbackKey, VoiceDto voiceDto) {
        String voiceListValue = getVoiceListValue(loginEmailKey, feedbackKey);
        Gson gson = new Gson();

        //녹음 한 개도 없는 경우
        if (voiceListValue.equals("NONE")) {
            JSONObject voiceObject = new JSONObject(); //녹음 정보를 저장할 json object 객체 생성
            JSONArray voiceArray = new JSONArray(); //녹음 정보 json object를 저장할 json array 객체 생성
            try {
                voiceObject.put("voiceKey", voiceDto.getVoiceKey()); //녹음 고유 키 값 저장
                voiceObject.put("voiceTitle", voiceDto.getVoiceTitle()); //녹음 제목 저장
                voiceObject.put("voicePath", voiceDto.getVoicePath()); //녹음 경로 저장
                voiceObject.put("voiceLength", voiceDto.getVoiceLength()); //녹음 길이 저장
                voiceObject.put("voiceDate", voiceDto.getVoiceDate()); //녹음 날짜 저장

                voiceArray.put(voiceObject); //피드백 정보를 json array로 만들어서 저장
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //변경된 voiceInfo 값을 shared preference에 반영하는 쿼리 생성
            FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
            FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
            feedbackDto.setVoiceInfo(voiceArray.toString());
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
        }
        //녹음 최소한 한 개 이상 있는 경우
        else {
            //모든 녹음 목록을 불러와서 저장
            ArrayList<VoiceDto> voiceArrList = readAllVoiceInfo(loginEmailKey, feedbackKey);
            voiceArrList.add(voiceDto); //arrayList에 새로운 녹음 추가

            //수정된 ArrayList를 gson 활용해서 JSON으로 변경
            String jsonObject = gson.toJson(voiceArrList);

            //변경된 voiceInfo 값을 shared preference에 반영하는 쿼리 생성
            FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
            FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
            feedbackDto.setVoiceInfo(jsonObject);
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 녹음 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneVoiceInfo(String loginEmailKey, String feedbackKey, VoiceDto updateVoiceDto) {
        String voiceListValue = getVoiceListValue(loginEmailKey, feedbackKey);
        ArrayList<VoiceDto> voiceArrList = new ArrayList<VoiceDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(voiceListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 정보를 변경
            for (int i = 0; i < jsonArray.length(); i++) {
                String voiceInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                VoiceDto voiceDto = gson.fromJson(voiceInfoLine, VoiceDto.class);
                voiceArrList.add(voiceDto); //arraylist에 값을 집어넣음

                //내가 선택한 정보의 키 값과 shared preference에 저장된 정보의 키 값이 일치하는 경우
                if (updateVoiceDto.getVoiceKey().equals(voiceDto.getVoiceKey())) {
                    voiceArrList.set(i, updateVoiceDto); //데이터 수정
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(voiceArrList);

        //변경된 voiceInfo 값을 shared preference에 반영하는 쿼리 생성
        FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
        feedbackDto.setVoiceInfo(jsonObject);
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 녹음 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneVoiceInfo(String loginEmailKey, String feedbackKey, String voiceKey) {
        String voiceListValue = getVoiceListValue(loginEmailKey, feedbackKey);
        ArrayList<VoiceDto> voiceArrList = new ArrayList<VoiceDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(voiceListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 정보를 삭제
            for (int i = 0; i < jsonArray.length(); i++) {
                String voiceInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                VoiceDto voiceDto = gson.fromJson(voiceInfoLine, VoiceDto.class);
                voiceArrList.add(voiceDto); //arraylist에 값을 집어넣음

                //내가 선택한 정보의 키 값과 shared preference에 저장된 정보의 키 값이 일치하는 경우
                if (voiceKey.equals(voiceDto.getVoiceKey())) {
                    voiceArrList.remove(i); //데이터 삭제

                    String filePath = voiceDto.getVoicePath(); //파일 경로를 가져옴
                    File file = new File(filePath);
                    file.delete();//해당하는 파일 삭제
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(voiceArrList);

        //변경된 voiceInfo 값을 shared preference에 반영하는 쿼리 생성
        FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
        feedbackDto.setVoiceInfo(jsonObject);
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //녹음 목록 크기를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public String getVoiceListValue(String loginEmailKey, String feedbackKey) {
        FeedbackDao feedbackDao = new FeedbackDao(context);
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);

        String voiceListValue = feedbackDto.getVoiceInfo();

        return voiceListValue;
    }

    ///////////////////////////////////////////////////////////////////////////
    //녹음 목록에서 모든 녹음 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllVoiceInfo(String loginEmailKey, String feedbackKey) {
        String voiceListValue = getVoiceListValue(loginEmailKey, feedbackKey);

        ArrayList<VoiceDto> voiceArrList = new ArrayList<VoiceDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(voiceListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String voiceInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                VoiceDto voiceDto = gson.fromJson(voiceInfoLine, VoiceDto.class);
                voiceArrList.add(voiceDto); //arraylist에 값을 집어넣음
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return voiceArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 녹음 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneRecordInfoToFirebase(String loginEmailKey, String feedbackKey, VoiceDto voiceDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> voiceHashMap = new HashMap<>();
        Map<String, Object> voiceValue = voiceDto.setVoiceHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        voiceHashMap.put("/voiceInfo/" + accountUserKey + "/" + feedbackKey + "/" + voiceDto.getVoiceKey(), voiceValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(voiceHashMap);

        //firebase storage에 사진 추가
        //일단 사진 path를 uri로 변경
        Uri uri = Uri.fromFile(new File(voiceDto.getVoicePath()));

        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

        StorageReference uploadVoiceIntoFirebase = storageReference.child("voiceInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + voiceDto.getVoiceKey() + "/" + uri.getLastPathSegment());

        //mp3 파일의 메타 데이터 설정
        StorageMetadata storageMetadata = new StorageMetadata.Builder().
                setContentType("audio/mp3").build();

        UploadTask uploadTask = uploadVoiceIntoFirebase.putFile(uri);

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
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 녹음 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneVoiceInfoToFirebase(String loginEmailKey, String feedbackKey, VoiceDto updateVoiceDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> voiceHashMap = new HashMap<>();
        Map<String, Object> voiceValue = updateVoiceDto.setVoiceHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        voiceHashMap.put("/voiceInfo/" + accountUserKey + "/" + feedbackKey + "/" + updateVoiceDto.getVoiceKey(), voiceValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(voiceHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 녹음 정보를 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneVoiceInfoToFirebase(String loginEmailKey, String feedbackKey, VoiceDto voiceDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> voiceHashMap = new HashMap<>();
        Map<String, Object> voiceValue = null;

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        voiceHashMap.put("/voiceInfo/" + accountUserKey + "/" + feedbackKey + "/" + voiceDto.getVoiceKey(), voiceValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(voiceHashMap);

        //firebase storage에 음성 삭제
        //일단 사진 path를 uri로 변경
        Uri uri = Uri.fromFile(new File(voiceDto.getVoicePath()));

        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

        StorageReference uploadPhotoIntoFirebase = storageReference.child("voiceInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + voiceDto.getVoiceKey() + "/" + uri.getLastPathSegment());

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

        //로컬 저장소에서 음성 파일 삭제
        String filePath = voiceDto.getVoicePath(); //파일 경로를 가져옴
        File file = new File(filePath);
        file.delete();//해당하는 파일 삭제
    }
}
