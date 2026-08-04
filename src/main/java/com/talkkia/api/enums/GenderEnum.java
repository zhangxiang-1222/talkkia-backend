package com.talkkia.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Ethan
 * @Date: 2024/6/18
 * @Description: Gender Enum In Nearby
 */
@Getter
@AllArgsConstructor
@ToString
public enum GenderEnum {

    MALE(1, "Male"),
    FEMALE(2, "Female"),
    /**
     * The user can be searchable by both genders.
     */
    BOTH(3, "Both");

    private static Map<String, Integer> map;
    /**
     * gender code
     */
    private Integer code;

    /**
     * gender desc
     */
    private String desc;

    public static Integer getGenderCode(String genderDesc) {
        if (map == null || map.isEmpty()) {
            GenderEnum[] genderEnums = values();
            map = new HashMap<>();
            for (GenderEnum genderEnum : genderEnums) {
                map.put(genderEnum.getDesc(), genderEnum.getCode());
            }
        }
        return map.get(genderDesc);
    }
}
