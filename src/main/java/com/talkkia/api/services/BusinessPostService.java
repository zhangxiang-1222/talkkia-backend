package com.talkkia.api.services;

import com.talkkia.api.entity.BusinessPost;
import com.talkkia.api.view.BusinessPostView;
import java.util.List;

public interface BusinessPostService {
    Long createPost(Long userId, Long businessAccountId, BusinessPost post);

    Boolean updatePost(Long userId, BusinessPost post);

    //List<PostView> getPosts(Pageable pageable, Long userId);

    Boolean deletePost(Long userId, Long postId);

    BusinessPostView getPost(Long userId, Long postId);

    boolean react(Long userId, Long postId, boolean like);

    public List<BusinessPostView> getAllPost(Long userId, Long businessAccountId);

    public Boolean sharePost(Long userId, Long postId);

    public Boolean likePost(Long postId, boolean like);
}
