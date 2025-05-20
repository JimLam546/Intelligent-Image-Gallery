package com.jim.yun_picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jim.yun_picture.entity.Picture;
import com.jim.yun_picture.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jim.yun_picture.entity.request.SpaceAddRequest;
import com.jim.yun_picture.entity.request.SpaceQueryRequest;
import com.jim.yun_picture.entity.vo.SpaceVO;
import com.jim.yun_picture.entity.vo.UserVO;

/**
* @author Jim_Lam
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-05-02 18:22:45
*/
public interface SpaceService extends IService<Space> {
    void validSpace(Space space, boolean add);

    Page<SpaceVO> getSpaceVOListByPage(Page<Space> spacePage, UserVO loginUser);

    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    void fileSpaceBySpaceLevel(Space space);

    Long addSpace(SpaceAddRequest spaceAddRequest, UserVO loginUser);

    Space checkSpaceCapacity(Long spaceId);

    boolean updateSpaceCapacity(Space space, Picture picture);
}
