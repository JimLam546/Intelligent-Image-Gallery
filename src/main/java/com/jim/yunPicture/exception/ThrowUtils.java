package com.jim.yunPicture.exception;

/**
 * @author Jim_Lam
 * @description ThrowUtils
 */

public class ThrowUtils {

    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw new RuntimeException();
        }
    }

    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String description) {
        if (condition) {
            throw new BusinessException(errorCode, description);
        }
    }
}