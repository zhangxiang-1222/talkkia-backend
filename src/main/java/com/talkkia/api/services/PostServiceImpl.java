package com.talkkia.api.services;

import com.talkkia.api.config.DateTimeConverter;
import com.talkkia.api.entity.Post;
import com.talkkia.api.entity.User;
import com.talkkia.api.repositories.PostRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.PostDTO;
import com.talkkia.api.view.PostView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public PostDTO createPost(Long userId, Post post) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        post.setPostCreator(user);
        post.setDateTime(new DateTimeConverter().formatDateTime());
        PostView pv = postRepository.findPostById(postRepository.save(post).getId());
        return new PostDTO(pv.getId(), pv.getPostCreator().getId(), pv.getPostText(), pv.getPostPicVid(), pv.getDateTime(), pv.getLikes().size(), pv.getDislikes().size(), pv.getComments().size());
    }

    @Override
    public PostDTO updatePost(Long userId, Post post) {
        Post postExist = postRepository.findByIdAndPostCreatorId(post.getId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        postExist.setPostText(post.getPostText());
        postExist.setPostPicVid(post.getPostPicVid());

        return getPost(userId, postRepository.save(postExist).getId());
    }

    @Override
    public List<PostDTO> getPosts(Pageable pageable, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Long> creatorIds = user.getContacts().stream().map(creator -> creator.getUser().getId()).collect(Collectors.toList());
        creatorIds.add(user.getId());
//        List<User> postCreators = user.getContacts();
//        postCreators.add(user);
//        List<Long> creatorIds = postCreators.stream().map(creator -> creator.getId()).collect(Collectors.toList());
        System.out.println(creatorIds);
        List<PostDTO> posts = new ArrayList<>();
        postRepository.findAllPostByPostCreatorIdIn(pageable, creatorIds).getContent()
                .stream().forEach(post -> posts.add(new PostDTO(post.getId(), post.getPostCreator().getId(), post.getPostText(), post.getPostPicVid(), post.getDateTime(), post.getLikes().size(), post.getDislikes().size(), post.getComments().size())));
        return posts;
    }

    @Override
    public Map deletePost(Long userId, Long postId) {
        Map<String, String> response = new HashMap<>();
        System.out.println(userId + " : " + postId);
        if (postRepository.existsByIdAndPostCreatorId(postId, userId)) {
            postRepository.deleteById(postId);
            response.put("status", "true");
            response.put("message", "Post has been deleted.");
            return response;
        }
        response.put("status", "false");
        response.put("message", "Post doesn't exist.");
        return response;

    }

    @Override
    public PostDTO getPost(Long userId, Long postId) {
        PostView pv = postRepository.findPostByIdAndPostCreatorId(postId, userId);
        return new PostDTO(pv.getId(), pv.getPostCreator().getId(), pv.getPostText(), pv.getPostPicVid(), pv.getDateTime(), pv.getLikes().size(), pv.getDislikes().size(), pv.getComments().size());
    }

    @Override
    public List<PostDTO> getAllPostsByUser(Pageable pageable, Long userId) {
        List<PostDTO> posts = new ArrayList<>();
        postRepository.findAllPostByPostCreatorId(pageable, userId).getContent()
                .stream().forEach(post -> posts.add(new PostDTO(post.getId(), post.getPostCreator().getId(), post.getPostText(), post.getPostPicVid(), post.getDateTime(), post.getLikes().size(), post.getDislikes().size(), post.getComments().size())));
        return posts;
    }

    @Override
    public boolean react(Long userId, Long postId, boolean like) {
        try {
            Post post = postRepository.findById(postId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
            User userReact = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            if (!Objects.equals(post.getPostCreator().getId(), userId)
                    && !post.getPostCreator().getContacts().contains(userReact)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            List<User> likes = post.getLikes();
            List<User> disLikes = post.getDislikes();
            if (like) {

                if (disLikes != null) {
                    disLikes.removeIf(user -> user.getId().equals(userId));
                }

                if (likes != null) {
                    if (likes.removeIf(user -> user.getId().equals(userId))) {

                        post.setLikes(likes);
                        postRepository.save(post);
                        return true;
                    }
                    likes.add(userReact);
                } else {
                    likes = new ArrayList<>();
                    likes.add(userReact);
                }
                post.setLikes(likes);

            } else {
                if (likes != null) {
                    likes.removeIf(user -> user.getId().equals(userId));
                    post.setLikes(likes);
                }

                if (disLikes != null) {
                    if (disLikes.removeIf(user -> user.getId().equals(userId))) {
                        post.setDislikes(disLikes);
                        postRepository.save(post);
                        return true;
                    }
                    disLikes.add(userReact);
                } else {
                    disLikes = new ArrayList<>();
                    disLikes.add(userReact);
                }
                post.setDislikes(disLikes);
            }

            postRepository.save(post);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /* for now share is function is depricated. if required in future the same can
    be used.
    @Override
    public Boolean sharePost(Long userId, ShareRequest shareRequest) {

        try {
            Post post = postRepository.findByIdAndPostCreatorId(shareRequest.getPostId(),userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

            List<Long> creatorContacts = post.getPostCreator().getContacts().stream().map(creator -> creator.getId())
                    .collect(Collectors.toList());

            if(shareRequest.isPublic()){
                post.setDoNotShareWith(post.getPostCreator().getDoNotShareWith());
                return true;
            }

            if(shareRequest.isPrivate()){
                post.setDoNotShareWith(creatorContacts);
                return true;
            }

            if(shareRequest.isShareWith()){
                creatorContacts.removeAll(post.getPostCreator().getShareWith());
                post.setDoNotShareWith(creatorContacts);
                return true;
            }
        } catch (Exception e){
            return false;
        }

        return false;
    }
     */
}
