package com.remindfeedbackproject.Dto;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

//회원가입 정보 데이터 교환을 위한 DTO 클래스
public class MemberDto {
    private String memberEmail; //(중복불가) 회원 이메일
    private String memberNickName; //회원 이름(별명)
    private String memberPassword; //회원 비밀번호
    private String memberRegisterDate; //회원 가입일자
    private String memberProfilePhotoPath; //회원 프로필 사진
    private String memberProfileMessage; //회원 프로필 상태 메세지
    private String memberIntroSkip; //회원의 인트로 스킵
    private String memberAdviserInfo; //회원의 조언 정보
    private String memberDataPathInfo; //회원 파일 저장 경로 정보
    private String memberFriendInfo; //회원정보
    private String memberFeedbackInfo; //피드백 정보

    public MemberDto() {
    }

    //모든 회원 정보를 저장하는 메소드
    public void setMemberItem(String memberEmail, String memberNickName, String memberPassword, String memberRegisterDate,
                              String memberProfilePhotoPath, String memberProfileMessage, String memberIntroSkip,
                              String memberAdviserInfo, String memberDataPathInfo, String memberFriendInfo, String memberFeedbackInfo) {
        this.memberEmail = memberEmail;
        this.memberNickName = memberNickName;
        this.memberPassword = memberPassword;
        this.memberRegisterDate = memberRegisterDate;
        this.memberProfilePhotoPath = memberProfilePhotoPath;
        this.memberProfileMessage = memberProfileMessage;
        this.memberIntroSkip = memberIntroSkip;
        this.memberAdviserInfo = memberAdviserInfo;
        this.memberDataPathInfo = memberDataPathInfo;
        this.memberFriendInfo = memberFriendInfo;
        this.memberFeedbackInfo = memberFeedbackInfo;
    }

    //firebase에 저장할 memberInfo HashMap
    @Exclude
    public Map<String, Object> setMemberHashMap(){
        Map<String, Object> memberHashMap = new HashMap<>();
        memberHashMap.put("memberEmail", memberEmail);
        memberHashMap.put("memberNickName", memberNickName);
        memberHashMap.put("memberPassword", memberPassword);
        memberHashMap.put("memberRegisterDate", memberRegisterDate);
        memberHashMap.put("memberProfilePhotoPath", memberProfilePhotoPath);
        memberHashMap.put("memberProfileMessage", memberProfileMessage);
        memberHashMap.put("memberIntroSkip", memberIntroSkip);

        return memberHashMap;
    }

    //firebase에 저장할 memberInfo HashMap
    @Exclude
    public Map<String, Object> setMemberDeleteHashMap(){
        Map<String, Object> memberHashMap = new HashMap<>();
        memberHashMap.put("memberEmail", memberEmail);
        memberHashMap.put("memberNickName", "anonymous");
        memberHashMap.put("memberProfileMessage", "탈퇴한 회원입니다.");
        return memberHashMap;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberEmail() {
        return memberEmail;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberNickName() {
        return memberNickName;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberNickName(String memberNickName) {
        this.memberNickName = memberNickName;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberPassword() {
        return memberPassword;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberPassword(String memberPassword) {
        this.memberPassword = memberPassword;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberRegisterDate() {
        return memberRegisterDate;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberRegisterDate(String memberRegisterDate) {
        this.memberRegisterDate = memberRegisterDate;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberProfilePhotoPath() {
        return memberProfilePhotoPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberProfilePhotoPath(String memberProfilePhotoPath) {
        this.memberProfilePhotoPath = memberProfilePhotoPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberProfileMessage() {
        return memberProfileMessage;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberProfileMessage(String memberProfileMessage) {
        this.memberProfileMessage = memberProfileMessage;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberIntroSkip() {
        return memberIntroSkip;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberIntroSkip(String memberIntroSkip) {
        this.memberIntroSkip = memberIntroSkip;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberAdviserInfo() {
        return memberAdviserInfo;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberAdviserInfo(String memberAdviserInfo) {
        this.memberAdviserInfo = memberAdviserInfo;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberDataPathInfo() {
        return memberDataPathInfo;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberDataPathInfo(String memberDataPathInfo) {
        this.memberDataPathInfo = memberDataPathInfo;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberFriendInfo() {
        return memberFriendInfo;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberFriendInfo(String memberFriendInfo) {
        this.memberFriendInfo = memberFriendInfo;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemberFeedbackInfo() {
        return memberFeedbackInfo;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemberFeedbackInfo(String memberFeedbackInfo) {
        this.memberFeedbackInfo = memberFeedbackInfo;
    }
}