package com.talkkia.api.dto.tfit;

import lombok.Data;

/**
 * @Author: Ethan
 * @Date: 2024/8/8
 * @Description: user likes
 */
@Data
public class TFitUserLikesDTO {
    /**
     * User Id
     */
    private Long currentUserId;

    /**
     * Be Liked User Id
     */
    private Long userIdLikedByCurrentUserId;
}
