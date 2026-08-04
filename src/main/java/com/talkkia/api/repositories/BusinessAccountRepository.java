package com.talkkia.api.repositories;

import com.talkkia.api.entity.BusinessAccount;
import com.talkkia.api.view.BusinessAccountView;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface BusinessAccountRepository extends Neo4jRepository<BusinessAccount, Long> {

    @Query("MATCH(:User {id: $operatorId})-[o:OPERATOR]-(:BusinessAccount {id:$businessAccountId}) DETACH DELETE o")
    public void deleteOperator(Long operatorId, Long businessAccountId);
    //@Query("MATCH(u:User {mobile: $mobile})-[m:MEMBER]-(g:Group {id:$groupid}) DETACH DELETE m")
    //public String leaveGroup(String mobile, Long groupid);

    public boolean existsByIdAndOwnerId(Long businessAccountId, Long userId1);

    @Query("MATCH(:User {id: $userId})-[f:FOLLOWER]-(:BusinessAccount {id:$businessAccountId}) DETACH DELETE f")
    public void deleteFollower(Long userId, Long businessAccountId);

    public BusinessAccountView findOneById(Long businessAccountId);
}
