package com.talkkia.api.repositories;

import com.talkkia.api.entity.Step;
import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import com.talkkia.api.view.StepView;

public interface StepRepository extends Neo4jRepository<Step, Long> {

    public boolean existsBySteptakerMobile(String mobile);

    public Step findOneBySteptakerMobile(String mobile);

    //@Query("MATCH (User {mobile:$mobile})-[:CONTACT]-(n:User)-[:STEPTAKER]-(s:Step {status:true}) RETURN s.count AS count")
    public List<StepView> findAllBySteptakerContactsUserMobileAndStatus(String mobile, boolean status);

    public boolean existsBySteptakerId(Long userId);

    public Step findOneBySteptakerId(Long userId);

    public List<StepView> findAllBySteptakerIdAndStatus(Long userId, boolean status);
    
    List<StepView> findAllStepBySteptakerIdInAndStatus(List<Long> steptakers, boolean status);

    public List<StepView> findAllBySteptakerContactsUserIdAndStatus(Long userId, boolean b);
}
