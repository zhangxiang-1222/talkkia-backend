package com.talkkia.api.view;

public interface BusinessPostView {

    public Long getId();

    public String getPostTitle() ;
    
    public String getPostText() ;

    public String[] getPostPicVid();

    public String getDateTime();

    public BusinessView getPostFrom() ;

    public Integer getLikes() ;

    public Integer getDislikes();
    
    public Integer getShared();
}
