package com.remindfeedbackproject.Dao;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.remindfeedbackproject.Dto.FeedbackDto;
import com.remindfeedbackproject.Dto.MemberDto;
import com.remindfeedbackproject.Dto.MemoDto;
import com.remindfeedbackproject.Dto.PhotoDto;
import com.remindfeedbackproject.Dto.VideoDto;
import com.remindfeedbackproject.Dto.VoiceDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FeedbackDao {
    Context context;

    public FeedbackDao(Context context) {
        this.context = context; //액티비티 내부가 아니라 context를 받아와야 함
    }

    ///////////////////////////////////////////////////////////////////////////
    //피드백 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertFeedbackInfo(String loginEmailKey, FeedbackDto feedbackDto) {
        //피드백의 진행 상태, 제목, 시간, 피드백 조언자의 사진 경로, 이름(별명)을 저장
        //insert feedbackTag, feedbackTitle, feedbackTime, adviserPhotoPath, adviserNickName

        //객체 선언
        String feedbackListValue = getFeedbackListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FeedbackDto> feedbackArrList = new ArrayList<FeedbackDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        //피드백이 한 개도 없는 경우
        if (feedbackListValue.equals("NONE")) {
            JSONObject feedbackObject = new JSONObject(); //피드백 정보를 저장할 json object 객체 생성
            JSONArray feedbackArray = new JSONArray(); //피드백 정보 json object를 저장할 json array 객체 생성
            try {
                //피드백을 구분하는 키 값은 여기서 지정
                feedbackObject.put("feedbackKey", feedbackDto.getFeedbackKey()); //피드백 고유 키 값 저장
                feedbackObject.put("feedbackTag", feedbackDto.getFeedbackTag()); //피드백 진행 상태 저장
                feedbackObject.put("feedbackTitle", feedbackDto.getFeedbackTitle()); //피드백 제목 저장
                feedbackObject.put("feedbackDate", feedbackDto.getFeedbackDate()); //피드백 날짜 저장
                feedbackObject.put("feedbackBackground", feedbackDto.getFeedbackBackground()); //피드백 배경 저장
                feedbackObject.put("adviserEmail", feedbackDto.getAdviserEmail()); //피드백 조언자의 이메일을 저장
                feedbackObject.put("adviserNickName", feedbackDto.getAdviserNickName()); //피드백 조언자의 이름(별명)을 저장
                feedbackObject.put("adviserPhotoPath", feedbackDto.getAdviserPhotoPath()); //피드백 조언자의 사진 경로 저장
                feedbackObject.put("memoInfo", "NONE"); //피드백의 메모 정보를 저장
                feedbackObject.put("photoInfo", "NONE"); //피드백의 사진 정보를 저장
                feedbackObject.put("voiceInfo", "NONE"); //피드백의 녹음 정보를 저장
                feedbackObject.put("videoInfo", "NONE"); //피드백의 녹화 정보를 저장

                feedbackArray.put(feedbackObject); //피드백 정보를 json array로 만들어서 저장

                //변경된 feedbackInfo 값을 shared preference에 반영하는 쿼리 생성
                MemberDao memberDao = new MemberDao(context);
                MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
                memberDto.setMemberFeedbackInfo(feedbackArray.toString());
                memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //피드백이 최소한 한 개 이상 있는 경우
        else {
            //모든 친구목록을 불러와서 friendArrList에 저장
            feedbackArrList = readAllFeedbackInfo(loginEmailKey);
            feedbackArrList.add(feedbackDto); //arrayList에 새로운 친구 추가

            //수정된 ArrayList를 gson 활용해서 JSON으로 변경
            String jsonObject = gson.toJson(feedbackArrList);

            //변경된 feedbackInfo 값을 shared preference에 반영하는 쿼리 생성
            MemberDao memberDao = new MemberDao(context);
            MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
            memberDto.setMemberFeedbackInfo(jsonObject);
            memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 피드백의 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneFeedbackInfo(String loginEmailKey, FeedbackDto updateFeedbackDto) {
        //객체 선언
        String feedbackListValue = getFeedbackListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FeedbackDto> feedbackArrList = new ArrayList<FeedbackDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(feedbackListValue);

            //반복문을 돌려 저장된 피드백 정보 중 선택한 피드백을 찾고 해당 FeedbackDto의 정보 변경
            for (int i = 0; i < jsonArray.length(); i++) {
                String feedbackInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FeedbackDto feedbackDto = gson.fromJson(feedbackInfoLine, FeedbackDto.class);
                feedbackArrList.add(feedbackDto); //arraylist에 값을 집어넣음

                //내가 선택한 피드백의 키 값과 shared preference에 저장된 피드백 정보가 일치하는 경우
                if (updateFeedbackDto.getFeedbackKey().equals(feedbackDto.getFeedbackKey())) {
                    feedbackArrList.set(i, updateFeedbackDto); //데이터 수정
                    System.out.println("feedbackArrList의 memoInfo는 " + feedbackArrList.get(i).getMemoInfo());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(feedbackArrList);

        //변경된 feedbackInfo 값을 shared preference에 반영하는 쿼리 생성
        MemberDao memberDao = new MemberDao(context);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
        memberDto.setMemberFeedbackInfo(jsonObject);
        memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //피드백 목록에서 하나의 이메일에 관련된 모든 피드백 정보를 변경하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneFriendEmailFeedbackInfo(String loginEmailKey, String friendEmailKey) {
        //객체 선언
        String feedbackListValue = getFeedbackListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FeedbackDto> feedbackArrList = new ArrayList<FeedbackDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(feedbackListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String feedbackInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FeedbackDto feedbackDto = gson.fromJson(feedbackInfoLine, FeedbackDto.class);
                feedbackArrList.add(feedbackDto); //arraylist에 값을 집어넣음

                //친구 이메일 값과 shared preference에 저장된 이메일이 같은 경우 정보 변경
                if(friendEmailKey.equals(feedbackDto.getAdviserEmail())) {
                    System.out.println("친구 이메일 값은 " + friendEmailKey + ", shared preference에 저장된 값은 " + feedbackDto.getAdviserEmail());

                    FeedbackDto updateFeedbackDto = feedbackArrList.get(i);
                    updateFeedbackDto.setAdviserEmail("NONE");
                    updateFeedbackDto.setAdviserNickName("없음");
                    updateFeedbackDto.setAdviserPhotoPath("NONE");
                    feedbackArrList.set(i, updateFeedbackDto);

                    //firebase에 해당 친구에 관한 피드백 정보 업데이트
                    updateOneFeedbackInfoToFirebase(loginEmailKey, updateFeedbackDto);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(feedbackArrList);

        //변경된 feedbackInfo 값을 shared preference에 반영하는 쿼리 생성
        MemberDao memberDao = new MemberDao(context);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
        memberDto.setMemberFeedbackInfo(jsonObject);
        memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //선택한 피드백을 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneFeedbackInfo(String loginEmailKey, String feedbackKey) {
        //객체 선언
        String feedbackListValue = getFeedbackListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FeedbackDto> feedbackArrList = new ArrayList<FeedbackDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(feedbackListValue);

            //반복문을 돌려 저장된 피드백 정보 중 선택한 피드백을 찾고 해당 FeedbackDto를 삭제
            for (int i = 0; i < jsonArray.length(); i++) {
                String friendInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FeedbackDto feedbackDto = gson.fromJson(friendInfoLine, FeedbackDto.class);
                feedbackArrList.add(feedbackDto); //arraylist에 값을 집어넣음

                //내가 선택한 피드백의 키 값과 shared preference에 저장된 피드백 정보가 일치하는 경우
                if (feedbackKey.equals(feedbackDto.getFeedbackKey())) {
                    feedbackArrList.remove(i); //데이터 삭제
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //수정된 ArrayList를 gson 활용해서 JSON으로 변경
        String jsonObject = gson.toJson(feedbackArrList);

        //변경된 feedbackInfo 값을 shared preference에 반영하는 쿼리 생성
        MemberDao memberDao = new MemberDao(context);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);
        memberDto.setMemberFeedbackInfo(jsonObject);
        memberDao.updateOneMemberInfo(memberDto); //업데이트 쿼리를 수행
    }

    ///////////////////////////////////////////////////////////////////////////
    //피드백 목록(memberFeedbackInfo)를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public String getFeedbackListValue(String loginEmailKey) {
        //memberDao에서 한 명의 회원 정보를 가져옴
        MemberDao memberDao = new MemberDao(context);
        MemberDto memberDto = memberDao.readOneMemberInfo(loginEmailKey);

        //JSONObject에서 friendInfo 항목만 꺼내와서 String으로 저장
        String feedbackListValue = memberDto.getMemberFeedbackInfo();

        return feedbackListValue;
    }

    ///////////////////////////////////////////////////////////////////////////
    //피드백 목록에서 하나의 피드백 위치를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public int readOneFeedbackPosition(String loginEmailKey, String feedbackKey) {
        //객체 선언
        String feedbackListValue = getFeedbackListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        int position = -1; //피드백 위치 값을 저장할 변수 선언
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(feedbackListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String friendInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FeedbackDto feedbackDto = gson.fromJson(friendInfoLine, FeedbackDto.class);

                //찾고자 하는 피드백 키 값과 shared preference에 저장된 키 값이 일치하는 경우
                if (feedbackDto.getFeedbackKey().equals(feedbackKey)) {
                    position = i;
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return position;
    }

    ///////////////////////////////////////////////////////////////////////////
    //피드백 목록에서 하나의 피드백 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public FeedbackDto readOneFeedbackInfo(String loginEmailKey, String feedbackKey) {
        //객체 선언
        String feedbackListValue = getFeedbackListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        FeedbackDto feedbackDto = new FeedbackDto(); //FeedbackDto 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(feedbackListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String feedbackInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                feedbackDto = gson.fromJson(feedbackInfoLine, FeedbackDto.class);

                //찾고자 하는 피드백 키 값과 shared preference에 저장된 키 값이 일치하는 경우
                if (feedbackDto.getFeedbackKey().equals(feedbackKey)) {
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return feedbackDto;
    }

    ///////////////////////////////////////////////////////////////////////////
    //피드백 목록에서 모든 피드백 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllFeedbackInfo(String loginEmailKey) {
        //객체 선언
        String feedbackListValue = getFeedbackListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FeedbackDto> feedbackArrList = new ArrayList<FeedbackDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(feedbackListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String feedbackInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FeedbackDto feedbackDto = gson.fromJson(feedbackInfoLine, FeedbackDto.class);
                feedbackArrList.add(feedbackDto); //arraylist에 값을 집어넣음
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return feedbackArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //피드백 목록에서 조언자가 NONE인 모든 피드백 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ArrayList readAllNoneFeedbackInfo(String loginEmailKey) {
        //객체 선언
        String feedbackListValue = getFeedbackListValue(loginEmailKey); //키값에 따라 가져온 value 저장 객체
        ArrayList<FeedbackDto> feedbackArrList = new ArrayList<FeedbackDto>(); //arrayList 객체 생성
        Gson gson = new Gson(); //Gson 객체 생성

        try {
            JSONArray jsonArray = new JSONArray(feedbackListValue);
            for (int i = 0; i < jsonArray.length(); i++) {
                String feedbackInfoLine = jsonArray.getJSONObject(i).toString(); //JSON Array에 저장된 값을 하나씩 가져옴
                //JSON object를 파싱해서 ArrayList에 넣음
                FeedbackDto feedbackDto = gson.fromJson(feedbackInfoLine, FeedbackDto.class);

                if(feedbackDto.getAdviserEmail().equals("NONE")) {
                    feedbackArrList.add(feedbackDto); //arraylist에 값을 집어넣음
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return feedbackArrList;
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 피드백 정보를 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertOneFeedbackInfoToFirebase(String loginEmailKey, FeedbackDto feedbackDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> feedbackHashMap = new HashMap<>();
        Map<String, Object> feedbackValue = feedbackDto.setFeedbackHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        feedbackHashMap.put("/feedbackInfo/" + accountUserKey + "/" + feedbackDto.getFeedbackKey(), feedbackValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(feedbackHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 한 개의 피드백 정보를 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateOneFeedbackInfoToFirebase(String loginEmailKey, FeedbackDto updateFeedbackDto) {
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> feedbackHashMap = new HashMap<>();
        Map<String, Object> feedbackValue = updateFeedbackDto.setFeedbackHashMap();

        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        feedbackHashMap.put("/feedbackInfo/" + accountUserKey + "/" + updateFeedbackDto.getFeedbackKey(), feedbackValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(feedbackHashMap);
    }

    ///////////////////////////////////////////////////////////////////////////
    //[firebase] 한 개의 피드백을 삭제하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void deleteOneFeedbackInfoToFirebase(String loginEmailKey, FeedbackDto feedbackDto) {
        //회원 고유 키 생성
        String tempUserKey = loginEmailKey.replace("@", "_");
        String accountUserKey = "MemberKey_" + tempUserKey.replace(".", "_");

        //만일 피드백 안에 메모가 있는 경우
        if(!feedbackDto.getMemoInfo().equals("NONE")|!feedbackDto.getMemoInfo().equals("")) {
            //피드백 안의 모든 메모 정보를 가져옴
            MemoDao memoDao = new MemoDao(context);
            ArrayList<MemoDto> memoArrList = memoDao.readAllMemoInfo(loginEmailKey, feedbackDto.getFeedbackKey());

            //메모 정보를 shared preference와 firebase database에서 삭제
            for(int i=0; i<memoArrList.size(); i++){
                memoDao.deleteOneMemoInfo(loginEmailKey, feedbackDto.getFeedbackKey(), memoArrList.get(i).getMemoKey());
                memoDao.deleteOneMemoInfoToFirebase(loginEmailKey, feedbackDto.getFeedbackKey(), memoArrList.get(i).getMemoKey());
            }
        }

        //만일 피드백 안에 사진이 있는 경우
        if(!feedbackDto.getPhotoInfo().equals("NONE")|!feedbackDto.getPhotoInfo().equals("")) {
            //피드백 안의 모든 사진 정보를 가져옴
            PhotoDao photoDao = new PhotoDao(context);
            ArrayList<PhotoDto> photoArrList = photoDao.readAllPhotoInfo(loginEmailKey, feedbackDto.getFeedbackKey());

            //사진 정보를 shared preference, firebase database, storage, 내부 저장소에서 삭제
            for(int i=0; i<photoArrList.size(); i++){
                photoDao.deleteOnePhotoInfo(loginEmailKey, feedbackDto.getFeedbackKey(), photoArrList.get(i).getPhotoKey());
                photoDao.deleteOnePhotoInfoToFirebase(loginEmailKey, feedbackDto.getFeedbackKey(), photoArrList.get(i));
            }
        }

        //만일 피드백 안에 음성이 있는 경우
        if(!feedbackDto.getVoiceInfo().equals("NONE")|!feedbackDto.getVoiceInfo().equals("")) {
            //피드백 안의 모든 음성 정보를 가져옴
            VoiceDao voiceDao = new VoiceDao(context);
            ArrayList<VoiceDto> voiceArrList = voiceDao.readAllVoiceInfo(loginEmailKey, feedbackDto.getFeedbackKey());

            //음성 정보를 shared preference, firebase database, storage, 내부 저장소에서 삭제
            for(int i=0; i<voiceArrList.size(); i++){
                voiceDao.deleteOneVoiceInfo(loginEmailKey, feedbackDto.getFeedbackKey(), voiceArrList.get(i).getVoiceKey());
                voiceDao.deleteOneVoiceInfoToFirebase(loginEmailKey, feedbackDto.getFeedbackKey(), voiceArrList.get(i));
            }
        }

        //만일 피드백 안에 영상이 있는 경우
        if(!feedbackDto.getVideoInfo().equals("NONE")|!feedbackDto.getVideoInfo().equals("")) {
            //피드백 안의 모든 영상 정보를 가져옴
            VideoDao videoDao = new VideoDao(context);
            ArrayList<VideoDto> videoArrList = videoDao.readAllVideoInfo(loginEmailKey, feedbackDto.getFeedbackKey());

            //영상 정보를 shared preference, firebase database, storage, 내부 저장소에서 삭제
            for(int i=0; i<videoArrList.size(); i++){
                videoDao.deleteOneVideoInfo(loginEmailKey, feedbackDto.getFeedbackKey(), videoArrList.get(i).getVideoKey());
                videoDao.deleteOneVideoInfoToFirebase(loginEmailKey, feedbackDto.getFeedbackKey(), videoArrList.get(i));
            }
        }

        //메모, 사진, 음성, 영상 모두 삭제 후 피드백 삭제
        //memberInfo 값을 firebase에 반영
        //firebase 각종 인스턴스 초기화
        FirebaseApp.initializeApp(context);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        Map<String, Object> feedbackHashMap = new HashMap<>();
        Map<String, Object> feedbackValue = null;

        feedbackHashMap.put("/feedbackInfo/" + accountUserKey + "/" + feedbackDto.getFeedbackKey(), feedbackValue);

        //update children 쿼리를 사용해서 firebase에 정보 업데이트
        databaseReference.updateChildren(feedbackHashMap);
    }
}
