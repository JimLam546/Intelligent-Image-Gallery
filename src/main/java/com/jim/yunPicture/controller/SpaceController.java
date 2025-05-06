package com.jim.yunPicture.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jim.yunPicture.annotation.AuthCheck;
import com.jim.yunPicture.common.BaseResponse;
import com.jim.yunPicture.common.ResultUtil;
import com.jim.yunPicture.constant.UserConstant;
import com.jim.yunPicture.entity.Space;
import com.jim.yunPicture.entity.User;
import com.jim.yunPicture.entity.request.DeleteRequest;
import com.jim.yunPicture.entity.request.SpaceAddRequest;
import com.jim.yunPicture.entity.request.SpaceQueryRequest;
import com.jim.yunPicture.entity.request.SpaceUpdateRequest;
import com.jim.yunPicture.entity.vo.SpaceVO;
import com.jim.yunPicture.entity.vo.UserVO;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.exception.ThrowUtils;
import com.jim.yunPicture.service.SpaceService;
import com.jim.yunPicture.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jim_Lam
 * @description SpaceController
 */

@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(ObjectUtil.isNotNull(loginUser), ErrorCode.NOT_LOGIN_ERROR);
        Long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        ThrowUtils.throwIf(spaceId <= 0, ErrorCode.OPERATION_ERROR, "空间创建失败");
        return ResultUtil.success(true);
        }
    
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNotNull(spaceUpdateRequest.getId()) || spaceUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(spaceUpdateRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isNotNull(space), ErrorCode.NOT_FOUND_ERROR, "存储空间不存在");
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        // 合法性检验
        spaceService.validSpace(space, false);
        spaceService.fileSpaceBySpaceLevel(space);
        boolean res = spaceService.updateById(space);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtil.success(true);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除空间")
    public BaseResponse<Boolean> deleteById(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(deleteRequest.getId()) || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        UserVO loginUser = userService.getLoginUser(request);
        Space space = spaceService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "该存储空间不存在");
        // 验证是否是管理员和图片用户
        ThrowUtils.throwIf(!userService.isAdmin(loginUser) &&
                        !Objects.equals(space.getUserId(), loginUser.getId())
                , ErrorCode.NO_AUTH_ERROR, "无操作权限");
        boolean res = spaceService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtil.success(true);
    }

    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "获取空间信息（管理员）")
    public BaseResponse<SpaceVO> getPictureById(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceQueryRequest.getId())|| spaceQueryRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(spaceQueryRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "存储空间不存在");
        SpaceVO spaceVO = Space.objToVO(space);
        if (ObjectUtil.isNotNull(space.getUserId())) {
            UserVO userVO = User.objToVO(userService.getById(space.getUserId()));
            if (ObjectUtil.isNotNull(spaceVO)) {
                spaceVO.setUser(userVO);
            }
        }
        return ResultUtil.success(spaceVO);
    }

    @PostMapping("/getVO/page")
    @ApiOperation(value = "获取图片列表")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<SpaceVO>> getSpaceVOListByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest.getPageSize() > 30, ErrorCode.PARAMS_ERROR, "一页最大数量不能超过30");
        Page<Space> spacePage = new Page<>(spaceQueryRequest.getCurrent(), spaceQueryRequest.getPageSize());
        QueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);
        spacePage = spaceService.page(spacePage, queryWrapper);
        List<SpaceVO> spaceVOList = spacePage.getRecords().stream().map(Space::objToVO).collect(Collectors.toList());
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        // 根据创建用户id查询用户信息
        Set<Long> idList = spaceVOList.stream().map(SpaceVO::getUserId).collect(Collectors.toSet());
        Map<Long, List<UserVO>> idListMap = userService.listByIds(idList).stream().map(User::objToVO).collect(Collectors.groupingBy(UserVO::getId));
        spaceVOList.forEach(spaceVO -> spaceVO.setUser(idListMap.get(spaceVO.getUserId()).get(0)));
        spaceVOPage.setRecords(spaceVOList);
        return ResultUtil.success(spaceVOPage);
    }
}