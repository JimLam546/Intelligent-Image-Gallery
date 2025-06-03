package com.jim.yun_picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jim.yun_picture.entity.Picture;
import com.jim.yun_picture.entity.request.SpaceAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceCategoryAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceSizeAnalyzeRequest;
import com.jim.yun_picture.entity.request.SpaceTagAnalyzeRequest;
import com.jim.yun_picture.entity.response.SpaceAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceCategoryAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceSizeAnalyzeResponse;
import com.jim.yun_picture.entity.response.SpaceTagAnalyzeResponse;
import com.jim.yun_picture.entity.vo.UserVO;

import java.util.List;

/**
 * @author Jim_Lam
 * @description SpaceAnalyzeService
 */
public interface SpaceAnalyzeService {

    SpaceAnalyzeResponse getSpaceUsageAnalyze(SpaceAnalyzeRequest spaceAnalyzeRequest, UserVO userVO);

    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, UserVO loginUser);

    void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper);

    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, UserVO loginUser);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, UserVO loginUser);
}