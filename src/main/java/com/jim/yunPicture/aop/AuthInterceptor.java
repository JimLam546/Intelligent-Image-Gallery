package com.jim.yunPicture.aop;

import com.jim.yunPicture.annotation.AuthCheck;
import com.jim.yunPicture.entity.enums.UserRoleEnum;
import com.jim.yunPicture.entity.vo.UserVO;
import com.jim.yunPicture.exception.BusinessException;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Jim_Lam
 * @description AuthInterceptor
 */

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        UserVO loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 不需要权限则放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 检查是否有权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户权限异常");
        }
        if (!userRoleEnum.equals(mustRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }

}