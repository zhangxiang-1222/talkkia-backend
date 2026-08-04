package com.talkkia.api.repositories;

import com.talkkia.api.entity.Post;
import com.talkkia.api.view.PostCommentView;
import com.talkkia.api.view.PostDTO;
import com.talkkia.api.view.PostView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.query.Query;

@Repository
public interface PostRepository extends Neo4jRepository<Post, Long> {

    PostView findPostById(Long postId);

    List<Post> findAllByPostCreatorId(Long userId);

    Optional<Post> findByIdAndPostCreatorId(Long postId, Long userId);

    Page<PostView> findAllPostByPostCreatorIdIn(Pageable pageable, List<Long> postCreators);

    Optional<Post> findPostByIdAndPostCreatorIdIn(Long postId, List<Long> creatorIds);

    boolean existsByIdAndPostCreatorIdIn(Long postId, List<Long> creatorIds);

    Optional<PostCommentView> findByIdAndPostCreatorIdIn(Long postId, List<Long> creatorIds);

    Optional<Page<PostView>> findAllByPostTextContainsIgnoreCaseAndPostCreatorIdIn(String keyword, List<Long> postCreators, Pageable pageable);

    public boolean existsByIdAndPostCreatorId(Long postId, Long userId);

    public PostView findPostByIdAndPostCreatorId(Long postId, Long creatorId);

    public Page<PostView> findAllPostByPostCreatorId(Pageable pageable, Long userId);

}
