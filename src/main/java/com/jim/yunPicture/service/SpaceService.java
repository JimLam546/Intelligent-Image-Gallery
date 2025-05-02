package com.jim.yunPicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jim.yunPicture.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jim.yunPicture.entity.request.SpaceQueryRequest;
import com.jim.yunPicture.entity.vo.SpaceVO;
import com.jim.yunPicture.entity.vo.UserVO;

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
}
