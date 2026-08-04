package com.talkkia.api.repositories;

import com.talkkia.api.entity.Step;
import com.talkkia.api.entity.UserTimeZone;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: Neo4j interface of user Time Zone
 */
public interface UserTimeZoneRepository extends Neo4jRepository<UserTimeZone, Long> {

    /**
     * find user Time Zone By UserId
     * @param userId
     * @return
     */
    @Query("MATCH (n:UserTimeZone) where n.userId = $userId RETURN n")
    UserTimeZone findUserTimeZoneByUserId(@Param("userId") Long userId);

    @Query("MATCH (n:UserTimeZone) where n.userId = $userId RETURN n")
    List<UserTimeZone> findUserTimeZonesByUserId(@Param("userId") Long userId);
}
