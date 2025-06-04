package com.jim.yun_picture.entity.enums;

import lombok.Getter;

/**
 * @author Jim_Lam
 * @description SpaceRoleEnum
 */

@Getter
public enum SpaceRoleEnum {

    VIEWER("浏览者", "viewer"),
    EDITOR("编辑者", "editor"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    SpaceRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static SpaceRoleEnum getEnumByValue(String value) {
        for (SpaceRoleEnum spaceRoleEnum : values()) {
            if (spaceRoleEnum.getValue().equals(value)) {
                return spaceRoleEnum;
            }
        }
        return null;
    }
}
