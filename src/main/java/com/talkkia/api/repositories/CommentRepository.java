package com.talkkia.api.repositories;

import com.talkkia.api.entity.Comment;
import com.talkkia.api.view.CommentView;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends Neo4jRepository<Comment, Long> {

    Optional<CommentView> findOneById(Long commentId);


}
