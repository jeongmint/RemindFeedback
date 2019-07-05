package com.remindfeedbackproject.Dto;

import android.os.Environment;

import java.io.File;

public class FolderDto {
    //각각의 폴더 경로를 저장하는 변수
    private String rootDirectoryPath;
    private String accountDirectoryPath;
    private String profileDirectoryPath;
    private String feedbackDirectoryPath;
    private String photoDirectoryPath;
    private String voiceDirectoryPath;
    private String videoDirectoryPath;

    //생성자
    public FolderDto() {
        rootDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/remindFeedback";
    }

    //값을 가져오기 위한 Getter 메소드
    public String getRootDirectoryPath() {
        return rootDirectoryPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setRootDirectoryPath(String rootDirectoryPath) {
        this.rootDirectoryPath = rootDirectoryPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getAccountDirectoryPath() {
        return accountDirectoryPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setAccountDirectoryPath(String accountDirectoryPath) {
        this.accountDirectoryPath = accountDirectoryPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getProfileDirectoryPath() {
        return profileDirectoryPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setProfileDirectoryPath(String profileDirectoryPath) {
        this.profileDirectoryPath = profileDirectoryPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFeedbackDirectoryPath() {
        return feedbackDirectoryPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFeedbackDirectoryPath(String feedbackDirectoryPath) {
        this.feedbackDirectoryPath = feedbackDirectoryPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getPhotoDirectoryPath() {
        return photoDirectoryPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setPhotoDirectoryPath(String photoDirectoryPath) {
        this.photoDirectoryPath = photoDirectoryPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVoiceDirectoryPath() {
        return voiceDirectoryPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVoiceDirectoryPath(String voiceDirectoryPath) {
        this.voiceDirectoryPath = voiceDirectoryPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVideoDirectoryPath() {
        return videoDirectoryPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVideoDirectoryPath(String videoDirectoryPath) {
        this.videoDirectoryPath = videoDirectoryPath;
    }


    //디바이스 자체의 저장공간에 루트 디렉토리를 생성하는 메소드
    public void createRootFolder() {
        //디바이스 자체의 저장공간 경로를 저장하는 변수
        String internalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //생성할, 혹은 생성된 폴더의 이름
        String rootFolderName = "remindFeedback";
        //폴더를 포함하는 디렉토리 경로
        rootDirectoryPath = internalStoragePath + "/" + rootFolderName;
        //디렉터리 경로의 폴더 파일 객체를 생성
        File rootFolder = new File(rootDirectoryPath);

        //폴더가 존재하지 않는 경우
        if (!rootFolder.exists()) {
            rootFolder.mkdir(); //해당 파일 이름과 동일한 폴더를 생성
        }
    }

    //루트 디렉토리 아래 가입한 회원 디렉토리를 생성하는 메소드
    public void createAccountFolder(String accountEmailKey) {
        //생성할, 혹은 생성된 폴더의 이름
        //특수문자가 들어가면 안되므로 문자열의 특수문자 변경
        String tempFolderName = accountEmailKey.replace("@", "_");
        String accountFolderName = tempFolderName.replace(".", "_");

        //폴더를 포함하는 디렉토리 경로
        accountDirectoryPath = rootDirectoryPath + "/" + accountFolderName;
        //디렉터리 경로의 폴더 파일 객체를 생성
        File accountFolder = new File(accountDirectoryPath);

        //폴더가 존재하지 않는 경우
        if (!accountFolder.exists()) {
            accountFolder.mkdir(); //해당 파일 이름과 동일한 폴더를 생성
        }

        //회원 폴더를 생성했으면 프로필 폴더 생성
        //디바이스 자체의 저장공간 경로를 저장하는 변수
        //생성할, 혹은 생성된 폴더의 이름
        String profileFolderName = "profile";

        //폴더를 포함하는 디렉토리 경로
        profileDirectoryPath = accountDirectoryPath + "/" + profileFolderName;

        //디렉터리 경로의 폴더 파일 객체를 생성
        File profileFolder = new File(profileDirectoryPath);

        //프로필 폴더가 존재하지 않는 경우
        if (!profileFolder.exists()) {
            profileFolder.mkdir(); //해당 파일 이름과 동일한 폴더를 생성
        }

        //회원 폴더를 생성했으면 피드백 폴더 생성
        //디바이스 자체의 저장공간 경로를 저장하는 변수
        //생성할, 혹은 생성된 폴더의 이름
        String feedbackFolderName = "feedback";

        //폴더를 포함하는 디렉토리 경로
        feedbackDirectoryPath = accountDirectoryPath + "/" + feedbackFolderName;

        //디렉터리 경로의 폴더 파일 객체를 생성
        File feedbackFolder = new File(feedbackDirectoryPath);

        //피드백 폴더가 존재하지 않는 경우
        if (!feedbackFolder.exists()) {
            feedbackFolder.mkdir(); //해당 파일 이름과 동일한 폴더를 생성
        }

        //피드백 폴더 생성했으면 사진, 음성, 영상 폴더 생성
        //디바이스 자체의 저장공간 경로를 저장하는 변수
        //생성할, 혹은 생성된 폴더의 이름
        String photoFolderName = "photo";
        String voiceFolderName = "voice";
        String videoFolderName = "video";

        //폴더를 포함하는 디렉토리 경로
        photoDirectoryPath = feedbackDirectoryPath + "/" + photoFolderName;
        voiceDirectoryPath = feedbackDirectoryPath + "/" + voiceFolderName;
        videoDirectoryPath = feedbackDirectoryPath + "/" + videoFolderName;

        //디렉터리 경로의 폴더 파일 객체를 생성
        File photoFolder = new File(photoDirectoryPath);
        File voiceFolder = new File(voiceDirectoryPath);
        File videoFolder = new File(videoDirectoryPath);

        //폴더가 존재하지 않는 경우
        if (!photoFolder.exists()) {
            photoFolder.mkdir(); //해당 파일 이름과 동일한 폴더를 생성
        }
        if (!voiceFolder.exists()) {
            voiceFolder.mkdir(); //해당 파일 이름과 동일한 폴더를 생성
        }
        if (!videoFolder.exists()) {
            videoFolder.mkdir(); //해당 파일 이름과 동일한 폴더를 생성
        }
    }
}