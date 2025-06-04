package com.jim.yun_picture.entity.enums;

import lombok.Getter;

/**
 * @author Jim_Lam
 * @description SpaceTypeEnum
 */

@Getter
public enum SpaceTypeEnum {

    PRIVATE("私有空间", 0),

    TEAM("团队空间", 1);

    private final String text;

    private final Integer value;

    SpaceTypeEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    public static SpaceTypeEnum getSpaceTypeEnum(Integer value) {
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.value.equals(value)) {
                return spaceTypeEnum;
            }
        }
        return null;
    }
}