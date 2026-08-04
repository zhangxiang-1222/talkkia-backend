package com.talkkia.api.view;

public interface CommentView {

    public Long getId();

    public String getText();

    public String getCommentPicVid();

    public String getTimestamp() ;

    public CommentBy getCommentCreator();
    
    interface CommentBy{
        public Long getId();
    }
}
