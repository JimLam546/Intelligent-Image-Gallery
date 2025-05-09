package com.jim.yunPicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jim.yunPicture.common.BaseResponse;
import com.jim.yunPicture.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jim.yunPicture.entity.dto.PictureUploadResult;
import com.jim.yunPicture.entity.request.PictureQueryRequest;
import com.jim.yunPicture.entity.request.PictureUploadByBatchRequest;
import com.jim.yunPicture.entity.request.PictureUploadRequest;
import com.jim.yunPicture.entity.vo.PictureVO;
import com.jim.yunPicture.entity.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
}
