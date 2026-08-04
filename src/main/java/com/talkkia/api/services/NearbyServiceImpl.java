package com.talkkia.api.services;

import com.talkkia.api.constants.NearbyConstant;
import com.talkkia.api.dto.nearby.NearbySearchDTO;
import com.talkkia.api.model.NearbySearchFilterModel;
import com.talkkia.api.model.NearbySearchMsgModel;
import com.talkkia.api.entity.User;
import com.talkkia.api.enums.GenderEnum;
import com.talkkia.api.model.SearchResultModel;
import com.talkkia.api.utils.RedisCommonUtil;
import com.talkkia.api.utils.RedisGeoUtil;
import com.talkkia.api.vo.UserNearbyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/6/12
 * @Description: nearby feature ServiceImpl
 */
@Service
@Slf4j
public class NearbyServiceImpl implements NearbyService {

    @Resource
    private RedisGeoUtil redisGeoUtil;

    @Resource
    private RedisCommonUtil redisCommonUtil;

    @Resource
    private UserService userService;

    /**
     * Get data within the specified coordinate range(Need to exclude inaccurate
     * data) and
     * Sort the retrieved data and return a data list to the frontend
     *
     * <p>
     * Additional operation:
     * 1、When searching for others, filter users within a specified distance based
     * on gender.
     * 2、When searched, set it such that only users of a specified gender can search
     * for you.
     *
     * @param userId
     * @return
     */
    @Override
    public List<UserNearbyVO> getAccurateDataWithinRange(Long userId) {
        // Based on the userId, find location information within a specified radius.
        List<SearchResultModel> locationList = redisGeoUtil.findNearbyPeople(NearbyConstant.REDIS_NEARBY_GEO_KEY,
                userId, NearbyConstant.MAXIMUM_DISTANCE);
        // The object returned by the method.
        List<UserNearbyVO> userList = new ArrayList<>();
        // Retrieve the userId from the locationList and populate the user entity.
        User currentUser = userService.getUser(userId);
        if (currentUser == null) {
            log.warn("Current user not found: {}", userId);
            return userList;
        }
        // Determine the gender of the current user. Male or Female
        String genderDesc = currentUser.getGender();
        Integer genderCode = GenderEnum.getGenderCode(genderDesc);
        for (SearchResultModel location : locationList) {
            // Determine the restriction conditions for each element (whether it is
            // specified to be searchable only by a certain gender; if not, it means there
            // is no restriction)
            Long itemUserId = Long.valueOf(location.getUserId());
            // Compare the current user with the current loop element to determine if it
            // meets the search conditions of the current loop element
            String cacheKey = NearbyConstant.REDIS_NEARBY_GENDER_KEY + itemUserId;
            Object itemGenderCodeObj = redisCommonUtil.get(cacheKey);
            if (itemGenderCodeObj == null || GenderEnum.BOTH.getCode().equals(((Integer) itemGenderCodeObj))) {
                // Indicates no restriction, allowing both male and female users to search for
                // this user.
                User user = userService.getUser(itemUserId);
                if (user == null) {
                    log.warn("Nearby user not found, skipping: {}", itemUserId);
                    continue;
                }
                user.setContacts(new ArrayList<>());
                UserNearbyVO userNearbyVO = new UserNearbyVO();
                userNearbyVO.setUser(user);
                userNearbyVO.setDistance(location.getDistance());
                userNearbyVO.setGender(GenderEnum.getGenderCode(user.getGender()));
                userList.add(userNearbyVO);
                // The current user is set to be searchable by both males and females.
            } else {
                Integer itemGenderCode = (Integer) itemGenderCodeObj;
                if (genderCode.equals(itemGenderCode)) {
                    /**
                     * Only users who meet the defined search
                     * conditions (e.g., I can only be searched by males)
                     * can be added to the collection.
                     */
                    User user = userService.getUser(itemUserId);
                    if (user == null) {
                        log.warn("Nearby user not found, skipping: {}", itemUserId);
                        continue;
                    }
                    user.setContacts(new ArrayList<>());
                    UserNearbyVO userNearbyVO = new UserNearbyVO();
                    userNearbyVO.setUser(user);
                    userNearbyVO.setDistance(location.getDistance());
                    userNearbyVO.setGender(GenderEnum.getGenderCode(user.getGender()));
                    userList.add(userNearbyVO);
                }
            }
        }
        return userList;
    }

    /**
     * Store location data in Redis
     *
     * @param longitude
     * @param latitude
     * @param userId
     */
    @Override
    public Boolean storeLocationData(Double longitude, Double latitude, Long userId) {
        return redisGeoUtil.addGeoPoint(NearbyConstant.REDIS_NEARBY_GEO_KEY, longitude, latitude, userId);
    }

    /**
     * Deleting data in Redis
     *
     * @param userId
     */
    @Override
    public Boolean deleteData(Long userId) {
        try {
            redisGeoUtil.deleteGeoLocation(NearbyConstant.REDIS_NEARBY_GEO_KEY, userId);
            redisCommonUtil.del(NearbyConstant.REDIS_NEARBY_GENDER_KEY + userId);
            return true;
        } catch (Exception e) {
            log.error("error", e);
        }
        return false;
    }

    /**
     * Set who can search you by Nearby feature
     *
     * @param userId
     * @param gender
     * @return
     */
    @Override
    public Boolean setWhoCanSeeYou(Long userId, Integer gender) {
        return redisCommonUtil.set(NearbyConstant.REDIS_NEARBY_GENDER_KEY + userId, gender);
    }

    /**
     * Filter users who meet the distance and gender requirements
     * Deprecated because handle by frontend
     *
     * @param nearbySearchDTO
     * @return
     */
    @Override
    @Deprecated
    public List<UserNearbyVO> filterUserByGenderAndDistance(NearbySearchDTO nearbySearchDTO) {

        List<NearbySearchMsgModel> nearbySearchMsgModelList = nearbySearchDTO.getNearbySearchMsgModelList();

        NearbySearchFilterModel nearbySearchFilterModel = nearbySearchDTO.getNearbySearchFilterModel();

        /**
         * result list
         */
        List<UserNearbyVO> userList = new ArrayList<>();
        /**
         * In the query condition: the minimum distance
         */
        Integer queryMinDistance = nearbySearchFilterModel.getMinDistance();
        /**
         * In the query condition: the maximum distance
         */
        Integer queryMaxDistance = nearbySearchFilterModel.getMaxDistance();
        /**
         * In the query condition: specified gender
         */
        Integer queryGender = nearbySearchFilterModel.getGender();
        for (NearbySearchMsgModel nearbySearchMsgModel : nearbySearchMsgModelList) {
            /**
             * item userId
             */
            Long itemUserId = nearbySearchMsgModel.getUserId();
            /**
             * item user distance
             */
            Double itemDistance = nearbySearchMsgModel.getDistance();
            /**
             * item user gender
             */
            Integer itemGender = nearbySearchMsgModel.getGender();
            if (itemDistance != null && itemDistance >= queryMinDistance && itemDistance <= queryMaxDistance) {
                if (itemGender != null && itemGender.equals(queryGender)) {
                    User user = userService.getUser(itemUserId);
                    UserNearbyVO userNearbyVO = new UserNearbyVO();
                    userNearbyVO.setUser(user);
                    userNearbyVO.setDistance(itemDistance);
                    userNearbyVO.setGender(GenderEnum.getGenderCode(user.getGender()));
                    userList.add(userNearbyVO);
                }
            }
        }
        return userList;
    }
}
