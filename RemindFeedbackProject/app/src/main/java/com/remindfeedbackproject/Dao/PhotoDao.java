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
import com.remindfeedbackproject.Dto.PhotoDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhotoDao {
    //DB를 쓰지 않고 초기 설정값이나 간단한 값을 저장을 위해 사용하는 객체
    Context context;

    //firebase 객체 선언
    FirebaseStorage firebaseStorage;

    public PhotoDao(Context context) {
        this.context = context; //액티비티 내부가 아니라 context를 받아와야 함
    }

    ///////////////////////////////////////////////////////////////////////////
    //사진 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertPhotoInfo(String loginEmailKey, String feedbackKey, PhotoDto photoDto) {
        String photoListValue = getPhotoListValue(loginEmailKey, feedbackKey);
        Gson gson = new Gson();

        //사진이 한 개도 없는 경우
        if (photoListValue.equals("NONE")) {
            JSONObject photoObject = new JSONObject(); //사진 정보를 저장할 json object 객체 생성
            JSONArray photoArray = new JSONArray(); //사진 정보 json object를 저장할 json array 객체 생성
            try {
                photoObject.put("photoKey", photoDto.getPhotoKey()); //사진 고유 키 값 저장
                photoObject.put("photoTitle", photoDto.getPhotoTitle()); //사진 제목 저장
                photoObject.put("photoPath", photoDto.getPhotoPath()); //사진 경로 저장
                photoObject.put("photoDate", photoDto.getPhotoDate()); //사진 날짜 저장

                photoArray.put(photoObject); //피드백 정보를 json array로 만들어서 저장
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //변경된 photoInfo 값을 shared preference에 반영하는 쿼리 생성
            FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
            FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
            feedbackDto.setPhotoInfo(photoArray.toString());
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
        }
        //사진이 최소한 한 개 이상 있는 경우
        else {
            //모든 사진 목록을 불러와서 저장
            ArrayList<PhotoDto> photoArrList = readAllPhotoInfo(loginEmailKey, feedbackKey);
            photoArrList.add(photoDto); //arrayList에 새로운 사진 추가

            //수정된 ArrayList를 gson 활용해서 JSON으로 변경
            String jsonObject = gson.toJson(photoArrList);

            //변경된 photoInfo 값을 shared preference에 반영하는 쿼리 생성
            FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
            FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
            feedbackDto.setPhotoInfo(jsonObject);
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 사진의 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOnePhotoInfo(String loginEmailKey, String feedbackKey, PhotoDto updatePhotoDto) {
        String photoListValue = getPhotoListValue(loginEmailKey, feedbackKey);
        ArrayList<PhotoDto> photoArrList = new ArrayList<PhotoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(photoListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 정보를 변경
            for (int i = 0; i < jsonArray.length(); i++) {
                String photoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                PhotoDto photoDto = gson.fromJson(photoInfoLine, PhotoDto.class);
                photoArrList.add(photoDto); //arraylist에 값을 집어넣음

                //내가 선택한 정보의 키 값과 shared preference에 저장된 정보의 키 값이 일치하는 경우
                if (updatePhotoDto.getPhotoKey().equals(photoDto.getPhotoKey())) {
                    photoArrList.set(i, updatePhotoDto); //데이터 수정
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(photoArrList);

        //변경된 photoInfo 값을 shared preference에 반영하는 쿼리 생성
        FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
        feedbackDto.setPhotoInfo(jsonObject);
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 사진을 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOnePhotoInfo(String loginEmailKey, String feedbackKey, String photoKey) {
        String photoListValue = getPhotoListValue(loginEmailKey, feedbackKey);
        ArrayList<PhotoDto> photoArrList = new ArrayList<PhotoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(photoListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 정보를 삭제
            for (int i = 0; i < jsonArray.length(); i++) {
                String photoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                PhotoDto photoDto = gson.fromJson(photoInfoLine, PhotoDto.class);
                photoArrList.add(photoDto); //arraylist에 값을 집어넣음

                //내가 선택한 정보의 키 값과 shared preference에 저장된 정보의 키 값이 일치하는 경우
                if (photoKey.equals(photoDto.getPhotoKey())) {
                    photoArrList.remove(i); //데이터 삭제
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(photoArrList);

        //변경된 photoInfo 값을 shared preference에 반영하는 쿼리 생성
        FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
        feedbackDto.setPhotoInfo(jsonObject);
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //쿼리 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //사진 목록 값을 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public String getPhotoListValue(String loginEmailKey, String feedbackKey) {
        FeedbackDao feedbackDao = new FeedbackDao(context);
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);

        String photoListValue = feedbackDto.getPhotoInfo();

        return photoListValue;
    }

    ///////////////////////////////////////////////////////////////////////////
    //사진 목록에서 모든 사진 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllPhotoInfo(String loginEmailKey, String feedbackKey) {
        String photoListValue = getPhotoListValue(loginEmailKey, feedbackKey);
        ArrayList<PhotoDto> photoArrList = new ArrayList<PhotoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(photoListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String photoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                PhotoDto photoDto = gson.fromJson(photoInfoLine, PhotoDto.class);
                photoArrList.add(photoDto); //arraylist에 값을 집어넣음
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return photoArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 사진 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOnePhotoInfoToFirebase(String loginEmailKey, String feedbackKey, PhotoDto photoDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> photoHashMap = new HashMap<>();
        Map<String, Object> photoValue = photoDto.setPhotoHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        photoHashMap.put("/photoInfo/" + accountUserKey + "/" + feedbackKey + "/" + photoDto.getPhotoKey(), photoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(photoHashMap);

        //firebase storage에 사진 추가
        //일단 사진 path를 uri로 변경
        Uri uri = Uri.fromFile(new File(photoDto.getPhotoPath()));

        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

        StorageReference uploadPhotoIntoFirebase = storageReference.child("photoInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + photoDto.getPhotoKey() + "/" + uri.getLastPathSegment());

        UploadTask uploadTask = uploadPhotoIntoFirebase.putFile(uri);

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
    //[firebase] 사진 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOnePhotoInfoToFirebase(String loginEmailKey, String feedbackKey, PhotoDto updatePhotoDto) {
        //TODO : 인터넷 되는지 확인(firebase 동작 안할 수도 있음)
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> photoHashMap = new HashMap<>();
        Map<String, Object> photoValue = updatePhotoDto.setPhotoHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        photoHashMap.put("/photoInfo/" + accountUserKey + "/" + feedbackKey + "/" + updatePhotoDto.getPhotoKey(), photoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(photoHashMap);

        //firebase storage에 사진 추가
        //일단 사진 path를 uri로 변경
        Uri uri = Uri.fromFile(new File(updatePhotoDto.getPhotoPath()));

        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

        StorageReference uploadPhotoIntoFirebase = storageReference.child("photoInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + updatePhotoDto.getPhotoKey() + "/" + uri.getLastPathSegment());

        UploadTask uploadTask = uploadPhotoIntoFirebase.putFile(uri);

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
    //[firebase] 사진 정보를 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOnePhotoInfoToFirebase(String loginEmailKey, String feedbackKey, PhotoDto photoDto) {
        //TODO : 인터넷 되는지 확인(firebase 동작 안할 수도 있음)
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> photoHashMap = new HashMap<>();
        Map<String, Object> photoValue = null;

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        photoHashMap.put("/photoInfo/" + accountUserKey + "/" + feedbackKey + "/" + photoDto.getPhotoKey(), photoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(photoHashMap);

        //firebase storage에 사진 삭제
        //일단 사진 path를 uri로 변경
        Uri uri = Uri.fromFile(new File(photoDto.getPhotoPath()));

        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

        StorageReference uploadPhotoIntoFirebase = storageReference.child("photoInfo/" + accountUserKey + "/" +
                feedbackKey + "/" + photoDto.getPhotoKey() + "/" + uri.getLastPathSegment());

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

        //로컬 저장소에서 실제 파일 삭제
        String filePath = photoDto.getPhotoPath(); //파일 경로를 가져옴
        File file = new File(filePath);
        file.delete();//해당하는 파일 삭제
    }
}
