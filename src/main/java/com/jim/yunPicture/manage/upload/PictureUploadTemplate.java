package com.jim.yunPicture.manage.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import com.jim.yunPicture.config.COSClientConfig;
import com.jim.yunPicture.entity.dto.PictureUploadResult;
import com.jim.yunPicture.entity.vo.UserVO;
import com.jim.yunPicture.exception.BusinessException;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.manage.COSManager;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author Jim_Lam
 * @description PictureUploadTemplate
 */

@Service
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private COSClientConfig cosClientConfig;

    @Resource
    private COSManager cosManager;

    @Value("${cos.path.uploadPath}")
    private String UPLOAD_PATH;

    private static final String TEMP_FILE_PREFIX = "temp";

    /**
     * @param inputSource MultipartFile 或 Url-String
     * @param loginUser
     * @return
     */
    public PictureUploadResult uploadPicture(Object inputSource, UserVO loginUser) {
        // 1. 校验图片合法性
        validPicture(inputSource);
        // 2. 获取图片文件名（包含后缀）
        String originalFileName = getOriginalFileName(inputSource);
        String fileSuffix = FileUtil.getSuffix(originalFileName);
        String fileName = String.format("%s_%s.%s", DateUtil.format(new Date(), "yyyyMMddHHmmss"), UUID.randomUUID(), fileSuffix);
        // 上传路径 + 文件名
        String filePath = String.format("%s/%s", UPLOAD_PATH + loginUser.getId(), fileName);
        // 3. 创建临时文件
        File file = null;
        try {
            file = File.createTempFile(TEMP_FILE_PREFIX, "." + fileSuffix);
            // 处理文件来源
            processFile(inputSource, file);
            // 4. 上传图片到 COS
            PutObjectResult putObjectResult = cosManager.putObject(filePath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 5. 封装图片上传结果集
            return buildResult(imageInfo, file, filePath, fileName);
        } catch (IOException e) {
            log.error("上传图片失败!", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            // 6. 处理临时文件
            deleteTempFile(file);
        }
    }

    public abstract void validPicture(Object inputSource);

    public abstract String getOriginalFileName(Object inputSource);

    protected abstract void processFile(Object inputSource, File file) throws IOException;

    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        if (!file.delete()) {
            log.info("临时文件删除失败!");
        }
    }

    /**
     * @param imageInfo
     * @param tempFile
     * @param filePath  上传路径 + 文件名
     * @return
     */
    public PictureUploadResult buildResult(ImageInfo imageInfo, File tempFile, String filePath, String fileName) {
        // 封装图片信息
        PictureUploadResult pictureUploadResult = new PictureUploadResult();
        int height = imageInfo.getHeight();
        int width = imageInfo.getWidth();
        double picScale = NumberUtil.round(width * 0.1 / height, 2).doubleValue();
        pictureUploadResult.setName(fileName);
        pictureUploadResult.setPicWidth(width);
        pictureUploadResult.setPicHeight(height);
        pictureUploadResult.setPicFormat(imageInfo.getFormat());
        pictureUploadResult.setPicScale(picScale);
        pictureUploadResult.setPicSize(FileUtil.size(tempFile));
        pictureUploadResult.setUrl(cosClientConfig.getHost() + filePath);
        return pictureUploadResult;
    }
}