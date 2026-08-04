package com.talkkia.api.services;

import com.talkkia.api.entity.Step;
import com.talkkia.api.repositories.StepRepository;
import com.talkkia.api.repositories.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.talkkia.api.view.StepView;

@Service
public class StepServiceImpl implements StepService {

    @Autowired
    private StepRepository stepRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void createStep(int count, Long userId, boolean status) {
        if (stepRepository.existsBySteptakerId(userId)) {
            Step step = stepRepository.findOneBySteptakerId(userId);
            step.setCount(count);
            step.setStatus(status);
            stepRepository.save(step);
        } else {
            stepRepository.save(new Step(0L, count, status, userRepository.findById(userId).get()));
        }
    }
    
    @Override
    public void updateStep(int count, Long userId) {
            Step step = stepRepository.findOneBySteptakerId(userId);
            step.setCount(count);
            stepRepository.save(step);
    }
    
    @Override
    public void changeStepStatus(Long userId, boolean status) {
            Step step = stepRepository.findOneBySteptakerId(userId);
            step.setStatus(status);
            stepRepository.save(step);
    }

    @Override
    public List<StepView> getAllSteps(Long userId) {
        List<StepView> steps = stepRepository.findAllBySteptakerContactsUserIdAndStatus(userId, true);
        List<Long> steptakers = userRepository.findById(userId).get().getContacts().stream().map(steptaker -> steptaker.getId()).collect(Collectors.toList());
        steps.addAll(stepRepository.findAllStepBySteptakerIdInAndStatus(steptakers, true));
        return steps;
    }

}
