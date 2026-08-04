package com.talkkia.api.view;

public interface ArticleView {

    Long getId();
    String getArticleURL();
    Integer getLikes();
    Integer getDislikes();
    Integer getShared();
    String getTitle();
    String getDescription();
    String getUrlToImage();
}
