package com.jim.yunPicture.exception;

import com.jim.yunPicture.common.ResultUtil;
import com.jim.yunPicture.common.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 *
 * @author Jim_Lam
 * @description GlobalExceptionHander
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({BusinessException.class})
    public BaseResponse<Object> exceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        // log.error(e.getDescription());
        return ResultUtil.failure(e.getCode(), e.getDescription());
    }

    @ExceptionHandler({RuntimeException.class})
    public BaseResponse<Object> exceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtil.failure(ErrorCode.SYSTEM_ERROR);
    }
}