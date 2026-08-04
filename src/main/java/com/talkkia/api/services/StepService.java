package com.talkkia.api.services;

import java.util.List;
import com.talkkia.api.view.StepView;

public interface StepService {
    
    public void createStep(int count, Long userId, boolean status);
    
    public List<StepView> getAllSteps(Long userId);

    public void updateStep(int count, Long userId);

    public void changeStepStatus(Long userId, boolean status);
    
}
