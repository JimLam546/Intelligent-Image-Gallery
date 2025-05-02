package com.jim.yunPicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jim.yunPicture.entity.Space;
import com.jim.yunPicture.entity.User;
import com.jim.yunPicture.entity.enums.SpaceLevelEnum;
import com.jim.yunPicture.entity.request.SpaceQueryRequest;
import com.jim.yunPicture.entity.vo.PictureVO;
import com.jim.yunPicture.entity.vo.SpaceVO;
import com.jim.yunPicture.entity.vo.UserVO;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.exception.ThrowUtils;
import com.jim.yunPicture.service.SpaceService;
import com.jim.yunPicture.mapper.SpaceMapper;
import com.jim.yunPicture.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jim_Lam
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-05-02 18:22:45
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    private final UserService userService;

    public SpaceServiceImpl(UserService userService) {
        this.userService = userService;
    }

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
                        && ObjectUtil.isNotNull(SpaceLevelEnum.getEnumByValue(spaceLevel)),
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
}




