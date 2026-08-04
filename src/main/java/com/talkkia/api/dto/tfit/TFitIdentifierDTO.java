package com.talkkia.api.dto.tfit;

import lombok.Data;

/**
 * @Author: Ethan
 * @Date: 2024/8/8
 * @Description: Stores the flag of whether the user has enabled the TFIT function
 */
@Data
public class TFitIdentifierDTO {
    /**
     * Table Id
     */
    private Long id;

    /**
     * User Id
     */
    private Long userId;

    /**
     *  0-closed 1-opened
     */
    private Integer isOpen;
}
