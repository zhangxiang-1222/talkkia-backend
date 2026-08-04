package com.talkkia.api.model;

import lombok.Data;

/**
 * @Author: Ethan
 * @Date: 2024/6/19
 * @Description: search Result
 */
@Data
public class SearchResultModel {
    private Long userId;
    private Double distance;
    private Integer gender;
}
