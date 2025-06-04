package com.jim.yun_picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jim.yun_picture.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jim.yun_picture.entity.request.SpaceUserAddRequest;
import com.jim.yun_picture.entity.request.SpaceUserQueryRequest;
import com.jim.yun_picture.entity.vo.SpaceUserVO;

import java.util.List;

/**
* @author Jim_Lam
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-06-03 16:16:21
*/
public interface SpaceUserService extends IService<SpaceUser> {

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceQueryRequest);

    Long addSpaceUser(SpaceUserAddRequest spaceAddRequest);

}
