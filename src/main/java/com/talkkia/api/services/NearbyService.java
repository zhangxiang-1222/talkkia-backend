package com.talkkia.api.services;

import com.talkkia.api.dto.nearby.NearbySearchDTO;
import com.talkkia.api.vo.UserNearbyVO;

import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/6/12
 * @Description: nearby feature Service
 */
public interface NearbyService {

    /**
     * Get data within the specified coordinate range(Need to exclude inaccurate data) and
     * Sort the retrieved data and return a data list to the frontend
     *
     * <p>
     * Additional operation:
     * 1、When searching for others, filter users within a specified distance based on gender.
     * 2、When searched, set it such that only users of a specified gender can search for you.
     */
    List<UserNearbyVO> getAccurateDataWithinRange(Long userId);

    /**
     * Filter users who meet the distance and gender requirements
     *  Deprecated because handle by frontend
     * @param nearbySearchDTO
     * @return
     */
    @Deprecated
    List<UserNearbyVO> filterUserByGenderAndDistance(NearbySearchDTO nearbySearchDTO);

    /**
     * Store location data in Redis
     *
     * @param longitude
     * @param latitude
     * @param userId
     */
    Boolean storeLocationData(Double longitude, Double latitude, Long userId);

    /**
     * Deleting data in Redis
     *
     * @param userId
     */
    Boolean deleteData(Long userId);

    /**
     * Set who can search you by Nearby feature
     *
     * @param userId
     * @param gender
     * @return
     */
    Boolean setWhoCanSeeYou(Long userId,Integer gender);

}
