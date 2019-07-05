package com.remindfeedbackproject.Dto;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

//메모 게시글 데이터 교환을 위한 DTO 클래스
public class MemoDto {
    private String memoKey;
    private String memoTitle;
    private String memoContent;
    private String memoDate;

    public MemoDto(){}

    //recycler view에 넣을 정보를 가져와 저장하는 메소드
    public void setMemoItem(String memoKey, String memoTitle, String memoContent, String memoDate){
        this.memoKey = memoKey;
        this.memoTitle = memoTitle;
        this.memoContent = memoContent;
        this.memoDate = memoDate;
    }

    //firebase에 저장할 memoInfo HashMap
    @Exclude
    public Map<String, Object> setMemoHashMap(){
        HashMap<String, Object> memoHashMap = new HashMap<>();
        memoHashMap.put("memoKey", memoKey);
        memoHashMap.put("memoTitle", memoTitle);
        memoHashMap.put("memoContent", memoContent);
        memoHashMap.put("memoDate", memoDate);

        return memoHashMap;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemoKey() {
        return memoKey;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemoKey(String memoKey) {
        this.memoKey = memoKey;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemoTitle() {
        return memoTitle;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemoTitle(String memoTitle) {
        this.memoTitle = memoTitle;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemoContent() {
        return memoContent;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemoContent(String memoContent) {
        this.memoContent = memoContent;
    }

    //값을 가져오기 위한 Getter 메소드
    public String getMemoDate() {
        return memoDate;
    }

    //값을 저장하기 위한 Setter 메소드
    public void setMemoDate(String memoDate) {
        this.memoDate = memoDate;
    }
}
