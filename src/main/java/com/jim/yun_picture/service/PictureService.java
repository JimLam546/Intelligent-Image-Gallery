package com.jim.yun_picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jim.yun_picture.common.BaseResponse;
import com.jim.yun_picture.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jim.yun_picture.entity.request.DeleteRequest;
import com.jim.yun_picture.entity.request.PictureQueryRequest;
import com.jim.yun_picture.entity.request.PictureUploadByBatchRequest;
import com.jim.yun_picture.entity.request.PictureUploadRequest;
import com.jim.yun_picture.entity.vo.PictureVO;
import com.jim.yun_picture.entity.vo.UserVO;

/**
* @author Jim_Lam
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-04-17 22:06:10
*/
public interface PictureService extends IService<Picture> {
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, UserVO loginUser);

    void validPicture(Picture picture);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    void fillReviewParam(Picture picture, UserVO loginUser);

    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, UserVO loginUser);

    BaseResponse<Page<PictureVO>> getPictureVOListByPageWithCache(PictureQueryRequest pictureQueryRequest, String key);

    void checkPictureAuth(Picture picture, UserVO loginUser);

    Picture deletePicture(DeleteRequest deleteRequest, UserVO loginUser);

    void clearPictureFile(Picture oldPicture);
}
