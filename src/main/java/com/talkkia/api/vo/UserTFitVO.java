package com.talkkia.api.vo;

import lombok.Data;

/**
 * @Author: Ethan
 * @Date: 2024/8/12
 * @Description:  result in TFit(today rank)
 */
@Data
public class UserTFitVO {

    private Long userId;
    private String userName;
    /**
     * avatar
     */
    private String picture;
    private Integer steps;
    private Integer likes;
    // if current user likes his contact's TFit
    private Boolean isLike;
}
