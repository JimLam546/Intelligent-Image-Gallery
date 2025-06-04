package com.jim.yun_picture.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jim.yun_picture.common.BaseResponse;
import com.jim.yun_picture.common.ResultUtil;
import com.jim.yun_picture.entity.SpaceUser;
import com.jim.yun_picture.entity.request.DeleteRequest;
import com.jim.yun_picture.entity.request.SpaceUserAddRequest;
import com.jim.yun_picture.entity.request.SpaceUserEditRequest;
import com.jim.yun_picture.entity.request.SpaceUserQueryRequest;
import com.jim.yun_picture.entity.vo.SpaceUserVO;
import com.jim.yun_picture.exception.ErrorCode;
import com.jim.yun_picture.exception.ThrowUtils;
import com.jim.yun_picture.service.SpaceUserService;
import com.jim.yun_picture.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Jim_Lam
 * @description SpaceUserController
 */

@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 添加空间用户
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceAddRequest, HttpServletRequest request) {
        // 检查是否登录
        userService.getLoginUser(request);
        Long spaceUserId = spaceUserService.addSpaceUser(spaceAddRequest);
        return ResultUtil.success(spaceUserId);
    }

    /**
     * 从空间删除成员
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 检查是否登录
        userService.getLoginUser(request);
        ThrowUtils.throwIf(deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        SpaceUser oldSpaceUser = spaceUserService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR, "空间用户不存在");
        boolean result = spaceUserService.removeById(deleteRequest.getId());
        return ResultUtil.success(result);
    }

    /**
     * 查询某个成员在某个空间的信息
     * @param spaceUserQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/get")
    public BaseResponse<Boolean> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
                                              HttpServletRequest request) {
        userService.getLoginUser(request);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR, "空间用户不存在");
        return ResultUtil.success(ObjectUtil.isNotNull(spaceUser));
    }

    /**
     * 获取空间所有成员信息
     * @param spaceUserQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        userService.getLoginUser(request);
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserList);
        return ResultUtil.success(spaceUserVOList);
    }

    /**
     * 编辑空间成员信息
     * @param spaceUserEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest,
                                               HttpServletRequest request) {
        userService.getLoginUser(request);
        ThrowUtils.throwIf(spaceUserEditRequest.getId() == null || spaceUserEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        // 实体类转换
        SpaceUser spaceUser = BeanUtil.copyProperties(spaceUserEditRequest, SpaceUser.class);
        // 数据校验
        spaceUserService.validSpaceUser(spaceUser, false);
        Long spaceUserId = spaceUserEditRequest.getId();
        // 检查是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(spaceUserId);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR, "空间用户不存在");
        // 更新数据库
        boolean updated = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(true);
    }

    /**
     * 获取当前用户加入的团队空间信息
     * @param request
     * @return
     */
    @GetMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMySpaceUser(HttpServletRequest request) {
        Long userId = userService.getLoginUser(request).getId();
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(userId);
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserList);
        return ResultUtil.success(spaceUserVOList);
    }
}