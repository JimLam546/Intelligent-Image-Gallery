package com.jim.yun_picture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jim.yun_picture.entity.Picture;
import com.jim.yun_picture.entity.Space;
import com.jim.yun_picture.entity.SpaceUser;
import com.jim.yun_picture.entity.User;
import com.jim.yun_picture.entity.enums.SpaceLevelEnum;
import com.jim.yun_picture.entity.enums.SpaceRoleEnum;
import com.jim.yun_picture.entity.enums.SpaceTypeEnum;
import com.jim.yun_picture.entity.request.SpaceAddRequest;
import com.jim.yun_picture.entity.request.SpaceQueryRequest;
import com.jim.yun_picture.entity.vo.SpaceVO;
import com.jim.yun_picture.entity.vo.UserVO;
import com.jim.yun_picture.exception.ErrorCode;
import com.jim.yun_picture.exception.ThrowUtils;
import com.jim.yun_picture.service.SpaceService;
import com.jim.yun_picture.mapper.SpaceMapper;
import com.jim.yun_picture.service.SpaceUserService;
import com.jim.yun_picture.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Jim_Lam
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-05-02 18:22:45
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    private final ConcurrentHashMap<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * @param space
     * @param add   判断是否为第一次创建
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.PARAMS_ERROR, "参数为空");
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        // 创建时校验
        if (add) {
            ThrowUtils.throwIf(CharSequenceUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(ObjectUtil.isNull(spaceLevel), ErrorCode.PARAMS_ERROR, "空间等级不能为空");
        }
        // 修改时校验
        ThrowUtils.throwIf(spaceName.length() > 20, ErrorCode.PARAMS_ERROR, "空间名称不能太长");
        ThrowUtils.throwIf(ObjectUtil.isNotNull(spaceLevel)
                        && ObjectUtil.isNull(SpaceLevelEnum.getEnumByValue(spaceLevel)),
                ErrorCode.PARAMS_ERROR, "空间等级错误");
    }

    @Override
    public Page<SpaceVO> getSpaceVOListByPage(Page<Space> spacePage, UserVO loginUser) {
        List<Space> spacePageRecords = spacePage.getRecords();
        Page<SpaceVO> pictureVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spacePageRecords)) {
            return pictureVOPage;
        }
        List<SpaceVO> spaceVOList = spacePageRecords.stream().map(Space::objToVO).collect(Collectors.toList());
        Set<Long> userIdSet = spacePageRecords.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<UserVO>> listMap = userService.listByIds(userIdSet).stream().map(User::objToVO).collect(Collectors.groupingBy(UserVO::getId));
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            UserVO userVO = null;
            if (listMap.containsKey(userId)) {
                userVO = listMap.get(userId).get(0);
            }
            spaceVO.setUser(userVO);
        });
        pictureVOPage.setRecords(spaceVOList);
        return pictureVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNull(spaceQueryRequest)) {
            return spaceQueryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Long userId = spaceQueryRequest.getUserId();
        spaceQueryWrapper.lambda()
                .eq(ObjectUtil.isNotEmpty(id), Space::getId, id)
                .like(CharSequenceUtil.isNotBlank(spaceName), Space::getSpaceName, spaceName)
                .eq(ObjectUtil.isNotEmpty(spaceLevel), Space::getSpaceLevel, spaceLevel)
                .eq(ObjectUtil.isNotEmpty(userId), Space::getUserId, userId);
        return spaceQueryWrapper;
    }

    @Override
    public void fileSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        ThrowUtils.throwIf(ObjectUtil.isNull(enumByValue), ErrorCode.PARAMS_ERROR, "空间设置失败");
        if (ObjectUtil.isNull(space.getMaxSize())) {
            space.setMaxSize(enumByValue.getMaxSize());
        }
        if (ObjectUtil.isNull(space.getMaxCount())) {
            space.setMaxCount(enumByValue.getMaxCount());
        }
    }

    @Override
    public Long addSpace(SpaceAddRequest spaceAddRequest, UserVO loginUser) {
        Space space = BeanUtil.copyProperties(spaceAddRequest, Space.class);
        if (ObjectUtil.isNull(spaceAddRequest.getSpaceLevel())) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }

        if (CharSequenceUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");
        }

        // 参数校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        space.setMaxSize(SpaceLevelEnum.COMMON.getMaxSize());
        space.setMaxCount(SpaceLevelEnum.COMMON.getMaxCount());
        // 权限校验
        ThrowUtils.throwIf(space.getSpaceLevel().equals(SpaceLevelEnum.COMMON.getValue())
                        && !userService.isAdmin(loginUser),
                ErrorCode.NO_AUTH_ERROR);

        // 控制一个用户只能有一个空间
        lockMap.putIfAbsent(userId, new Object());
        synchronized (lockMap.get(userId)) {
            try {
                Long spaceId = transactionTemplate.execute(status -> {
                    // 后面验证可以换成检查用户表是否存在 spaceId
                    boolean exists = this.lambdaQuery().eq(Space::getUserId, userId)
                            .eq(Space::getSpaceLevel, SpaceLevelEnum.COMMON.getValue()).exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "用户只能创建一个空间");
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "空间创建失败");
                    boolean update = userService.lambdaUpdate().eq(User::getId, userId).set(User::getSpaceId, space.getId()).update();
                    ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间关联用户失败");
                    if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequest.getSpaceType()) {
                        // 如果是创建团队空间则添加团队成员记录
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(userId);
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        boolean save = spaceUserService.save(spaceUser);
                        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建空间关联创建用户失败");
                    }
                    return space.getId();
                });
                return Optional.ofNullable(spaceId).orElse(-1L);
            } finally {
                // 防止报错而不清理 userId 导致内存泄漏
                lockMap.remove(userId);
            }
        }
    }

    @Override
    public Space checkSpaceCapacity(Long spaceId) {
        Space space = this.getById(spaceId);
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 检验额度
        ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(),
                ErrorCode.OPERATION_ERROR, "空间文件数量已达上限");
        ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(),
                ErrorCode.OPERATION_ERROR, "空间内存大小已达上限");
        return space;
    }

    @Override
    public boolean updateSpaceCapacity(Space oldSpace, Picture picture) {
        // 判断空间大小是否足够放下
        Space space = new Space();
        space.setId(oldSpace.getId());
        space.setTotalCount(oldSpace.getTotalCount() + 1);
        space.setTotalSize(oldSpace.getTotalSize() + picture.getPicSize());
        this.updateById(space);
        return true;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, UserVO loginUser) {
        SpaceVO spaceVO = BeanUtil.copyProperties(space, SpaceVO.class);
        spaceVO.setUser(loginUser);
        return spaceVO;
    }
}




