package com.talkkia.api.services;

import com.talkkia.api.config.DateTimeConverter;
import com.talkkia.api.entity.BusinessAccount;
import com.talkkia.api.entity.BusinessPost;
import com.talkkia.api.entity.Post;
import com.talkkia.api.entity.User;
import com.talkkia.api.repositories.BusinessAccountRepository;
import com.talkkia.api.repositories.BusinessPostRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.BusinessPostView;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessPostServiceImpl implements BusinessPostService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessAccountRepository businessAccountRepository;
    @Autowired
    private BusinessPostRepository businessPostRepository;
    @Autowired
    private PostService postService;
    
    @Override
    public Long createPost(Long userId, Long businessAccountId, BusinessPost post) {
        User user = userRepository.findById(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        BusinessAccount account = businessAccountRepository.findById(businessAccountId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        post.setPostCreator(user);
        post.setPostFrom(account);
        post.setDateTime(new DateTimeConverter().formatDateTime());
        return businessPostRepository.save(post).getId();
    }

    @Override
    public Boolean updatePost(Long userId, BusinessPost post) {
        businessPostRepository.save(post);
        return true;
    }

    @Override
    public Boolean deletePost(Long userId, Long postId) {
        businessPostRepository.deleteById(postId);
        return true;
    }

    @Override
    public BusinessPostView getPost(Long userId, Long postId) {
        return businessPostRepository.findOneById(postId).get();
    }

    @Override
    public boolean react(Long userId, Long postId, boolean like) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<BusinessPostView> getAllPost(Long userId, Long businessAccountId) {
        return businessPostRepository.findAllByPostFromId(businessAccountId);
    }

    @Override
    public Boolean sharePost(Long userId, Long postId) {
        BusinessPost bp = businessPostRepository.findById(postId).get();
        Post post = new Post();
        post.setPostText(bp.getPostTitle());
        post.setPostPicVid(bp.getPostPicVid());
        post.setPostCreator(userRepository.findById(userId).get());
        post.setDateTime(new DateTimeConverter().formatDateTime());
        postService.createPost(userId, post);
        bp.setShared(bp.getShared() + 1);
        businessPostRepository.save(bp);
        return true;
    }

    @Override
    public Boolean likePost(Long postId, boolean like) {
        BusinessPost bp = businessPostRepository.findById(postId).get();
        if(like)
        bp.setLikes(bp.getLikes() + 1);
        else
            bp.setDislikes(bp.getDislikes() + 1);
        businessPostRepository.save(bp);
        return true;
    }
    
}
