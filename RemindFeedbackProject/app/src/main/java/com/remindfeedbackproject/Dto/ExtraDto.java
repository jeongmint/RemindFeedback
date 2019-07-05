package com.remindfeedbackproject.Dto;

public class ExtraDto {
    private String firstLaunchConfirmKey; //최초 설치 확인
    private String autoLoginConfirmKey; //자동 로그인 확인
    private String autoLoginEmail; //자동 로그인을 할 이메일
    private String autoLoginPassword; //자동 로그인을 할 비밀번호

//    //설정화면에서 변경할 수 있는 변수
//    private String imageWidth;
//    private String imageHeight;
//    private String voiceEncoding;
//    private String videoResolution;

    public ExtraDto(){}

    //모든 기타 정보를 저장하는 메소드
    public void setExtraItem(String firstLaunchConfirmKey, String autoLoginConfirmKey, String autoLoginEmail, String autoLoginPassword){
        this.firstLaunchConfirmKey = firstLaunchConfirmKey;
        this.autoLoginConfirmKey = autoLoginConfirmKey;
        this.autoLoginEmail = autoLoginEmail;
        this.autoLoginPassword = autoLoginPassword;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFirstLaunchConfirmKey() {
        return firstLaunchConfirmKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFirstLaunchConfirmKey(String firstLaunchConfirmKey) {
        this.firstLaunchConfirmKey = firstLaunchConfirmKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getAutoLoginConfirmKey() {
        return autoLoginConfirmKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setAutoLoginConfirmKey(String autoLoginConfirmKey) {
        this.autoLoginConfirmKey = autoLoginConfirmKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getAutoLoginEmail() {
        return autoLoginEmail;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setAutoLoginEmail(String autoLoginEmail) {
        this.autoLoginEmail = autoLoginEmail;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getAutoLoginPassword() {
        return autoLoginPassword;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setAutoLoginPassword(String autoLoginPassword) {
        this.autoLoginPassword = autoLoginPassword;
    }

//    //값을 가져오기 위한 Getter 메소드
//    public String getImageWidth() {
//        return imageWidth;
//    }
//
//    //값을 저장하기 위한 Setter 메소드
//    public void setImageWidth(String imageWidth) {
//        this.imageWidth = imageWidth;
//    }
//
//    //값을 가져오기 위한 Getter 메소드
//    public String getImageHeight() {
//        return imageHeight;
//    }
//
//    //값을 저장하기 위한 Setter 메소드
//    public void setImageHeight(String imageHeight) {
//        this.imageHeight = imageHeight;
//    }
//
//    //값을 가져오기 위한 Getter 메소드
//    public String getVoiceEncoding() {
//        return voiceEncoding;
//    }
//
//    //값을 저장하기 위한 Setter 메소드
//    public void setVoiceEncoding(String voiceEncoding) {
//        this.voiceEncoding = voiceEncoding;
//    }
//
//    //값을 가져오기 위한 Getter 메소드
//    public String getVideoResolution() {
//        return videoResolution;
//    }
//
//    //값을 저장하기 위한 Setter 메소드
//    public void setVideoResolution(String videoResolution) {
//        this.videoResolution = videoResolution;
//    }
}
