package com.jim.yunPicture.entity.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @author Jim_Lam
 * @description UserRoleEnum
 */

@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }

        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }

}