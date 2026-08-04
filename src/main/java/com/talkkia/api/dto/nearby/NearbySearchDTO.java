package com.talkkia.api.dto.nearby;

import com.talkkia.api.model.NearbySearchFilterModel;
import com.talkkia.api.model.NearbySearchMsgModel;
import lombok.Data;

import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/6/23
 * @Description: Filter by gender and distance
 */
@Data
public class NearbySearchDTO {
    /**
     * filter condition
     */
    private NearbySearchFilterModel nearbySearchFilterModel;
    /**
     * user list msg
     */
    private List<NearbySearchMsgModel> nearbySearchMsgModelList;
}
