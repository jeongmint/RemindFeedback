package com.remindfeedbackproject.Dto;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class VideoDto {
    private String videoKey; //녹화 고유 키 값
    private String videoTitle; //녹화한 영상 제목
    private String videoLength; //녹화한 영상 길이
    private String videoPath; //녹화한 영상의 폴더 경로
    private String videoDate; //녹화한 영상 날짜
    private String videoThumbnailPath; //녹화한 영상 썸네일 사진 경로

    public VideoDto(){}

    //recycler view에 넣을 정보를 가져와 저장하는 메소드
    public void setVideoItem(String videoKey, String videoTitle, String videoLength, String videoPath, String videoDate,
                             String videoThumbnailPath){
        this.videoKey = videoKey;
        this.videoTitle = videoTitle;//파일의 제목을 받아옴
        this.videoLength = videoLength; //파일의 길이를 받아옴
        this.videoPath = videoPath; //파일의 경로를 받아옴
        this.videoDate = videoDate;
        this.videoThumbnailPath = videoThumbnailPath;
    }

    //firebase에 저장할 videoInfo HashMap
    @Exclude
    public Map<String, Object> setVideoHashMap(){
        HashMap<String, Object> videoHashMap = new HashMap<>();
        videoHashMap.put("videoKey", videoKey);
        videoHashMap.put("videoTitle", videoTitle);
        videoHashMap.put("videoLength", videoLength);
        videoHashMap.put("videoPath", videoPath);
        videoHashMap.put("videoDate", videoDate);
        videoHashMap.put("videoThumbnailPath", videoThumbnailPath);

        return videoHashMap;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVideoKey() {
        return videoKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVideoKey(String videoKey) {
        this.videoKey = videoKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVideoTitle() {
        return videoTitle;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVideoLength() {
        return videoLength;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVideoLength(String videoLength) {
        this.videoLength = videoLength;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVideoPath() {
        return videoPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVideoDate() {
        return videoDate;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVideoDate(String videoDate) {
        this.videoDate = videoDate;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getVideoThumbnailPath() {
        return videoThumbnailPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setVideoThumbnailPath(String videoThumbnailPath) {
        this.videoThumbnailPath = videoThumbnailPath;
    }
}
