package com.talkkia.api.dto.tfit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Author: Ethan
 * @Date: 2024/8/8
 * @Description: Store DTO of TFit
 */
@Data
public class TFitStoreDTO {
    /**
     * Table Id
     */
    private Long id;

    /**
     * User Id
     */
    private Long userId;
    /**
     * stepDate
     */
    private String stepDate;
    /**
     *  Steps
     */
    private Integer steps;

    /**
     * all value or increment
     *
     * 0-All
     * 1-Increment
     */
    private Integer allOrIncrement;
}
