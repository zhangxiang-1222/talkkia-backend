package com.talkkia.api.repositories;

import com.talkkia.api.entity.Group;
import com.talkkia.api.entity.User;
import com.talkkia.api.relationship.Type;
import com.talkkia.api.view.GroupView;
import com.talkkia.api.view.GroupViewShort;
import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface GroupRepository extends Neo4jRepository<Group, Long> {
    @Query("MATCH(:User {id: $memberId})-[m:MEMBER]-(:Group {id:$groupid}) DETACH DELETE m")
    public void leaveGroup(Long memberId, Long groupid);
    
    @Query("MATCH(:User {id:$adminId})-[a:ADMIN]-(:Group {id:$groupId}) DETACH DELETE a")
    public void removeGroupAdmin(Long adminId, Long groupId);

    public Optional<Group> findOneByIdAndMembersTypeAndMembersUserId(Long groupId, Type type, Long ownerId);

    public Optional<GroupView> findOneById(Long id);
    
    public Optional<GroupViewShort> findGroupById(Long id);

    public List<GroupView> findAllByMembersUserId(Long userId);
}
