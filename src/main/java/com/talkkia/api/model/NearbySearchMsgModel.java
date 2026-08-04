package com.talkkia.api.model;

import lombok.Data;

/**
 * @Author: Ethan
 * @Date: 2024/6/23
 * @Description: User information in the result list
 */
@Data
public class NearbySearchMsgModel {

    private Long userId;
    /**
     * item user distance
     */
    private Double distance;
    /**
     * item user gender
     */
    private Integer gender;

}