package com.remindfeedbackproject.Dao;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.remindfeedbackproject.Dto.FeedbackDto;
import com.remindfeedbackproject.Dto.MemoDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MemoDao {
    Context context;

    public MemoDao(Context context) {
        this.context = context; //액티비티 내부가 아니라 context를 받아와야 함
    }

    ///////////////////////////////////////////////////////////////////////////
    //메모 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneMemoInfo(String loginEmailKey, String feedbackKey, MemoDto memoDto) {
        String memoListValue = getMemoListValue(loginEmailKey, feedbackKey);
        Gson gson = new Gson();

        //메모가 하나도 없는 경우 메모 목록을 불러오지 않고 저장
        //메모가 한 개도 없는 경우
        if (memoListValue.equals("NONE")) {
            JSONObject memoObject = new JSONObject(); //정보를 저장할 json object 객체 생성
            JSONArray memoArray = new JSONArray(); //json object를 저장할 json array 객체 생성
            try {
                memoObject.put("memoKey", memoDto.getMemoKey()); //메모 고유 키 값 저장
                memoObject.put("memoTitle", memoDto.getMemoTitle()); //메모 제목 저장
                memoObject.put("memoContent", memoDto.getMemoContent()); //메모 내용 저장
                memoObject.put("memoDate", memoDto.getMemoDate()); //메모 날짜 저장

                memoArray.put(memoObject); //피드백 정보를 json array로 만들어서 저장
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //변경된 memoInfo 값을 shared preference에 반영하는 쿼리 생성
            FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
            FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
            feedbackDto.setMemoInfo(memoArray.toString());
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //업데이트 쿼리 수행
        }
        //메모가 최소한 한 개 이상 있는 경우
        else {
            //모든 메모목록을 불러와서 저장
            ArrayList<MemoDto> memoArrList = readAllMemoInfo(loginEmailKey, feedbackKey);
            memoArrList.add(memoDto); //arrayList에 새로운 친구 추가

            //수정된 ArrayList를 gson 활용해서 JSON으로 변경
            String jsonObject = gson.toJson(memoArrList);

            System.out.println("memo jsonObject는 " + jsonObject + "입니다.");

            //변경된 memoInfo 값을 shared preference에 반영하는 쿼리 생성
            FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
            FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
            feedbackDto.setMemoInfo(jsonObject);
            feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //업데이트 쿼리 수행
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 메모의 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneMemoInfo(String loginEmailKey, String feedbackKey, int feedbackPosition, MemoDto updateMemoDto) {
        String memoListValue = getMemoListValue(loginEmailKey, feedbackKey);

        ArrayList<MemoDto> memoArrList = new ArrayList<MemoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(memoListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 FeedbackDto를 변경
            for (int i = 0; i < jsonArray.length(); i++) {
                String memoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                MemoDto memoDto = gson.fromJson(memoInfoLine, MemoDto.class);
                memoArrList.add(memoDto); //arraylist에 값을 집어넣음

                //내가 선택한 정보의 키 값과 shared preference에 저장된 정보의 키 값이 일치하는 경우
                if (updateMemoDto.getMemoKey().equals(memoDto.getMemoKey())) {
                    memoArrList.set(i, updateMemoDto); //데이터 수정
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(memoArrList);

        //변경된 memoInfo 값을 shared preference에 반영하는 쿼리 생성
        FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
        feedbackDto.setMemoInfo(jsonObject);
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //업데이트 쿼리 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 메모를 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneMemoInfo(String loginEmailKey, String feedbackKey, String memoKey) {
        String memoListValue = getMemoListValue(loginEmailKey, feedbackKey);
        ArrayList<MemoDto> memoArrList = new ArrayList<MemoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {
            JSONArray jsonArray = new JSONArray(memoListValue);

            //반복문을 돌려 저장된 정보 중 선택한 정보를 찾고 해당 FeedbackDto를 삭제
            for (int i = 0; i < jsonArray.length(); i++) {
                String memoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                MemoDto memoDto = gson.fromJson(memoInfoLine, MemoDto.class);
                memoArrList.add(memoDto); //arraylist에 값을 집어넣음

                //내가 선택한 정보의 키 값과 shared preference에 저장된 정보의 키 값이 일치하는 경우
                if (memoKey.equals(memoDto.getMemoKey())) {
                    memoArrList.remove(i); //데이터 삭제
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(memoArrList);

        //변경된 memoInfo 값을 shared preference에 반영하는 쿼리 생성
        FeedbackDao feedbackDao = new FeedbackDao(context); //feedback dao 객체 생성
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);
        feedbackDto.setMemoInfo(jsonObject);
        feedbackDao.updateOneFeedbackInfo(loginEmailKey, feedbackDto); //삭제 쿼리 수행
    }

    /////////////////////////////////////////////////////////////////////////
    //메모 목록(memoInfo) 값을 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public String getMemoListValue(String loginEmailKey, String feedbackKey) {
        FeedbackDao feedbackDao = new FeedbackDao(context);
        FeedbackDto feedbackDto = feedbackDao.readOneFeedbackInfo(loginEmailKey, feedbackKey);

        String memoListValue = feedbackDto.getMemoInfo();

        return memoListValue;
    }

    ///////////////////////////////////////////////////////////////////////////
    //메모 목록에서 모든 메모 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllMemoInfo(String loginEmailKey, String feedbackKey) {
        String memoListValue = getMemoListValue(loginEmailKey, feedbackKey);
        ArrayList<MemoDto> memoArrList = new ArrayList<MemoDto>(); //JSON Array를 저장할 array list
        Gson gson = new Gson();

        try {

            JSONArray jsonArray = new JSONArray(memoListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String memoInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                MemoDto memoDto = gson.fromJson(memoInfoLine, MemoDto.class);
                memoArrList.add(memoDto); //arraylist에 값을 집어넣음
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return memoArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 메모 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneMemoInfoToFirebase(String loginEmailKey, String feedbackKey, MemoDto memoDto) {
        //TODO : 인터넷 되는지 확인(firebase 동작 안할 수도 있음)
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> memoHashMap = new HashMap<>();
        Map<String, Object> memoValue = memoDto.setMemoHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        memoHashMap.put("/memoInfo/" + accountUserKey + "/" + feedbackKey + "/" + memoDto.getMemoKey(), memoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(memoHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 메모 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneMemoInfoToFirebase(String loginEmailKey, String feedbackKey, MemoDto updateMemoDto) {
        //TODO : 인터넷 되는지 확인(firebase 동작 안할 수도 있음)
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> memoHashMap = new HashMap<>();
        Map<String, Object> memoValue = updateMemoDto.setMemoHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        memoHashMap.put("/memoInfo/" + accountUserKey + "/" + feedbackKey + "/" + updateMemoDto.getMemoKey(), memoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(memoHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 메모 정보를 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneMemoInfoToFirebase(String loginEmailKey, String feedbackKey, String memoKey) {
        //TODO : 인터넷 되는지 확인(firebase 동작 안할 수도 있음)
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> memoHashMap = new HashMap<>();
        Map<String, Object> memoValue = null;

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        memoHashMap.put("/memoInfo/" + accountUserKey + "/" + feedbackKey + "/" + memoKey, memoValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(memoHashMap);
    }
}
