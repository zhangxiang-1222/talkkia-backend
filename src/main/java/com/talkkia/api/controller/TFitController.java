package com.talkkia.api.controller;

import com.talkkia.api.dto.tfit.TFitIdentifierDTO;
import com.talkkia.api.dto.tfit.TFitStoreDTO;
import com.talkkia.api.dto.tfit.TFitTimeZoneDTO;
import com.talkkia.api.dto.tfit.TFitUserLikesDTO;
import com.talkkia.api.entity.TFit;
import com.talkkia.api.services.TFitService;
import com.talkkia.api.vo.UserTFitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/8/8
 * @Description: TFit Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/tfit")
public class TFitController {

    @Resource
    private TFitService tFitService;

    @PostMapping("/saveOrUpdateUserTimeZone")
    public ResponseEntity<Boolean> saveOrUpdateUserTimeZone(@RequestBody TFitTimeZoneDTO tFitTimeZoneDTO){
        return ResponseEntity.ok(tFitService.saveOrUpdateUserTimeZone(tFitTimeZoneDTO));
    }

    /**
     * saveOrUpdate TFit data
     * @param tFitStoreDTO
     * @return
     */
    @PostMapping("/saveOrUpdateTFit")
    public ResponseEntity<Boolean> saveOrUpdateTFit(@RequestBody TFitStoreDTO tFitStoreDTO){
        return ResponseEntity.ok(tFitService.saveOrUpdateTFit(tFitStoreDTO));
    }

    /**
     * saveOrUpdate TFit open identifier
     * @param tFitIdentifierDTO
     * @return
     */
    @PostMapping("/saveOrUpdateIdentifier")
    public ResponseEntity<Boolean> saveOrUpdateIdentifier(@RequestBody TFitIdentifierDTO tFitIdentifierDTO){
        return ResponseEntity.ok(tFitService.saveOrUpdateIdentifier(tFitIdentifierDTO));
    }

    /**
     * determine if current user has opened TFit
     * @param userId
     * @return
     */
    @GetMapping("/isOpenTFit")
    public ResponseEntity<Boolean> isOpenTFit(@RequestParam("userId") Long userId){
        return ResponseEntity.ok(tFitService.isOpenTFit(userId));
    }

    /**
     * like or cancel like a user
     * @param tFitUserLikesDTO
     * @return
     */
    @PostMapping("/likeOrCancelLike")
    public ResponseEntity<UserTFitVO> likeOrCancelLike(@RequestBody TFitUserLikesDTO tFitUserLikesDTO){
        return ResponseEntity.ok(tFitService.likeOrCancelLike(tFitUserLikesDTO));
    }

    /**
     * returns the records of the current user in the last 30 days
     * @param userId
     * @return
     */
    @GetMapping("/myStepLog")
    public ResponseEntity<List<TFit>> getMyStepLog(@RequestParam("userId") Long userId){
        log.info("myStepLog access...");
        List<TFit> myStepLogList = new ArrayList<>();
        try {
            myStepLogList = tFitService.getMyStepLog(userId);
        } catch (ParseException e) {
            log.error("error",e);
        }
        return ResponseEntity.ok(myStepLogList);
    }

    /**
     * returns today's steps count rank,based on current user's time zone,must contain current user
     * @param userId
     * @return
     */
    @GetMapping("/todayStepsRank")
    public ResponseEntity<List<UserTFitVO>> todayStepsRank(@RequestParam("userId") Long userId){
        return ResponseEntity.ok(tFitService.getTodayStepsRank(userId));
    }

    /**
     * If user closes TFit,program will reset(to 0) his steps today.
     * @param userId
     * @return
     */
    @GetMapping("/resetTodayStep")
    public ResponseEntity<Boolean> resetTodayStep(@RequestParam("userId") Long userId){
        return ResponseEntity.ok(tFitService.resetTodayStep(userId));
    }
}
