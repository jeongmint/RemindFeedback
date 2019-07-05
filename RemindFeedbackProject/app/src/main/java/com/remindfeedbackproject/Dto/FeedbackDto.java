package com.remindfeedbackproject.Dto;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class FeedbackDto {
    private String feedbackKey; //피드백의 고유한 번호(중복 불가)
    private String feedbackTag; //피드백 진행 상태
    private String feedbackTitle; //피드백 제목
    private String feedbackDate; //피드백 작성 날짜
    private String feedbackBackground; //피드백 배경
    private String adviserEmail; //피드백 조언자의 이메일
    private String adviserNickName; //피드백 조언자의 이름
    private String adviserPhotoPath; //피드백 조언자의 사진 경로
    private String memoInfo; //피드백 메모 정보
    private String photoInfo; //피드백 사진 정보
    private String voiceInfo; //피드백 녹음 정보
    private String videoInfo; //피드백 녹화 정보

    //recycler view에 넣을 정보를 가져와 저장하는 메소드
    public void setFeedbackItem(String feedbackKey, String feedbackTag, String feedbackTitle, String feedbackDate, String feedbackBackground,
                                String adviserEmail, String adviserNickName, String adviserPhotoPath,
                                String memoInfo, String photoInfo, String voiceInfo, String videoInfo){
        this.feedbackKey = feedbackKey;
        this.feedbackTag = feedbackTag;
        this.feedbackTitle = feedbackTitle;
        this.feedbackDate = feedbackDate;
        this.feedbackBackground = feedbackBackground;
        this.adviserEmail = adviserEmail;
        this.adviserNickName = adviserNickName;
        this.adviserPhotoPath = adviserPhotoPath;
        this.memoInfo = memoInfo;
        this.photoInfo = photoInfo;
        this.voiceInfo = voiceInfo;
        this.videoInfo = videoInfo;
    }

    //firebase에 저장할 feedbackInfo HashMap
    @Exclude
    public Map<String, Object> setFeedbackHashMap(){
        Map<String, Object> feedbackHashMap = new HashMap<>();

        feedbackHashMap.put("feedbackKey", feedbackKey);
        feedbackHashMap.put("feedbackTag", feedbackTag);
        feedbackHashMap.put("feedbackTitle", feedbackTitle);
        feedbackHashMap.put("feedbackDate", feedbackDate);
        feedbackHashMap.put("feedbackBackground", feedbackBackground);
        feedbackHashMap.put("adviserEmail", adviserEmail);
        feedbackHashMap.put("adviserNickName", adviserNickName);
        feedbackHashMap.put("adviserPhotoPath", adviserPhotoPath);
        return feedbackHashMap;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFeedbackKey() {
        return feedbackKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFeedbackKey(String feedbackKey) {
        this.feedbackKey = feedbackKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFeedbackTag() {
        return feedbackTag;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFeedbackTag(String feedbackTag) {
        this.feedbackTag = feedbackTag;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFeedbackTitle() {
        return feedbackTitle;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFeedbackTitle(String feedbackTitle) {
        this.feedbackTitle = feedbackTitle;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFeedbackDate() {
        return feedbackDate;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFeedbackDate(String feedbackDate) {
        this.feedbackDate = feedbackDate;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFeedbackBackground() {
        return feedbackBackground;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFeedbackBackground(String feedbackBackground) {
        this.feedbackBackground = feedbackBackground;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getAdviserEmail() {
        return adviserEmail;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setAdviserEmail(String adviserEmail) {
        this.adviserEmail = adviserEmail;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getAdviserNickName() {
        return adviserNickName;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setAdviserNickName(String adviserNickName) {
        this.adviserNickName = adviserNickName;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getAdviserPhotoPath() {
        return adviserPhotoPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setAdviserPhotoPath(String adviserPhotoPath) {
        this.adviserPhotoPath = adviserPhotoPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemoInfo() {
        return memoInfo;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemoInfo(String memoInfo) {
        this.memoInfo = memoInfo;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getPhotoInfo() {
        return photoInfo;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setPhotoInfo(String photoInfo) {
        this.photoInfo = photoInfo;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVoiceInfo() {
        return voiceInfo;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVoiceInfo(String voiceInfo) {
        this.voiceInfo = voiceInfo;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVideoInfo() {
        return videoInfo;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVideoInfo(String videoInfo) {
        this.videoInfo = videoInfo;
    }
}
