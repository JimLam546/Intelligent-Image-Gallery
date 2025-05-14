package com.jim.yun_picture.common;

import com.jim.yun_picture.exception.ErrorCode;

/**
 * @author Jim_Lam
 * @description ResultUtil
 */

public class ResultUtil {
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data);
    }

    public static <T> BaseResponse<T> failure(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static <T> BaseResponse<T> failure(ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode, description);
    }

    public static <T> BaseResponse<T> failure(int code, String description) {
        return new BaseResponse<>(code, description);
    }
}