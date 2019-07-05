package com.remindfeedbackproject.Dao;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.remindfeedbackproject.Dto.ExtraDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExtraDao {
    //DB를 쓰지 않고 초기 설정값이나 간단한 값을 저장을 위해 사용하는 객체
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;
    Context context;

    //shared preference 파일 이름
    private static final String PREF_NAME = "RemindFeedbackInfo";

    public ExtraDao(Context context) {
        this.context = context; //액티비티 내부가 아니라 context를 받아와야 함

        sharedPreferences = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);//회원 정보 shared preference 파일 생성
        sharedPreferencesEditor = sharedPreferences.edit();//값을 읽어서 저장하기 위해 editor 객체 생성
    }

    ///////////////////////////////////////////////////////////////////////////
    //기타 목록을 저장하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void insertExtraInfo(ExtraDto extraDto) {
        JSONObject extraObject = new JSONObject();//jsonObject객체 생성
        JSONArray extraArray = new JSONArray(); //json object를 저장할 json array 객체 생성
        try {
            extraObject.put("firstLaunchConfirmKey", extraDto.getFirstLaunchConfirmKey()); //최초 실행 확인 키 저장
            extraObject.put("autoLoginConfirmKey", extraDto.getAutoLoginConfirmKey()); //자동 로그인 확인 키 저장
            extraObject.put("autoLoginEmail", extraDto.getAutoLoginEmail()); //자동 로그인 이메일 저장
            extraObject.put("autoLoginPassword", extraDto.getAutoLoginPassword()); //자동 로그인 비밀번호 저장

            extraArray.put(extraObject); //회원 정보를 json array로 만들어서 저장

            System.out.println("기타 목록 생성 쿼리를 수행한 후 ArrayList는 " + extraArray.toString() + "입니다.");

            //변경된 memberInfo 값을 shared preference에 반영하는 쿼리 생성
            sharedPreferencesEditor.putString("extraInfo", extraArray.toString());
            sharedPreferencesEditor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //기타 목록을 수정하는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public void updateExtraInfo(ExtraDto extraDto) {
        JSONObject extraObject = new JSONObject();//jsonObject객체 생성
        JSONArray extraArray = new JSONArray(); //json object를 저장할 json array 객체 생성
        try {
            extraObject.put("firstLaunchConfirmKey", extraDto.getFirstLaunchConfirmKey()); //최초 실행 확인 키 저장
            extraObject.put("autoLoginConfirmKey", extraDto.getAutoLoginConfirmKey()); //자동 로그인 확인 키 저장
            extraObject.put("autoLoginEmail", extraDto.getAutoLoginEmail()); //자동 로그인 이메일 저장
            extraObject.put("autoLoginPassword", extraDto.getAutoLoginPassword()); //자동 로그인 비밀번호 저장

            extraArray.put(extraObject); //회원 정보를 json array로 만들어서 저장

            System.out.println("기타 목록 수정 쿼리를 수행한 후 ArrayList는 " + extraArray.toString() + "입니다.");

            //변경된 memberInfo 값을 shared preference에 반영하는 쿼리 생성
            sharedPreferencesEditor.putString("extraInfo", extraArray.toString());
            sharedPreferencesEditor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //기타 목록(extraInfo) 값을 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public String getExtraListValue() {
        String extraListValue = sharedPreferences.getString("extraInfo", "NONE");

        return extraListValue;
    }

    ///////////////////////////////////////////////////////////////////////////
    //기타 목록에서 모든 기타 정보를 가져오는 메소드
    ///////////////////////////////////////////////////////////////////////////
    public ExtraDto readAllExtraInfo() {
        //객체 선언
        String extraListValue = getExtraListValue(); //키값에 따라 가져온 value 저장 객체
        ExtraDto extraDto = new ExtraDto();
        Gson gson = new Gson(); //Gson 객체

        try {
            JSONArray jsonArray = new JSONArray(extraListValue);
            String extraInfoLine = jsonArray.getJSONObject(0).toString(); //JSON Array에 저장된 값을 가져옴
            //JSON object를 파싱해서 ArrayList에 넣음
            extraDto = gson.fromJson(extraInfoLine, ExtraDto.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return extraDto;
    }
}