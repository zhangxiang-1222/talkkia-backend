package com.talkkia.api.repositories;

import com.talkkia.api.entity.User;
import com.talkkia.api.view.CompleteUserView;
import com.talkkia.api.view.ContactDiscoveryMatchView;
import com.talkkia.api.view.UserDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import com.talkkia.api.view.UserView;
import org.springframework.data.repository.query.Param;

//all the extra comments and code should be removed on release.

public interface UserRepository extends Neo4jRepository<User, Long> {

    //@Query("OPTIONAL MATCH (user:User{mobile:'$mobile'}) RETURN user IS NOT NULL AS exist")
    public Boolean existsByMobile(String mobile);

    public Boolean existsByEmail(String email);
    
    public Optional<CompleteUserView> findByMobile(String mobile);

    public Optional<UserView> findOneById(Long userId);

    public Optional<UserView> findOneByMobile(String mobile);

    @Query("MATCH (matched:User) " +
            "WHERE matched.mobile IN $mobiles AND matched.id <> $ownerId " +
            "RETURN matched.id AS id, matched.mobile AS mobile, matched.name AS name, " +
            "matched.picture AS picture, " +
            "exists((:User {id: $ownerId})-[:CONTACT]-(matched)) AS contact")
    List<ContactDiscoveryMatchView> findContactDiscoveryMatches(
            @Param("ownerId") Long ownerId,
            @Param("mobiles") List<String> mobiles
    );

    public Optional<UserView> findOneByEmail(String email);

    @Query("MATCH (u:User) " +
            "WHERE toLower(trim(u.email)) = toLower(trim($email)) " +
            "WITH u, " +
            "CASE " +
            "WHEN u.registrationDate IS NULL OR trim(u.registrationDate) = '' OR size(split(u.registrationDate, '/')) <> 3 " +
            "THEN date('9999-12-31') " +
            "ELSE date({day: toInteger(split(u.registrationDate, '/')[0]), month: toInteger(split(u.registrationDate, '/')[1]), year: toInteger(split(u.registrationDate, '/')[2])}) " +
            "END AS registrationDateValue " +
            "RETURN u " +
            "ORDER BY registrationDateValue ASC, u.id ASC " +
            "LIMIT 1")
    public Optional<User> findByEmail(String email);

    public Optional<User> findByFirebaseUid(String firebaseUid);

    @Query("MATCH (u:User {id: $userId}) " +
            "SET u.firebaseUid = $firebaseUid, " +
            "u.socialProvider = $socialProvider, " +
            "u.providerUserId = $providerUserId")
    public void updateSocialLoginIdentity(@Param("userId") Long userId,
                                          @Param("firebaseUid") String firebaseUid,
                                          @Param("socialProvider") String socialProvider,
                                          @Param("providerUserId") String providerUserId);

    @Query("MATCH (u:User) WHERE u.id IN $userIds RETURN u")
    public List<User> findDropListUsers(@Param("userIds") List<Long> userIds);

    @Query("MATCH (u:User) WHERE toLower(u.email) = toLower($email) AND coalesce(toLower(u.socialProvider), '') = coalesce(toLower($provider), '') RETURN count(u) > 0")
    public Boolean existsByEmailAndProvider(@Param("email") String email, @Param("provider") String provider);

    @Query("MATCH (u:User) WHERE toLower(u.email) = toLower($email) AND coalesce(toLower(u.socialProvider), '') = coalesce(toLower($provider), '') RETURN u LIMIT 1")
    public Optional<User> findByEmailAndProvider(@Param("email") String email, @Param("provider") String provider);
    
    public Optional<List<UserView>> findAllById();

    //public List<User> findAllByMobile(String mobile);

    //MATCH p=(n: User{id:7986475390})-[r:CONTACT]->() DELETE r
    @Query("MATCH(:User {id:$userId1})-[c:CONTACT]-(:User { id:$userId2}) DELETE c")
    public void deleteContact(@Param("userId1") long userId1,@Param("userId2") long userId2);

    public List<UserDTO> findAllByContactsUserId(long userId);
    
    public Optional<User> findByIdAndContactsUserId(long userId1, long userId2);
    
    @Query("MATCH(:User {id: $userId})-[c:CONTACT]-(:User) RETURN c")
    public List<UserView> findAllById(long userId);

    //@Query("MATCH (user:User {mobile:$mobile})-[c:CONTACT]-(n:User) RETURN n")
    //public List<User> findAllByContactsMobile(String mobile);

    public List<User> findAllByMobileAndContacts(String mobile, String contact);

    @Query("RETURN exists((:User {id:$userId1})-[:CONTACT]-(:User {id:$userId2}))")
    public boolean contactExists(long userId1, long userId2);

    @Query("MATCH (invitee:User {id: $inviteeUserId}), (inviter:User {id: $inviterUserId}) " +
            "MERGE (invitee)-[contact:CONTACT]-(inviter) " +
            "ON CREATE SET contact.hidemypost = false, contact.hidetheirpost = false")
    public void mergeInviteContact(@Param("inviteeUserId") Long inviteeUserId,
                                   @Param("inviterUserId") Long inviterUserId);

    @Query("OPTIONAL MATCH(user1:User {id:$userId1}), (user2:User {id:$userId2}) CREATE (user1)-[:CONTACT]->(user2)")
    public void addContact(long userId1, long userId2);
    
    @Query("OPTIONAL MATCH(user1:User {id:$userId1}), (user2:User {id:$userId2}) CREATE (user1)<-[:HIDEFROM]-(user2)")
    public void hideMyPost(long userId1, long userId2);
    
    @Query("OPTIONAL MATCH(user1:User {id:$userId1}), (user2:User {id:$userId2}) CREATE (user1)<-[:HIDETHEIR]-(user2)")
    public void hideTheirPost(long userId1, long userId2);

    public Optional<CompleteUserView> findUserById(Long userId);
}
