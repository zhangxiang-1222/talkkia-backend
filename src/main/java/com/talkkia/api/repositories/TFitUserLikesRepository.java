package com.talkkia.api.repositories;

import com.talkkia.api.entity.TFit;
import com.talkkia.api.entity.TFitUserLikes;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: Neo4j interface of TFit User Likes
 */
public interface TFitUserLikesRepository extends Neo4jRepository<TFitUserLikes, Long> {

    /**
     * find TFit User Likes By UserId And BeLiked UserId And StepDate
     * @param userId
     * @param beLikedUserId
     * @param stepDate
     * @return
     */
    @Query("MATCH (n:TFitUserLikes) where n.userId = $userId and n.beLikedUserId = $beLikedUserId and n.stepDate = $stepDate RETURN n")
    TFitUserLikes findTFitUserLikesByUserIdAndBeLikedUserIdAndStepDate(@Param("userId") Long userId,@Param("beLikedUserId")  Long beLikedUserId,@Param("stepDate")  String stepDate);

    /**
     * find TFit User Likes By BeLiked UserId And StepDate
     * @param beLikedUserId
     * @param stepDate
     * @return
     */
    @Query("MATCH (n:TFitUserLikes) where n.beLikedUserId = $beLikedUserId and n.stepDate = $stepDate RETURN n")
    List<TFitUserLikes> findTFitUserLikesByBeLikedUserIdAndStepDate(@Param("beLikedUserId") Long beLikedUserId,@Param("stepDate")  String stepDate);
}
