package com.talkkia.api.repositories;

import com.talkkia.api.entity.InviteAttribution;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface InviteAttributionRepository extends Neo4jRepository<InviteAttribution, String> {

    @Query("MERGE (attribution:InviteAttribution {id: $id}) " +
            "ON CREATE SET attribution.inviteeUserId = $inviteeUserId, " +
            "attribution.inviterUserId = $inviterUserId, " +
            "attribution.context = $context, " +
            "attribution.targetId = $targetId, " +
            "attribution.status = $status, " +
            "attribution.createdAt = $createdAt " +
            "RETURN attribution")
    InviteAttribution mergeApplied(@Param("id") String id,
                                   @Param("inviteeUserId") String inviteeUserId,
                                   @Param("inviterUserId") String inviterUserId,
                                   @Param("context") String context,
                                   @Param("targetId") String targetId,
                                   @Param("status") String status,
                                   @Param("createdAt") String createdAt);
}
