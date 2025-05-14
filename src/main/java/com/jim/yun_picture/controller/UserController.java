package com.jim.yun_picture.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.jim.yun_picture.annotation.AuthCheck;
import com.jim.yun_picture.common.BaseResponse;
import com.jim.yun_picture.common.ResultUtil;
import com.jim.yun_picture.entity.User;
import com.jim.yun_picture.entity.request.*;
import com.jim.yun_picture.entity.vo.UserVO;
import com.jim.yun_picture.exception.BusinessException;
import com.jim.yun_picture.exception.ErrorCode;
import com.jim.yun_picture.exception.ThrowUtils;
import com.jim.yun_picture.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.jim.yun_picture.constant.UserConstant.*;


/**
 * @author Jim_Lam
 * @description UserController
 */

@RestController
@RequestMapping("/user")
@Api(tags = "用户模块")
@ApiSupport(author = "JimLam")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册请求
     * @return 用户id
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        Long userId = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtil.success(userId);
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录请求参数
     * @param request 会话请求
     * @return 脱敏后的用户数据
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        UserVO userVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtil.success(userVO);
    }

    /**
     * 获取当前登录用户信息
     * @param request 会话请求
     * @return 脱敏后的用户信息
     */
    @GetMapping("/get/current")
    @ApiOperation(value = "获取当前登录用户信息")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(attribute == null, ErrorCode.NOT_LOGIN_ERROR);
        UserVO userVO = userService.getLoginUser(request);
        return ResultUtil.success(userVO);
    }

    /**
     * 用户注销
     * @param request 会话亲够
     * @return 注销操作结果
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(true);
    }

    /**
     * 创建用户（管理员）
     * @param userAddRequest 添加用户请求参数
     * @return 用户id
     */
    @PostMapping("/addUser")
    @AuthCheck(mustRole = ADMIN_ROLE) // 会验证是否已经登录
    @ApiOperation(value = "创建用户（管理员）")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        if (StrUtil.isBlank(userAddRequest.getUserPassword())) {
            // 默认密码
            final String DEFAULT_PASSWORD = "123456789";
            userAddRequest.setUserPassword(DEFAULT_PASSWORD);
        }
        String encryptPassword = userService.getEncryptPassword(userAddRequest.getUserPassword());
        userAddRequest.setUserPassword(encryptPassword);
        User user = BeanUtil.copyProperties(userAddRequest, User.class);
        user.setUserRole(USER_ROLE);
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(user.getId());
    }

    /**
     * 删除用户
     * @param userDeleteRequest 用户 id
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = ADMIN_ROLE)
    @ApiOperation(value = "删除用户")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest userDeleteRequest) {
        ThrowUtils.throwIf(userDeleteRequest == null, ErrorCode.PARAMS_ERROR);
        if (userDeleteRequest.getId() == null || userDeleteRequest.getId() < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(userDeleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(true);
    }

    /**
     * 更新用户
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = ADMIN_ROLE)
    @ApiOperation(value = "更新用户信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User user = BeanUtil.copyProperties(userUpdateRequest, User.class);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(true);
    }

    /**
     * 根据 id 获取用户（未脱敏）
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = ADMIN_ROLE)
    @ApiOperation(value = "获取用户信息")
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id == null || id < 1, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtil.success(user);
    }

    /**
     * 根据 id 获取用户（脱敏）
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "获取用户信息VO")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id < 1, ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.getUserVOById(id, request);
        return ResultUtil.success(userVO);
    }

    /**
     * 用户用户列信息（未脱敏）
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = ADMIN_ROLE)
    @ApiOperation(value = "分页获取用户列表（管理员）")
    public BaseResponse<Page<UserVO>> userListByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        QueryWrapper<User> queryWrapper = userService.getQueryWrapper(userQueryRequest);
        Page<User> page = userService
                .page(new Page<>(current, pageSize)
                        , queryWrapper);
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setCurrent(current);
        userVOPage.setSize(pageSize);
        List<UserVO> userVOList = userService.getUserVOList(page.getRecords());
        BeanUtil.copyProperties(page, userVOPage);
        userVOPage.setRecords(userVOList);
        return ResultUtil.success(userVOPage);
    }

}