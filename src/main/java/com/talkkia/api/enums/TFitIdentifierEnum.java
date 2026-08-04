package com.talkkia.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description:
 */
@Getter
@AllArgsConstructor
@ToString
public enum TFitIdentifierEnum {
    CLOSED(0,"CLOSED"),
    OPEN(1,"OPEN");

    private Integer code;

    private String desc;
}
