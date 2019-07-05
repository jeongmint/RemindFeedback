package com.remindfeedbackproject.Dto;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class FriendDto {
    private String friendKey;
    private String friendEmail;
    private String friendNickName;
    private String friendProfilePhotoPath;
    private String friendProfileMessage;
    private String friendRelationship;

    public FriendDto(){}

    //recycler view에 넣을 정보를 가져오는 메소드
    public void setFriendListItem(String friendKey, String friendEmail, String friendNickName, String friendProfilePhotoPath, String friendProfileMessage,
                                  String friendRelationship){
        this.friendKey = friendKey;
        this.friendEmail = friendEmail;
        this.friendNickName = friendNickName;
        this.friendProfilePhotoPath = friendProfilePhotoPath;
        this.friendProfileMessage = friendProfileMessage;
        this.friendRelationship = friendRelationship;
    }

    //firebase에 저장할 friendInfo HashMap
    @Exclude
    public Map<String, Object> setFriendHashMap(){
        HashMap<String, Object> friendHashMap = new HashMap<>();
        friendHashMap.put("friendKey", friendKey);
        friendHashMap.put("friendEmail", friendEmail);
        friendHashMap.put("friendNickName", friendNickName);
        friendHashMap.put("friendProfilePhotoPath", friendProfilePhotoPath);
        friendHashMap.put("friendProfileMessage", friendProfileMessage);
        friendHashMap.put("friendRelationship", friendRelationship);

        return friendHashMap;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFriendKey() {
        return friendKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFriendKey(String friendKey) {
        this.friendKey = friendKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFriendEmail() {
        return friendEmail;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFriendNickName() {
        return friendNickName;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFriendNickName(String friendNickName) {
        this.friendNickName = friendNickName;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFriendProfilePhotoPath() {
        return friendProfilePhotoPath;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFriendProfilePhotoPath(String friendProfilePhotoPath) {
        this.friendProfilePhotoPath = friendProfilePhotoPath;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFriendProfileMessage() {
        return friendProfileMessage;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFriendProfileMessage(String friendProfileMessage) {
        this.friendProfileMessage = friendProfileMessage;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getFriendRelationship() {
        return friendRelationship;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setFriendRelationship(String friendRelationship) {
        this.friendRelationship = friendRelationship;
    }
}