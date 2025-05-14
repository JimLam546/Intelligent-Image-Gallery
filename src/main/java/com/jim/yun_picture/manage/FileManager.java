package com.jim.yun_picture.manage;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import com.jim.yun_picture.config.COSClientConfig;
import com.jim.yun_picture.entity.dto.PictureUploadResult;
import com.jim.yun_picture.entity.vo.UserVO;
import com.jim.yun_picture.exception.BusinessException;
import com.jim.yun_picture.exception.ErrorCode;
import com.jim.yun_picture.exception.ThrowUtils;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import java.util.Date;
import java.util.List;

/**
 * @author Jim_Lam
 * @description FileManager
 */

@Service
@Slf4j
public class FileManager {

    @Resource
    private COSManager cosManager;

    @Resource
    private COSClientConfig cosClientConfig;

    // @Value("${cos.path.uploadPath}")
    private String UPLOAD_PATH;

    private static final String TEMP_FILE_PREFIX = "temp";

    public PictureUploadResult uploadPicture(MultipartFile file, UserVO loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        validPicture(file);
        File tempFile = null;
        String filePath = String.format("%s/%s_%s.%s", UPLOAD_PATH + loginUser.getId(),
                DateUtil.format(new Date(), "yyyyMMddHHmmss"), UUID.randomUUID(), FileUtil.getSuffix(file.getOriginalFilename()));

        try{
            tempFile = File.createTempFile(TEMP_FILE_PREFIX, null);
            file.transferTo(tempFile);
            PutObjectResult putObjectResult = cosManager.putObject(filePath, tempFile);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装图片信息
            PictureUploadResult pictureUploadResult = new PictureUploadResult();
            int height = imageInfo.getHeight();
            int width = imageInfo.getWidth();
            double picScale = NumberUtil.round(width * 0.1 / height, 2).doubleValue();
            pictureUploadResult.setName(tempFile.getName());
            pictureUploadResult.setPicWidth(width);
            pictureUploadResult.setPicHeight(height);
            pictureUploadResult.setPicFormat(imageInfo.getFormat());
            pictureUploadResult.setPicScale(picScale);
            pictureUploadResult.setPicSize(file.getSize());
            pictureUploadResult.setUrl(cosClientConfig.getHost() + filePath);
            return pictureUploadResult;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            this.deleteTempFile(tempFile);
        }
    }

    private void validPicture(MultipartFile file) {
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 校验文件大小
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(file.getSize() > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2 M");
        // 2. 校验文件的格式
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        final List<String> FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "bmp");
        ThrowUtils.throwIf(!FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型不符合要求");
    }

    private void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete) {
            log.info("临时文件删除失败!");
        }
    }
}