package com.talkkia.api.services;

import com.talkkia.api.entity.BusinessAccount;
import com.talkkia.api.repositories.BusinessAccountRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.BusinessAccountView;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessAccountServiceImpl implements BusinessAccountService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessAccountRepository businessAccountRepository;

    @Override
    public BusinessAccountView createBusinessAccount(Long userId, BusinessAccount businessAccount) {
        businessAccount.setId(ThreadLocalRandom.current().nextLong(10000000, 99999999));
        if (businessAccountRepository.existsById(businessAccount.getId())) {
            createBusinessAccount(userId, businessAccount);
        }
        businessAccount.setOwner(userRepository.findById(userId).get());
        return businessAccountRepository.findOneById(businessAccountRepository.save(businessAccount).getId());
                //"your business account '" + businessAccount.getTitle() + "' has been created";
    }

    @Override
    public String addBusinessAccountOperator(Long ownerId, Long operatorId, Long businessAccountId) {
        BusinessAccount ba = businessAccountRepository.findById(businessAccountId).get();
        ba.getOperators().add(userRepository.findById(operatorId).get());
        businessAccountRepository.save(ba);
        return operatorId + " has been added as operator to your business account " + businessAccountId;
    }

    @Override
    public String removeBusinessAccountOperator(Long ownerId, Long operatorId, Long businessAccountId) {
        if (businessAccountRepository.existsByIdAndOwnerId(businessAccountId, ownerId)) {
            businessAccountRepository.deleteOperator(businessAccountId, operatorId);
            return operatorId + " has beed removed as operator.";
        }
        return "error!";
    }
    
    @Override
    public String followBusinessAccount(Long userId, Long businessAccountId) {
            BusinessAccount ba = businessAccountRepository.findById(businessAccountId).get();
            if(ba.getFollowers().contains(userRepository.findById(userId).get())){
                businessAccountRepository.deleteFollower(userId, businessAccountId);
                return businessAccountId + " has beed unfollowed.";
            }else{
            ba.getFollowers().add(userRepository.findById(userId).get());
            businessAccountRepository.save(ba);
            return businessAccountId + " has beed followed.";
            }
    }

    @Override
    public BusinessAccountView getBusinessAccount(Long userId, Long businessAccountId) {
        return businessAccountRepository.findOneById(businessAccountId);
    }
}
