package com.remindfeedbackproject.Dto;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class PhotoDto {
    private String photoKey;
    private String photoTitle;
    private String photoPath;
    private String photoDate;

    //recycler view에 넣을 정보를 가져와 저장하는 메소드
    public void setPhotoItem(String photoKey, String photoTitle, String photoPath, String photoDate){
        this.photoKey = photoKey;
        this.photoTitle = photoTitle;
        this.photoPath = photoPath;
        this.photoDate = photoDate;
    }

    //firebase에 저장할 photoInfo HashMap
    @Exclude
    public Map<String, Object> setPhotoHashMap(){
        HashMap<String, Object> photoHashMap = new HashMap<>();
        photoHashMap.put("photoKey", photoKey);
        photoHashMap.put("photoTitle", photoTitle);
        photoHashMap.put("photoPath", photoPath);
        photoHashMap.put("photoDate", photoDate);

        return photoHashMap;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getPhotoKey() {
        return photoKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setPhotoKey(String photoKey) {
        this.photoKey = photoKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getPhotoTitle() {
        return photoTitle;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setPhotoTitle(String photoTitle) {
        this.photoTitle = photoTitle;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getPhotoPath() {
        return photoPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getPhotoDate() {
        return photoDate;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setPhotoDate(String photoDate) {
        this.photoDate = photoDate;
    }
}
