package com.talkkia.api.vo;

import com.talkkia.api.entity.User;
import lombok.Data;

/**
 * @Author: Ethan
 * @Date: 2024/6/23
 * @Description: result in nearby people
 */
@Data
public class UserNearbyVO {
    private User user;
    /**
     * item user distance
     */
    private Double distance;
    /**
     * item user gender
     */
    private Integer gender;
}
