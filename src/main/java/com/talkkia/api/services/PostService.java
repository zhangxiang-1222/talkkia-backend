package com.talkkia.api.services;

import com.talkkia.api.entity.Post;
import com.talkkia.api.view.PostDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PostService {

    PostDTO createPost(Long userId, Post post);

    PostDTO updatePost(Long userId, Post post);

    List<PostDTO> getPosts(Pageable pageable, Long userId);

    Map deletePost(Long userId, Long postId);

    PostDTO getPost(Long userId, Long postId);
    
    List<PostDTO> getAllPostsByUser(Pageable pageable, Long userId);

    boolean react(Long userId, Long postId, boolean like);

}
