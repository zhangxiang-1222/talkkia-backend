package com.talkkia.api.repositories;

import com.talkkia.api.entity.TFit;
import com.talkkia.api.entity.UserTimeZone;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: Neo4j interface of TFit
 */
public interface TFitRepository extends Neo4jRepository<TFit, Long> {

    /**
     * find TFit By UserId And StepDate
     *
     * @param userId
     * @param stepDate
     * @return
     */
    @Query("MATCH (n:TFit) where n.userId = $userId and n.stepDate = $stepDate RETURN n")
    TFit findTFitByUserIdAndStepDate(@Param("userId") Long userId,@Param("stepDate")  String stepDate);

    @Query("MATCH (n:TFit) where n.userId = $userId RETURN n")
    List<TFit> getMyTFitLogWithin30Days(@Param("userId") Long userId);
}
