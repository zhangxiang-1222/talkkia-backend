package com.talkkia.api.repositories;

import com.talkkia.api.entity.BusinessPost;
import com.talkkia.api.view.BusinessPostView;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface BusinessPostRepository extends Neo4jRepository<BusinessPost, Long> {
    
    Optional<BusinessPostView> findOneById(Long postId);

    public List<BusinessPostView> findAllByPostFromId(Long businessAccountId);

    public Optional<Page<BusinessPostView>> findByPostTitleContainsIgnoreCase(String keyword, Pageable pageable);
    
}
