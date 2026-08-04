package com.talkkia.api.dto.nearby;

import lombok.Data;
import lombok.ToString;

/**
 * @Author: Ethan
 * @Date: 2024/6/17
 * @Description: store Neaderby People Data
 */
@Data
@ToString
public class NearbyStoreDTO {
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
