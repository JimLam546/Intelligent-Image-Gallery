package com.jim.yunPicture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jim.yunPicture.entity.Picture;
import com.jim.yunPicture.entity.dto.PictureUploadResult;
import com.jim.yunPicture.entity.enums.PictureReviewStatusEnum;
import com.jim.yunPicture.entity.request.PictureQueryRequest;
import com.jim.yunPicture.entity.request.PictureUploadByBatchRequest;
import com.jim.yunPicture.entity.request.PictureUploadRequest;
import com.jim.yunPicture.entity.vo.PictureVO;
import com.jim.yunPicture.entity.vo.UserVO;
import com.jim.yunPicture.exception.BusinessException;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.exception.ThrowUtils;
import com.jim.yunPicture.manage.upload.FilePictureUpload;
import com.jim.yunPicture.manage.upload.PictureUploadTemplate;
import com.jim.yunPicture.manage.upload.UrlPictureUpload;
import com.jim.yunPicture.service.PictureService;
import com.jim.yunPicture.mapper.PictureMapper;
import com.jim.yunPicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

/**
 * @author Jim_Lam
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-04-17 22:06:10
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UserService userService;
    @Autowired
    private UrlPictureUpload urlPictureUpload;

    /**
     * 上传图片
     *
     * @return
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest uploadRequest, UserVO loginUser) {
        ThrowUtils.throwIf(inputSource == null, ErrorCode.PARAMS_ERROR);
        // 用于判断是新增还是修改
        Long pictureId = null;
        if (uploadRequest != null) {
            pictureId = uploadRequest.getId();
        }
        if (pictureId != null && pictureId > 0) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(ObjectUtil.isNull(oldPicture), ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 仅本人和管理员可以修改
            ThrowUtils.throwIf(ObjectUtil.notEqual(loginUser.getId(), oldPicture.getUserId())
                            && !userService.isAdmin(loginUser),
                    ErrorCode.OPERATION_ERROR, "无权限");
        }
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        PictureUploadResult pictureUploadResult = pictureUploadTemplate.uploadPicture(inputSource, loginUser);
        Picture picture = PictureUploadResult.objToDO(pictureUploadResult, loginUser);
        fillReviewParam(picture, loginUser);
        // 如果ID存在，则表示更新记录
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean res = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "上传失败");
        // 上传图片，添加数据库记录
        return Picture.objToVO(picture);
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjectUtil.isNull(id), ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        ThrowUtils.throwIf(url.length() > 512, ErrorCode.PARAMS_ERROR, "图片地址不能太长");
        ThrowUtils.throwIf(introduction.length() > 512, ErrorCode.PARAMS_ERROR, "图片介绍不能太长");
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        String tagsJSON = null;
        if (CollUtil.isNotEmpty(pictureQueryRequest.getTags())) {
            tagsJSON = JSONUtil.toJsonStr(pictureQueryRequest.getTags());
        }
        queryWrapper.lambda().and(ObjectUtil.isNotNull(pictureQueryRequest.getSearchText()),
                        pictureLambdaQueryWrapper ->
                                // 模糊查询图片名称和图片简介
                                pictureLambdaQueryWrapper
                                        .like(Picture::getName, pictureQueryRequest.getSearchText())
                                        .or()
                                        .like(Picture::getIntroduction, pictureQueryRequest.getSearchText()))
                .like(ObjectUtil.isNotNull(pictureQueryRequest.getCategory()), Picture::getCategory, pictureQueryRequest.getCategory())
                .in(CharSequenceUtil.isNotBlank(tagsJSON), Picture::getTags, pictureQueryRequest.getTags())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicWidth()), Picture::getPicWidth, pictureQueryRequest.getPicWidth())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicHeight()), Picture::getPicHeight, pictureQueryRequest.getPicHeight())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicScale()), Picture::getPicScale, pictureQueryRequest.getPicScale())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicFormat()), Picture::getPicFormat, pictureQueryRequest.getPicFormat())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getUserId()), Picture::getUserId, pictureQueryRequest.getUserId())
                .eq(ObjectUtil.isNotNull(pictureQueryRequest.getPicSize()), Picture::getPicSize, pictureQueryRequest.getPicSize());
        return queryWrapper;
    }

    public void fillReviewParam(Picture picture, UserVO loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动审核
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动通过审核");
            picture.setReviewTime(new Date());
            picture.setReviewerId(loginUser.getId());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, UserVO loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        ThrowUtils.throwIf(CharSequenceUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索文本不能为空");
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count <= 0, ErrorCode.PARAMS_ERROR, "图片数量不能小于等于0");
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "图片数量不能大于30");
        // 抓取的网址
        String fetchUrl = String.format("https://www.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document = null;
        try {
            document = Jsoup.connect(fetchUrl).get();

        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element imgpt = document.getElementsByClass("dgControl").first();
        ThrowUtils.throwIf(ObjectUtil.isNull(imgpt), ErrorCode.OPERATION_ERROR, "获取页面失败");
        Elements elements = imgpt.select("img.mimg");
        int uploadCount = 0;
        for(Element element : elements) {
            String src = element.attr("src");
            if (CharSequenceUtil.isBlank(src)) {
                log.info("当前图片链接为空，已跳过:{}", src);
                continue;
            }
            src = src.contains("?") ? src.substring(0, src.indexOf("?")) : src;
            try {
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                pictureUploadRequest.setFileUrl(src);
                this.uploadPicture(src, pictureUploadRequest, loginUser);
                log.info("图片上传成功，图片路径为：{}", src);
                uploadCount++;
                if (uploadCount >= count) {
                    break;
                }
            } catch (Exception e) {
                log.error("图片上传失败", e);
            }
        }
        return uploadCount;
    }
}




