package com.talkkia.api.view;

import java.util.List;

public interface PostCommentView {

    public Long getId();

    public String getPostText() ;

    public String[] getPostPicVid();

    public String getDateTime();

    public UserView getPostCreator() ;

    public List<UserView> getLikes() ;

    public List<UserView> getDislikes();

    public List<CommentView> getComments();


}
