package com.remindfeedbackproject.Dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.*;
import com.remindfeedbackproject.Dto.FeedbackDto;
import com.remindfeedbackproject.Dto.FolderDto;
import com.remindfeedbackproject.Dto.FriendDto;
import com.remindfeedbackproject.Dto.MemberDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemberDao {
    //DB를 쓰지 않고 초기 설정값이나 간단한 값을 저장을 위해 사용하는 객체
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;
    Context context;

    //firebase 객체 선언
    FirebaseStorage firebaseStorage;

    //shared preference 파일 이름
    private static final String PREF_NAME = "RemindFeedbackInfo";

    public MemberDao(Context context) {
        this.context = context; //액티비티 내부가 아니라 context를 받아와야 함

        sharedPreferences = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);//회원 정보 shared preference 파일 생성
        sharedPreferencesEditor = sharedPreferences.edit();//값을 읽어서 저장하기 위해 editor 객체 생성
    }

    ///////////////////////////////////////////////////////////////////////////
    //[shared preference] 회원 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneMemberInfo(MemberDto memberDto) {
        // 회원의 이메일, 이름(별명), 비밀번호, 가입일자, 프로필 사진 경로, 프로필 상태 메세지
        // 회원의 데이터 파일 저장 경로, 회원의 인트로 스킵 정보, 친구 정보, 피드백 정보를 저장
        //insert memberEmail(Not duplicated), memberNickName, memberPassword, memberRegisterDate, memberProfilePhotoPath, memberProfileMessage
        //insert memberDataPathInfo, memberIntroSkip, FriendInfo, FeedbackInfo

        //객체 선언
        String memberListValue = getMemberListValue(); //키값에 따라 가져온 value 저장 객체
        ArrayList<MemberDto> memberArrList = new ArrayList<MemberDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        //회원정보가 하나도 없는 경우
        if (memberListValue.equals("NONE")) {
            JSONObject memberObject = new JSONObject();//회원 정보와 프로필 정보를 저장할 jsonObject객체 생성
            JSONArray memberArray = new JSONArray(); //json object를 저장할 json array 객체 생성
            try {
                memberObject.put("memberEmail", memberDto.getMemberEmail()); //회원 이메일 저장
                memberObject.put("memberNickName", memberDto.getMemberNickName()); //회원 이름(별명) 저장
                memberObject.put("memberPassword", memberDto.getMemberPassword()); //회원 비밀번호 저장
                memberObject.put("memberRegisterDate", memberDto.getMemberRegisterDate()); //회원 가입일자 저장
                memberObject.put("memberProfilePhotoPath", memberDto.getMemberProfilePhotoPath()); //프로필 사진 경로 저장
                memberObject.put("memberProfileMessage", memberDto.getMemberProfileMessage()); //프로필 상태 메세지 저장
                memberObject.put("memberIntroSkip", memberDto.getMemberIntroSkip()); //회원의 인트로 스킵 정보 저장
                memberObject.put("memberAdviserInfo", memberDto.getMemberAdviserInfo()); //회원의 조언 정보 저장
                memberObject.put("memberDataPathInfo", memberDto.getMemberDataPathInfo()); //회원의 데이터 파일 경로 저장
                memberObject.put("memberFriendInfo", memberDto.getMemberFriendInfo()); //회원의 친구 정보 저장
                memberObject.put("memberFeedbackInfo", memberDto.getMemberFeedbackInfo()); //회원의 피드백 정보 저장

                memberArray.put(memberObject); //회원 정보를 json array로 만들어서 저장

                System.out.println("회원가입 쿼리를 수행한 후 ArrayList는 " + memberArray.toString() + "입니다.");

                //변경된 memberInfo 값을 shared preference에 반영하는 쿼리 생성
                sharedPreferencesEditor.putString("memberInfo", memberArray.toString());
                sharedPreferencesEditor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //회원정보가 최소한 하나 이상 있는 경우
        else {
            //모든 회원정보를 불러와서 memberArrList에 저장
            memberArrList = readAllMemberInfo();
            memberArrList.add(memberDto); //arrayList에 새로운 회원 추가

            //수정된 ArrayList를 gson 활용해서 JSON으로 변경
            String jsonObjectValue = gson.toJson(memberArrList);

            //변경된 memberInfo 값을 shared preference에 반영하는 쿼리 생성
            sharedPreferencesEditor.putString("memberInfo", jsonObjectValue);
            sharedPreferencesEditor.commit();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //회원 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneMemberInfo(MemberDto updateMemberDto) {
        // 회원의 이메일, 이름(별명), 비밀번호, 가입일자, 프로필 사진 경로, 프로필 상태 메세지
        // 회원의 데이터 파일 저장 경로, 회원의 인트로 스킵 정보, 친구 정보, 피드백 정보를 저장
        //insert memberEmail(Not duplicated), memberNickName, memberPassword, memberRegisterDate, memberProfilePhotoPath, memberProfileMessage
        //insert memberDataPathInfo, memberIntroSkip, FriendInfo, FeedbackInfo

        //객체 선언
        String memberListValue = getMemberListValue(); //키값에 따라 가져온 value 저장 객체
        ArrayList<MemberDto> memberArrList = new ArrayList<MemberDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson(); //Gson 객체

        try {
            JSONArray jsonArray = new JSONArray(memberListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 Dto의 정보 변경
            for (int i = 0; i < jsonArray.length(); i++) {
                String memberInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                MemberDto memberDto = gson.fromJson(memberInfoLine, MemberDto.class);
                memberArrList.add(memberDto); //arraylist에 값을 집어넣음

                //내가 선택한 회원의 이메일 값과 shared preference에 저장된 이메일 정보가 일치하는 경우
                if(updateMemberDto.getMemberEmail().equals(memberDto.getMemberEmail())){
                    memberArrList.set(i, updateMemberDto); //데이터 수정
                    System.out.println("getMemberEmail : " + memberArrList.get(i).getMemberEmail());
                    System.out.println("getMemberNickName : " + memberArrList.get(i).getMemberNickName());
                    System.out.println("getMemberPassword : " + memberArrList.get(i).getMemberPassword());
                    System.out.println("getMemberRegisterDate : " + memberArrList.get(i).getMemberRegisterDate());
                    System.out.println("getMemberProfilePhotoPath : " + memberArrList.get(i).getMemberProfilePhotoPath());
                    System.out.println("getMemberProfileMessage : " + memberArrList.get(i).getMemberProfileMessage());
                    System.out.println("getMemberIntroSkip : " + memberArrList.get(i).getMemberIntroSkip());
                    System.out.println("getMemberAdviserInfo : " + memberArrList.get(i).getMemberAdviserInfo());
                    System.out.println("getMemberDataPathInfo : " + memberArrList.get(i).getMemberDataPathInfo());
                    System.out.println("getMemberFriendInfo : " + memberArrList.get(i).getMemberFriendInfo());
                    System.out.println("getMemberFeedbackInfo : " + memberArrList.get(i).getMemberFeedbackInfo());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObjectValue = gson.toJson(memberArrList);

        //변경된 memberInfo 값을 shared preference에 반영하는 쿼리 생성
        sharedPreferencesEditor.putString("memberInfo", jsonObjectValue);
        sharedPreferencesEditor.commit();
    }

    ///////////////////////////////////////////////////////////////////////////
    //회원 탈퇴 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneMemberInfo(String loginEmailKey) {
        //객체 선언
        String memberListValue = getMemberListValue(); //키값에 따라 가져온 value 저장 객체
        ArrayList<MemberDto> memberArrList = new ArrayList<MemberDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson(); //Gson 객체

        try {
            JSONArray jsonArray = new JSONArray(memberListValue);

            //반복문을 돌려 저장된 피드백 정보 중 선택한 피드백을 찾고 해당 FeedbackDto의 정보 변경
            for (int i = 0; i < jsonArray.length(); i++) {
                String memberInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                MemberDto memberDto = gson.fromJson(memberInfoLine, MemberDto.class);
                memberArrList.add(memberDto); //arraylist에 값을 집어넣음

                //내가 선택한 회원의 이메일 값과 shared preference에 저장된 이메일 정보가 일치하는 경우
                if(memberDto.getMemberEmail().equals(loginEmailKey)){
                    //폴더에 저장된 데이터삭제
                    FolderDto folderDto = readMemberDataPathInfo(loginEmailKey); //회원의 파일이 저장된 경로를 불러와서 폴더 삭제
                    deleteFolder(folderDto.getAccountDirectoryPath()); //폴더를 포함해서 하위 폴더를 전부 삭제하는 재귀 메소드 호출

                    memberArrList.remove(i); //해당 회원 정보 삭제
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //삭제한 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObjectValue = gson.toJson(memberArrList);

        //변경된 memberInfo 값을 shared preference에 반영하는 쿼리 생성
        sharedPreferencesEditor.putString("memberInfo", jsonObjectValue);
        sharedPreferencesEditor.commit();
    }

    ///////////////////////////////////////////////////////////////////////////
    //회원 이메일 중복 확인하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public boolean EmailDuplicationConfirm(String inputEmailkey) {
        boolean emailDuplicationFlag = false; //이메일 중복 확인 플래그

        //단 한 명의 회원도 가입하지 않은 경우
        if(getMemberListValue().equals("NONE")){
            emailDuplicationFlag = false; //그대로 false 값 집어넣음
        }
        //한 명의 회원이라도 가입한 경우
        else {
            //한 명의 회원 정보를 가져옴
            MemberDto memberDto = readOneMemberInfo(inputEmailkey);

            //입력한 값과 저장된 값이 일치하는 경우
            if (inputEmailkey.equals(memberDto.getMemberEmail())) {
                //이메일 중복이므로 flag값 true로 변경
                emailDuplicationFlag = true;
            }
        }

        //Flag 값을 반환
        //중복인 경우 true, 중복이 아닌 경우 false
        return emailDuplicationFlag;
    }

    ///////////////////////////////////////////////////////////////////////////
    //이메일과 비밀번호를 입력받아 일치 여부를 확인하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public int loginConfirm(String inputEmailKey, String inputPasswordKey) {
        //플래그 선언
        int loginFlag = -1; //로그인 확인 플래그
        boolean correctEmail = false; //이메일 일치 확인 플래그
        boolean correctPassword = false; //비밀번호 일치 확인 플래그

        //객체 선언
        String memberListValue = getMemberListValue(); //키값에 따라 가져온 value 저장 객체
        ArrayList<MemberDto> memberArrList = new ArrayList<MemberDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson(); //Gson 객체

        try {
            JSONArray jsonArray = new JSONArray(memberListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String memberInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                MemberDto memberDto = gson.fromJson(memberInfoLine, MemberDto.class);
                memberArrList.add(memberDto); //arraylist에 값을 집어넣음

                //공백이 아닌 경우 사용자가 입력한 이메일과 DB의 이메일 값을 비교
                if (inputEmailKey != null && inputPasswordKey != null) {
                    //사용자가 입력한 이메일과 DB의 이메일이 일치하는 경우
                    if (inputEmailKey.equals(memberArrList.get(i).getMemberEmail())) {
                        correctEmail = true; //이메일 일치 플래그를 true로 바꿈
                    }
                    //사용자가 입력한 비밀번호와 DB의 비밀번호가 일치하는 경우
                    if (inputPasswordKey.equals(memberArrList.get(i).getMemberPassword())) {
                        correctPassword = true; //비밀번호 일치 플래그를 true로 바꿈
                    }

                    //둘 중 하나의 일치 플래그가 true인 경우
                    if (correctEmail == true | correctPassword == true) {
                        break; //반복문 종료
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //이메일과 비밀번호가 둘 다 일치하는 경우
        if (correctEmail == true && correctPassword == true) {
            loginFlag = 2;
        }
        //이메일은 일치하는데 비밀번호가 불일치하는 경우
        else if (correctEmail == true && correctPassword == false) {
            loginFlag = 1;
        }
        //그 외의 경우(이메일이 불일치하는 경우)
        else {
            loginFlag = 0;
        }

        //이메일과 비밀번호 일치 여부에 따른 int 값을 반환
        //이메일 불일치 : 0, 이메일 일치 비밀번호 불일치 : 1, 이메일 일치, 비밀번호 일치 : 2
        return loginFlag;
    }

    ///////////////////////////////////////////////////////////////////////////
    //회원 목록(memberInfo) 값을 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public String getMemberListValue() {
        //문자열로 가져온 memberInfo 값을 JSONObject로 변환
        String memberListValue = sharedPreferences.getString("memberInfo", "NONE");

        return memberListValue;
    }

    ///////////////////////////////////////////////////////////////////////////
    //한 명의 회원 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public MemberDto readOneMemberInfo(String loginEmailKey) {
        //JSONObject에서 memberInfo 항목만 꺼내와서 String으로 저장
        String memberListValue = getMemberListValue();
        MemberDto memberDto = new MemberDto();
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(memberListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String memberInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴

                //JSON object를 파싱해서 MemberDto에 넣음
                memberDto = gson.fromJson(memberInfoLine, MemberDto.class);

                //찾고자 하는 이메일 값과 shared preference에 저장된 이메일 값이 일치하는 경우
                if(memberDto.getMemberEmail().equals(loginEmailKey)){
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return memberDto;
    }

    ///////////////////////////////////////////////////////////////////////////
    //회원 목록에서 모든 회원 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllMemberInfo() {
        //객체 선언
        String memberListValue = getMemberListValue(); //키값에 따라 가져온 value 저장 객체
        ArrayList<MemberDto> memberArrList = new ArrayList<MemberDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson(); //Gson 객체

        try {
            JSONArray jsonArray = new JSONArray(memberListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String memberInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴

                //JSON object를 파싱해서 ArrayList에 넣음
                MemberDto memberDto = gson.fromJson(memberInfoLine, MemberDto.class);
                memberArrList.add(memberDto); //arraylist에 값을 집어넣음
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return memberArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //로그인한 회원의 파일 경로 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public FolderDto readMemberDataPathInfo(String loginEmailKey) {
        //로그인 한 회원 정보를 가져옴
        MemberDto memberDto = readOneMemberInfo(loginEmailKey);

        //JSONObject에서 memberDataPathInfo 항목만 꺼내와서 String으로 저장
        String memberDataPathInfoValue = memberDto.getMemberDataPathInfo();
        Gson gson = new Gson();
        FolderDto folderDto = new FolderDto();
        try {
            JSONObject jsonObject = new JSONObject(memberDataPathInfoValue);
            //JSON object를 파싱해서 ArrayList에 넣음
            folderDto = gson.fromJson(memberDataPathInfoValue, FolderDto.class);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return folderDto;
    }

    ///////////////////////////////////////////////////////////////////////////
    //폴더를 비롯해서 하위 폴더 및 파일을 전부 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public static void deleteFolder(String path) {

        File folder = new File(path);
        try {
            if (folder.exists()) {
                File[] folder_list = folder.listFiles(); //파일리스트 얻어오기

                for (int i = 0; i < folder_list.length; i++) {
                    if (folder_list[i].isFile()) {
                        folder_list[i].delete();
                        System.out.println("파일이 삭제되었습니다.");
                    } else {
                        deleteFolder(folder_list[i].getPath()); //재귀함수호출
                        System.out.println("폴더가 삭제되었습니다.");
                    }
                    folder_list[i].delete();
                }
                folder.delete(); //폴더 삭제
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 회원 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneMemberInfoToFirebase(MemberDto memberDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        //회원 고유 키 생성
        String tempUserKey = memberDto.getMemberEmail().replace("@", "_");
        String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        Map<String, Object> memberHashMap = new HashMap<>();
        Map<String, Object> memberValue = memberDto.setMemberHashMap();
        memberHashMap.put("/memberInfo/" + accountMemberKey, memberValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(memberHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 한 명의 회원 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneMemberInfoToFirebase(MemberDto memberDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        //회원 고유 키 생성
        String tempUserKey = memberDto.getMemberEmail().replace("@", "_");
        String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        Map<String, Object> memberHashMap = new HashMap<>();
        Map<String, Object> memberValue = memberDto.setMemberHashMap();
        memberHashMap.put("/memberInfo/" + accountMemberKey, memberValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(memberHashMap);

        //해당 경로에 파일이 있는지 확인
        File profilePhoto = new File(memberDto.getMemberProfilePhotoPath());
        //파일이 존재하는 경우
        if(profilePhoto.exists()){
            //firebase storage에 사진 추가
            //일단 프로필 사진 path를 uri로 변경
            Uri uri = Uri.fromFile(new File(memberDto.getMemberProfilePhotoPath()));

            firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

            StorageReference uploadPhotoIntoFirebase = storageReference.child("profileInfo/" + accountMemberKey + "/" + uri.getLastPathSegment());

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
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 회원 정보를 삭제하는 메소드(회원 탈퇴)
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneMemberInfoToFirebase(MemberDto memberDto) {
        //회원 고유 키 생성
        String tempUserKey = memberDto.getMemberEmail().replace("@", "_");
        String accountMemberKey = "MemberKey_" + tempUserKey.replace(".", "_");

        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        //일단 회원 프로필 있으면 프로필부터 삭제
        if(!memberDto.getMemberProfilePhotoPath().equals("NONE")){
            //firebase storage에 사진 추가
            //일단 프로필 사진 path를 uri로 변경
            Uri uri = Uri.fromFile(new File(memberDto.getMemberProfilePhotoPath()));

            firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://remindfeedback.appspot.com");

            StorageReference uploadPhotoIntoFirebase = storageReference.child("profileInfo/" + accountMemberKey + "/" + uri.getLastPathSegment());

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
        }

        //피드백 있으면 모든 피드백 정보 가져와서 삭제
        //만일 회원정보 안에 피드백이 있는 경우
        if(!memberDto.getMemberFeedbackInfo().equals("NONE")|!memberDto.getMemberFeedbackInfo().equals("")) {
            //피드백 안의 모든 메모 정보를 가져옴
            FeedbackDao feedbackDao = new FeedbackDao(context);
            ArrayList<FeedbackDto> feedbackArrList = feedbackDao.readAllFeedbackInfo(memberDto.getMemberEmail());

            //모든 피드백 정보를 shared preference와 firebase database에서 삭제
            for(int i=0; i<feedbackArrList.size(); i++){
                feedbackDao.deleteOneFeedbackInfoToFirebase(memberDto.getMemberEmail(), feedbackArrList.get(i));
            }
        }

        //memberInfo 삭제
        Map<String, Object> memberHashMap = new HashMap<>();
        Map<String, Object> reviseMemberValue = memberDto.setMemberDeleteHashMap();
        memberHashMap.put("/memberInfo/" + accountMemberKey, reviseMemberValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(memberHashMap);

        //friendInfo 삭제
        Map<String, Object> memberValue = null;
        memberHashMap.put("/friendInfo/" + accountMemberKey, memberValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(memberHashMap);

//        //feedbackInfo 삭제
//        memberHashMap.put("/feedbackInfo/" + accountMemberKey, memberValue);
//
//        //update children 쿼리를 사용해서 firebase에 정보 업데이트
//        databaseReference.updateChildren(memberHashMap);
//
//        //memoInfo 삭제
//        memberHashMap.put("/memoInfo/" + accountMemberKey, memberValue);
//
//        //update children 쿼리를 사용해서 firebase에 정보 업데이트
//        databaseReference.updateChildren(memberHashMap);
//
//        //photoInfo 삭제
//        memberHashMap.put("/photoInfo/" + accountMemberKey, memberValue);
//
//        //update children 쿼리를 사용해서 firebase에 정보 업데이트
//        databaseReference.updateChildren(memberHashMap);
//
//        //voiceInfo 삭제
//        memberHashMap.put("/voiceInfo/" + accountMemberKey, memberValue);
//
//        //update children 쿼리를 사용해서 firebase에 정보 업데이트
//        databaseReference.updateChildren(memberHashMap);
//
//        //videoInfo 삭제
//        memberHashMap.put("/videoInfo/" + accountMemberKey, memberValue);
//
//        //update children 쿼리를 사용해서 firebase에 정보 업데이트
//        databaseReference.updateChildren(memberHashMap);
    }
}