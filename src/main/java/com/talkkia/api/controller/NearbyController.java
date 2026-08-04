package com.talkkia.api.controller;

import com.talkkia.api.dto.nearby.NearbyStoreDTO;
import com.talkkia.api.services.NearbyService;
import com.talkkia.api.vo.UserNearbyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/6/16
 * @Description: nearby Controller
 */
@RestController
@RequestMapping("/api/nearby")
public class NearbyController {

    @Autowired
    private NearbyService nearbyService;


    /**
     * Get data within the specified coordinate range(Need to exclude inaccurate data) and
     * Sort the retrieved data and return a data list to the frontend
     *
     * <p>
     * Additional operation:
     * 1、When searching for others, filter users within a specified distance based on gender.
     * 2、When searched, set it such that only users of a specified gender can search for you.
     *
     * @param userId
     * @return
     */
    @RequestMapping("/list")
    public ResponseEntity<List<UserNearbyVO>> list(@RequestParam Long userId) {
        // TODO Retrieve the userId from the token, do not obtain it from the request.
        Long startTime = System.currentTimeMillis();
        List<UserNearbyVO> accurateDataWithinRange = nearbyService.getAccurateDataWithinRange(userId);
        Long endTime = System.currentTimeMillis();
        System.out.println("list spent time:"+(endTime-startTime) + "ms");
        return ResponseEntity.ok(accurateDataWithinRange);
    }

    /**
     * Store location data in Redis
     *
     * @param nearbyStoreDTO
     * @return
     */
    @RequestMapping("/save")
    public ResponseEntity<Boolean> save(@RequestBody NearbyStoreDTO nearbyStoreDTO) {
        Long startTime = System.currentTimeMillis();
        Boolean isStoreLocation = nearbyService.storeLocationData(nearbyStoreDTO.getLongitude(), nearbyStoreDTO.getLatitude(), nearbyStoreDTO.getUserId());
        Long endTime = System.currentTimeMillis();
        System.out.println("save spent time:"+(endTime-startTime) + "ms");
        return ResponseEntity.ok(isStoreLocation);
    }

    /**
     * Deleting data in Redis
     *
     * @param nearbyStoreDTO
     * @return
     */
    @RequestMapping("/delete")
    public ResponseEntity<Boolean> delete(@RequestBody NearbyStoreDTO nearbyStoreDTO) {
        return ResponseEntity.ok(nearbyService.deleteData(nearbyStoreDTO.getUserId()));
    }

    @RequestMapping("/whoCanSeeYou")
    public ResponseEntity<Boolean> whoCanSeeYou(@RequestParam Long userId,
                                                @RequestParam Integer gender) {
        return ResponseEntity.ok(nearbyService.setWhoCanSeeYou(userId, gender));
    }

//    /**
//     * 从 Nearby 发送好友请求
//     * 记录申请次数，避免重复申请同一人
//     *
//     * @param userId       申请者用户 ID
//     * @param targetUserId 目标用户 ID
//     * @return NearbyRequestVO 包含申请结果和今日统计信息
//     */
//    @PostMapping("/sendRequest")
//    public ResponseEntity<NearbyRequestVO> sendNearbyRequest(
//            @RequestParam Long userId,
//            @RequestParam Long targetUserId) {
//        System.out.println("用户 " + userId + " 向用户 " + targetUserId + " 发送 Nearby 好友申请");
//        NearbyRequestVO result = nearbyRequestService.recordRequest(userId, targetUserId);
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * 获取用户今天的申请统计信息
//     * 客户端可调用此接口获取当前已用次数和剩余次数
//     *
//     * @param userId 用户 ID
//     * @return NearbyRequestVO 包含今日统计信息
//     */
//    @GetMapping("/requestStats")
//    public ResponseEntity<NearbyRequestVO> getRequestStats(@RequestParam Long userId) {
//        NearbyRequestVO result = nearbyRequestService.getRequestStats(userId);
//        return ResponseEntity.ok(result);
//    }
//
//    /**
//     * 检查今天是否已经申请过指定用户
//     *
//     * @param userId       申请者用户 ID
//     * @param targetUserId 目标用户 ID
//     * @return true 如果已申请过，false 如果未申请过
//     */
//    @GetMapping("/hasRequested")
//    public ResponseEntity<Boolean> hasRequestedToday(
//            @RequestParam Long userId,
//            @RequestParam Long targetUserId) {
//        boolean hasRequested = nearbyRequestService.hasRequestedToday(userId, targetUserId);
//        return ResponseEntity.ok(hasRequested);
//    }
}
