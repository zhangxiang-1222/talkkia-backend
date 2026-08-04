package com.talkkia.api.model;

public enum SearchType{
    ALL("all"),
    BUSINESS_POST("business_post"),
    BUSINESS_ARTICLE("business_article"),
    POST("post"),
    NEWS_ARTICLE("news_article");

    String type;

    SearchType(String pType){
        type = pType;
    }

    public String getType(){
        return type;
    }
}

