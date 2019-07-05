package com.remindfeedbackproject.Dao;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.remindfeedbackproject.Dto.FriendDto;
import com.remindfeedbackproject.Dto.MemberDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendDao {
    Context context;

    public FriendDao(Context context) {
        this.context = context; //액티비티 내부가 아니라 context를 받아와야 함
    }

    ///////////////////////////////////////////////////////////////////////////
    //[shared preference] 한 명의 친구 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneFriendInfo(String loginEmailKey, FriendDto friendDto) {
        //친구의 이메일, 이름(별명), 프로필 사진 경로, 프로필 상태 메세지를 저장
        //insert memberEmail(Not duplicated), memberNickName, memberProfilePhotoPath, memberProfileMessage
        try {
            //객체 선언
            String friendListValue = getFriendListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
            ArrayList<FriendDto> friendArrList = new ArrayList<FriendDto>(); //arrayList 객체 생성
            Gson gson = new Gson(); //Gson 객체 생성

            //친구가 한 명도 없는 경우 친구목록을 불러오지 않고 저장
            //친구가 한 명도 없는 경우
            if (friendListValue.equals("NONE") | friendListValue.equals("")) {
                JSONObject friendObject = new JSONObject(); //친구 정보를 저장할 json object 객체 생성
                JSONArray friendArray = new JSONArray(); //친구 정보 json object를 저장할 json array 객체 생성

                friendObject.put("friendKey", friendDto.getFriendKey()); //친구의 고유 키 저장
                friendObject.put("friendEmail", friendDto.getFriendEmail()); //친구의 이메일 저장
                friendObject.put("friendNickName", friendDto.getFriendNickName()); //친구의 닉네임 저장
                friendObject.put("friendProfilePhotoPath", friendDto.getFriendProfilePhotoPath()); //친구의 프로필 사진 경로 저장
                friendObject.put("friendProfileMessage", friendDto.getFriendProfileMessage()); //친구의 프로필 상태 메세지 저장
                friendObject.put("friendRelationship", friendDto.getFriendRelationship()); //친구와 나의 관계 정보 저장

                friendArray.put(friendObject); //친구 정보를 json array로 만들어서 저장

                //변경된 friendInfo 값을 shared preference에 반영하는 쿼리 생성
                MemberDao memberDao = new MemberDao(context);
                MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
                memberDto.setMemberFriendInfo(friendArray.toString());
                memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
            }
            //친구가 최소한 한 명 이상 있는 경우
            else {
                //모든 친구목록을 불러와서 friendArrList에 저장
                friendArrList = readAllFriendInfo(loginEmailKey);
                friendArrList.add(friendDto); //arrayList에 새로운 친구 추가

                //수정된 ArrayList를 gson 활용해서 JSON으로 변경
                String jsonObject = gson.toJson(friendArrList);

                //변경된 friendInfo 값을 shared preference에 반영하는 쿼리 생성
                MemberDao memberDao = new MemberDao(context);
                MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
                memberDto.setMemberFriendInfo(jsonObject);
                memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 친구의 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneFriendInfo(String loginEmailKey, String friendEmail, FriendDto updateFriendDto) {
        //객체 선언
        String friendListValue = getFriendListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FriendDto> friendArrList = new ArrayList<FriendDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(friendListValue);
            //반복문을 돌려 저장된 이메일 중 선택한 친구 이메일을 찾고 해당 MemberDto의 닉네임 값 변경
            for (int i = 0; i < jsonArray.length(); i++) {
                String friendInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FriendDto friendDto = gson.fromJson(friendInfoLine, FriendDto.class);
                friendArrList.add(friendDto); //arraylist에 값을 집어넣음

                //내가 선택한 친구 이메일과 shared preference에 저장된 이메일이 일치하는 경우
                if (friendDto.getFriendEmail().equals(friendEmail)) {
                    friendArrList.set(i, updateFriendDto); //해당 friendDto의 값을 덮어씌움
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(friendArrList);

        //변경된 friendInfo 값을 shared preference에 반영하는 쿼리 생성
        MemberDao memberDao = new MemberDao(context);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
        memberDto.setMemberFriendInfo(jsonObject);
        memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 친구를 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneFriendInfo(String loginEmailKey, String friendEmailKey) {
        //객체 선언
        String friendListValue = getFriendListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FriendDto> friendArrList = new ArrayList<FriendDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(friendListValue);

            //반복문을 돌려 저장된 이메일 중 선택한 친구 이메일을 찾고 해당 MemberDto를 삭제
            for (int i = 0; i < jsonArray.length(); i++) {
                String friendInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FriendDto friendDto = gson.fromJson(friendInfoLine, FriendDto.class);
                friendArrList.add(friendDto); //arraylist에 값을 집어넣음

                //내가 선택한 친구 이메일과 shared preference에 저장된 이메일이 일치하는 경우
                if (friendDto.getFriendEmail().equals(friendEmailKey)) {
                    //친구를 조언자로 등록한 피드백 정보에서 친구 이메일/이름/사진을 "NONE"으로 변경
                    FeedbackDao feedbackDao = new FeedbackDao(context);
                    //전부 수정한 feedback arraylist를 shared preference에 업데이트
                    feedbackDao.updateOneFriendEmailFeedbackInfo(loginEmailKey, friendEmailKey);

                    friendArrList.remove(i); //해당 친구 데이터를 삭제
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(friendArrList);

        //변경된 friendInfo 값을 shared preference에 반영하는 쿼리 생성
        MemberDao memberDao = new MemberDao(context);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
        memberDto.setMemberFriendInfo(jsonObject);
        memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //친구 목록에서 친구의 이메일이 있는지 없는지를 확인하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public boolean friendEmailDuplicationConfirm(String loginEmailKey, String friendEmailKey) {
        boolean friendEmailDuplicationFlag = false; //이메일 중복 확인 플래그

        //객체 선언
        String friendListValue = getFriendListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FriendDto> friendArrList = new ArrayList<FriendDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(friendListValue);

            for (int i = 0; i < jsonArray.length(); i++) {
                String friendInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FriendDto friendDto = gson.fromJson(friendInfoLine, FriendDto.class);
                friendArrList.add(friendDto); //arraylist에 값을 집어넣음

                //shared preference에 저장된 이메일과 친구 이메일이 일치하는 경우
                if (friendDto.getFriendEmail().equals(friendEmailKey)) {
                    friendEmailDuplicationFlag = true;
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Flag 값을 반환
        //중복인 경우 true, 중복이 아닌 경우 false
        return friendEmailDuplicationFlag;
    }

    ///////////////////////////////////////////////////////////////////////////
    //친구 목록(memberFriendInfo)를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public String getFriendListValue(String loginEmailKey) {
        //memberDao에서 한 명의 회원 정보를 가져옴
        MemberDao memberDao = new MemberDao(context);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);

        //JSONObject에서 friendInfo 항목만 꺼내와서 String으로 저장
        String friendListValue = memberDto.getMemberFriendInfo();

        return friendListValue;
    }

    ///////////////////////////////////////////////////////////////////////////
    //친구 목록에서 친구 한 명의 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public FriendDto readOneFriendInfo(String loginEmailKey, String friendEmail) {
        //객체 선언
        String friendListValue = getFriendListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        FriendDto friendDto = new FriendDto(); //친구 정보를 저장할 FriendDto 객체
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(friendListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String friendInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                friendDto = gson.fromJson(friendInfoLine, FriendDto.class);

                //shared preference에 저장된 이메일과 친구 이메일이 일치하는 경우
                if (friendDto.getFriendEmail().equals(friendEmail)) {
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return friendDto;
    }

    ///////////////////////////////////////////////////////////////////////////
    //친구 목록에서 모든 친구 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllFriendInfo(String loginEmailKey) {
        //객체 선언
        String friendListValue = getFriendListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FriendDto> friendArrList = new ArrayList<FriendDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(friendListValue);

            for (int i = 0; i < jsonArray.length(); i++) {
                String friendInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FriendDto friendDto = gson.fromJson(friendInfoLine, FriendDto.class);
                friendArrList.add(friendDto); //arraylist에 값을 집어넣음

                System.out.println("friendArrList의 Key 값은 " + friendArrList.get(i).getFriendKey());
                System.out.println("실행되나요4");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return friendArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //친구 목록에서 탈퇴하지 않았고 쌍방관계인 모든 친구 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllAliveFriendInfo(String loginEmailKey) {
        //객체 선언
        String friendListValue = getFriendListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FriendDto> friendArrList = new ArrayList<FriendDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(friendListValue);

            for (int i = 0; i < jsonArray.length(); i++) {
                String friendInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FriendDto friendDto = gson.fromJson(friendInfoLine, FriendDto.class);
                //친구가 회원 탈퇴를 하지 않은 경우
                if (!friendDto.getFriendNickName().equals("anonymous")) {
                    //친구와 내가 쌍방관계인 경우
                    if(friendDto.getFriendRelationship().equals("true")) {
                        friendArrList.add(friendDto); //arraylist에 값을 집어넣음
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return friendArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 친구 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneFriendInfoToFirebase(String loginEmailKey, FriendDto friendDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> friendHashMap = new HashMap<>();
        Map<String, Object> friendValue = friendDto.setFriendHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        friendHashMap.put("/friendInfo/" + accountUserKey + "/" + friendDto.getFriendKey(), friendValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(friendHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 선택한 친구 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneFriendInfoToFirebase(String loginEmailKey, FriendDto friendDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> friendHashMap = new HashMap<>();
        Map<String, Object> friendValue = friendDto.setFriendHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        friendHashMap.put("/friendInfo/" + accountUserKey + "/" + friendDto.getFriendKey(), friendValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(friendHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 한 명의 친구 정보를 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneFriendInfoToFirebase(String loginEmailKey, FriendDto friendDto) {
        //친구 정보 삭제하기 전 상대방의 친구 관계를 "false"로 만듦
        //로그인 친구 회원 고유 키 생성
        String tempFriendKey = friendDto.getFriendEmail().replace("@", "_");
        String friendUserKey = "MemberKey_" + tempFriendKey.replace(".", "_");

        //로그인 친구의 친구 회원 고유 키 생성
        String tempFriendUserKey = loginEmailKey.replace("@", "_");
        String accountFriendUserKey = "FriendKey_" + tempFriendUserKey.replace(".", "_");

        //친구의 친구 관계를 false로 만듦
        FirebaseDatabase.getInstance().getReference("friendInfo").child(friendUserKey).child(accountFriendUserKey).child("friendRelationship").setValue("false");

        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> friendHashMap = new HashMap<>();
        Map<String, Object> friendValue = null;

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        //firebase에서 친구 정보 삭제
        friendHashMap.put("/friendInfo/" + accountUserKey + "/" + friendDto.getFriendKey(), friendValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(friendHashMap);
    }
}
