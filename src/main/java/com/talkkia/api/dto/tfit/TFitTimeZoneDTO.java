package com.talkkia.api.dto.tfit;

import lombok.Data;

/**
 * @Author: Ethan
 * @Date: 2024/8/8
 * @Description: current user's longitude and  latitude
 */
@Data
public class TFitTimeZoneDTO {
    /**
     * userId
     */
    private Long userId;
    /**
     * longitude
     */
    private Double longitude;
    /**
     * latitude
     */
    private Double latitude;
}
