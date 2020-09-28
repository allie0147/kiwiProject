package com.android.projectchatting.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// 게시글 dto
@IgnoreExtraProperties
public class Posts {
    private String uId; // 작성자 id
    private String nickname; //작성자 닉네임
    private String dong; // 동
    private String locationId; // 지역 번호
    private String categoryId; //카테고리 번호
    private String category; //카테고리 명
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String price; // 물품 가격
    private long wDate; // 작성일

    public Posts() {
    }

    public Posts(String uId, String nickname, String dong, String categoryId, String category, String title, String content, String price, long wDate) {
        this.uId = uId;
        this.nickname = nickname;
        this.dong = dong;
        this.categoryId = categoryId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.price = price;
        this.wDate = wDate;
    }

    public String getuId() {
        return uId;
    }

    public String getDong() {
        return dong;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPrice() {
        return price;
    }

    public long getwDate() {
        return wDate;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Posts{" +
                "uId='" + uId + '\'' +
                ", dong='" + dong + '\'' +
                ", locationId='" + locationId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", category='" + category + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", price='" + price + '\'' +
                ", wDate='" + wDate + "}";
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uId", uId);
        result.put("nickname", nickname);
        result.put("dong", dong);
        result.put("categoryId", categoryId);
        result.put("category", category);
        result.put("title", title);
        result.put("content", content);
        result.put("price", price);
        result.put("wDate", wDate);
        return result;
    }

}
