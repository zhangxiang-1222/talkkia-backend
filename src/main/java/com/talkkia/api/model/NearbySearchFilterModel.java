package com.talkkia.api.model;

import lombok.Data;

/**
 * @Author: Ethan
 * @Date: 2024/6/18
 * @Description: Retrieve a DTO for nearby people based on specified conditions.
 */
@Data
public class NearbySearchFilterModel {
    /**
     * userId
     */
    private Long userId;
    /**
     * The maximum position that a user can retrieve
     */
    private Integer minDistance;
    /**
     * The minimum position that a user can retrieve
     */
    private Integer maxDistance;
    /**
     * The gender of the user that can be retrieved
     */
    private Integer gender;
}
