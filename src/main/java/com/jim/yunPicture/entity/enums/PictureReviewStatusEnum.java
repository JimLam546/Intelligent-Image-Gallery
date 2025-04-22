package com.jim.yunPicture.entity.enums;

import lombok.Getter;

/**
 * @author Jim_Lam
 * @description PictureReviewStatusEnum
 */

@Getter
public enum PictureReviewStatusEnum {
    REVIEWING(0, "待审核"),
    PASS(1, "已审核"),
    REFUSE(2, "审核不通过");

    private final int value;
    private final String text;

    PictureReviewStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PictureReviewStatusEnum statusEnum : values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}