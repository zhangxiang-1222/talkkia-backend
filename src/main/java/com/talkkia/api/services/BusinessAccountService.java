package com.talkkia.api.services;

import com.talkkia.api.entity.BusinessAccount;
import com.talkkia.api.view.BusinessAccountView;

public interface BusinessAccountService {
    public BusinessAccountView createBusinessAccount(Long userId, BusinessAccount businessAccount);
    
    public String addBusinessAccountOperator(Long ownerId, Long operatorId, Long businessAccountId);
    
    public String removeBusinessAccountOperator(Long ownerId, Long operatorId, Long businessAccountId);

    public BusinessAccountView getBusinessAccount(Long userId, Long businessAccountId);
    
    public String followBusinessAccount(Long userId, Long businessAccountId);
}
