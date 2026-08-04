package com.talkkia.api.repositories;

import com.talkkia.api.entity.ContactRequest;
import com.talkkia.api.view.ContactRequestView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface ContactRequestRepository extends Neo4jRepository<ContactRequest, Long> {

    public boolean existsByFromIdAndToId(Long userId1, Long userId2);

    //@Query("MATCH (n:ContactRequest)-[:TO]-(u:User {id:$userId}) RETURN n, u.name AS name")
    public List<ContactRequestView> findAllByToId(Long userId);
    
    public Optional<ContactRequestView> findByFromIdAndToId(Long senderId, Long receiverId);
    
    public Optional<ContactRequestView> findOneById(Long requestId);
}
