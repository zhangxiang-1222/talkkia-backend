package com.talkkia.api.repositories;

import com.talkkia.api.entity.TFit;
import com.talkkia.api.entity.TFitIdentifier;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: Neo4j interface of TFit Identifier
 */
public interface TFitIdentifierRepository extends Neo4jRepository<TFitIdentifier, Long> {

    /**
     * find TFit Identifier By UserId
     * @param userId
     * @return
     */
    @Query("MATCH (n:TFitIdentifier) where n.userId = $userId RETURN n")
    TFitIdentifier findTFitIdentifierByUserId(@Param("userId") Long userId);

}
