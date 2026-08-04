package com.talkkia.api.services;

import com.talkkia.api.view.CommentView;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {

    public List<CommentView> getAllCommentByPostId(Pageable pageable, Long postId, Long userId);

    public CommentView getCommentById(Long commentId, Long userId, Long postId);

    public CommentView createComment(Long userId, Long postId, String commentTxt, String commentPic);

    public String deleteComment(Long userId, Long commentId);
}
