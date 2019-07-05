package com.remindfeedbackproject.Dto;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class AdviserDto {
    private String adviserKey; //피드백 조언 고유 키
    private String receiverFeedbackKey; //피드백 수혜자의 고유한 피드백 번호(중복 불가)
    private String receiverEmail; //피드백 수혜자의 이메일(중복 가능)

    //recycler view에 넣을 정보를 가져와 저장하는 메소드
    public void setAdviserItem(String adviserKey, String receiverEmail, String receiverFeedbackKey){
        this.adviserKey = adviserKey;
        this.receiverEmail = receiverEmail;
        this.receiverFeedbackKey = receiverFeedbackKey;
    }

    //firebase에 저장할 adviserInfo HashMap
    @Exclude
    public Map<String, Object> setAdviserHashMap(){
        HashMap<String, Object> adviserHashMap = new HashMap<>();
        adviserHashMap.put("adviserKey", adviserKey);
        adviserHashMap.put("receiverEmail", receiverEmail);
        adviserHashMap.put("receiverFeedbackKey", receiverFeedbackKey);

        return adviserHashMap;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getAdviserKey() {
        return adviserKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setAdviserKey(String adviserKey) {
        this.adviserKey = adviserKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getReceiverFeedbackKey() {
        return receiverFeedbackKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setReceiverFeedbackKey(String receiverFeedbackKey) {
        this.receiverFeedbackKey = receiverFeedbackKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getReceiverEmail() {
        return receiverEmail;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }
}