package com.talkkia.api.services;

import com.talkkia.api.config.DateTimeConverter;
import com.talkkia.api.entity.Comment;
import com.talkkia.api.relationship.Contact;
import com.talkkia.api.entity.Post;
import com.talkkia.api.entity.User;
import com.talkkia.api.repositories.CommentRepository;
import com.talkkia.api.repositories.PostRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.CommentView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    @Override
    public List<CommentView> getAllCommentByPostId(Pageable pageable, Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        //List<User> commentCreators = user.getContacts();
        List<Long> creatorsId = user.getContacts().stream().map(contact -> contact.getId()).collect(Collectors.toList());
        creatorsId.addAll(userRepository.findAllByContactsUserId(userId).stream().map(contact -> contact.getId()).collect(Collectors.toList()));
        creatorsId.add(userId);
        if(!postRepository.existsByIdAndPostCreatorIdIn(postId, creatorsId)){
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        List<CommentView> commentList = postRepository.findByIdAndPostCreatorIdIn(postId, creatorsId).orElseThrow().getComments();
        return commentList;
    }

    @Override
    public CommentView getCommentById(Long commentId, Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Long> creatorIds = user.getContacts().stream().map(creator -> creator.getUser().getId()).collect(Collectors.toList());
        creatorIds.add(user.getId());
        //List<Long> creatorIds = postCreators.stream().map(creator -> creator.getId()).collect(Collectors.toList());
        Post postExist = postRepository.findPostByIdAndPostCreatorIdIn(postId, creatorIds)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        Optional<CommentView> commentOptional = commentRepository.findOneById(commentId);
        if (!commentOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment Not found");
        }
        return commentOptional.get();
    }

    @Override
    public CommentView createComment(Long userId, Long postId, String commentTxt, String commentPic) {

        Post post = postRepository.findById(postId).get();
        Comment comment = new Comment(0L, commentTxt, commentPic,new DateTimeConverter().formatDateTime(), userRepository.findById(userId).get());
        comment = commentRepository.save(comment);
        post.getComments().add(comment);
        postRepository.save(post);

        return commentRepository.findOneById(comment.getId()).get();
    }

    /*Depricated for now
    @Override
    public Comment updateComment(Long userId, Long commentId, Comment comment) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (!commentOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment Not found");
        }
        if (commentOptional.get().getCommentCreator().getId() == userId) {
            commentOptional.get().setCommentPicVid(comment.getCommentPicVid());
            commentOptional.get().setText(comment.getText());
            comment = commentRepository.save(commentOptional.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not found");
        }
        return comment;
    }
*/
    @Override
    public String deleteComment(Long userId, Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (!comment.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment Not found");
        }
        if (comment.get().getCommentCreator().getId() != userId) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        commentRepository.deleteById(commentId);
        return "Comment having id " + commentId + " has been deleted.";
    }

}
