package com.remindfeedbackproject.Dto;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class VoiceDto {
    private String voiceKey;
    private String voiceTitle;
    private String voiceLength;
    private String voicePath;
    private String voiceDate;

    //recycler view에 넣을 정보를 가져와 저장하는 메소드
    public void setVoiceItem(String voiceKey, String voiceTitle, String voiceLength, String voicePath, String voiceDate) {
        this.voiceKey = voiceKey;
        this.voiceTitle = voiceTitle;
        this.voiceLength = voiceLength;
        this.voicePath = voicePath;
        this.voiceDate = voiceDate;
    }

    //firebase에 저장할 voiceInfo HashMap
    @Exclude
    public Map<String, Object> setVoiceHashMap(){
        HashMap<String, Object> voiceHashMap = new HashMap<>();
        voiceHashMap.put("voiceKey", voiceKey);
        voiceHashMap.put("voiceTitle", voiceTitle);
        voiceHashMap.put("voiceLength", voiceLength);
        voiceHashMap.put("voicePath", voicePath);
        voiceHashMap.put("voiceDate", voiceDate);

        return voiceHashMap;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVoiceKey() {
        return voiceKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVoiceKey(String voiceKey) {
        this.voiceKey = voiceKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVoiceTitle() {
        return voiceTitle;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVoiceTitle(String voiceTitle) {
        this.voiceTitle = voiceTitle;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVoiceLength() {
        return voiceLength;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVoiceLength(String voiceLength) {
        this.voiceLength = voiceLength;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVoicePath() {
        return voicePath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVoiceDate() {
        return voiceDate;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVoiceDate(String voiceDate) {
        this.voiceDate = voiceDate;
    }
}
