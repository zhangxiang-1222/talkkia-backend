package com.talkkia.api.view;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;

public interface PostView {

    public Long getId();

    public String getPostText();

    public String[] getPostPicVid();

    public String getDateTime();

    public PostBy getPostCreator();

    public List<UserId> getLikes();

    public List<UserId> getDislikes();
    
    public List<CommentId> getComments();
    
    interface PostBy {
        public Long getId();
    }
    
    interface CommentId{
        
    }
    
    interface UserId{
        public Long getId();
    }
}
