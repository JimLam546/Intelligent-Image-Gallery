package com.jim.yun_picture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jim.yun_picture.entity.Space;
import com.jim.yun_picture.entity.SpaceUser;
import com.jim.yun_picture.entity.User;
import com.jim.yun_picture.entity.enums.SpaceRoleEnum;
import com.jim.yun_picture.entity.request.SpaceUserAddRequest;
import com.jim.yun_picture.entity.request.SpaceUserQueryRequest;
import com.jim.yun_picture.entity.vo.SpaceUserVO;
import com.jim.yun_picture.exception.ErrorCode;
import com.jim.yun_picture.exception.ThrowUtils;
import com.jim.yun_picture.service.SpaceService;
import com.jim.yun_picture.service.SpaceUserService;
import com.jim.yun_picture.mapper.SpaceUserMapper;
import com.jim.yun_picture.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Jim_Lam
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-06-03 16:16:21
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceUser), ErrorCode.PARAMS_ERROR, "参数为空");

        // 校验角色
        String spaceRole = spaceUser.getSpaceRole();
        ThrowUtils.throwIf(CharSequenceUtil.isBlank(spaceRole), ErrorCode.PARAMS_ERROR, "空间角色不能为空");
        ThrowUtils.throwIf(SpaceRoleEnum.getEnumByValue(spaceRole) == null, ErrorCode.PARAMS_ERROR, "空间角色不存在");

        Long userId = spaceUser.getUserId();
        Long spaceId = spaceUser.getSpaceId();
        // 创建时校验
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(userId, spaceId), ErrorCode.PARAMS_ERROR, "参数为空");
            User user = userService.getById(userId);
            ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.PARAMS_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.PARAMS_ERROR, "空间不存在");
        }
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }

        List<SpaceUserVO> spaceUserVOS = spaceUserList.stream().map(spaceUser -> BeanUtil.copyProperties(spaceUser, SpaceUserVO.class))
                .collect(Collectors.toList());

        Set<Long> userIds = spaceUserVOS.stream().map(SpaceUserVO::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIds = spaceUserVOS.stream().map(SpaceUserVO::getSpaceId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIds).stream().collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceMap = spaceService.listByIds(spaceIds).stream().collect(Collectors.groupingBy(Space::getId));
        spaceUserVOS.forEach(spaceUserVO -> {
            Long spaceId = spaceUserVO.getSpaceId();
            Long userId = spaceUserVO.getUserId();

            if (spaceMap.containsKey(spaceId)) {
                spaceUserVO.setSpace(Space.objToVO(spaceMap.get(spaceId).get(0)));
            } else {
                spaceUserVO.setSpace(null);
            }

            if (userMap.containsKey(userId)) {
                spaceUserVO.setUser(User.objToVO(userMap.get(userId).get(0)));
            } else {
                spaceUserVO.setUser(null);
            }
        });

        return spaceUserVOS;
    }

    /**
     * 根据查询参数取值
     * @param spaceQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNull(spaceQueryRequest)) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long spaceId = spaceQueryRequest.getSpaceId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceRole = spaceQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjectUtil.isNotNull(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjectUtil.isNotNull(userId), "userId", userId);
        queryWrapper.eq(CharSequenceUtil.isNotBlank(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

    @Override
    public Long addSpaceUser(SpaceUserAddRequest spaceAddRequest) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);

        SpaceUser spaceUser = BeanUtil.copyProperties(spaceAddRequest, SpaceUser.class);
        validSpaceUser(spaceUser, true);

        boolean save = this.save(spaceUser);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }
}




