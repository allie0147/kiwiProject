package com.android.projectchatting.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Category {
    private String digital = "디지털/가전";
    private String furniture = "가구/인테리어";
    private String clothes = "의류/패션/잡화";
    private String beauty = "뷰티/미용";
    private String baby = "유아동/유아도서";
    private String etc = "기타 중고물품";
    private String[] categoryArr = {digital, furniture, clothes, beauty, baby, etc};
    private ArrayList<String> categoryList = new ArrayList<>(Arrays.asList(categoryArr));

    public Category() {
    }

    public ArrayList<String> getCategoryList() {
        return categoryList;
    }
}
