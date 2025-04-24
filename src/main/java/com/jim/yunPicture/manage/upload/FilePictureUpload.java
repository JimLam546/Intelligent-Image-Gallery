package com.jim.yunPicture.manage.upload;

import cn.hutool.core.io.FileUtil;
import com.jim.yunPicture.exception.BusinessException;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jim_Lam
 * @description FilePictureUpload
 */

@Service
@Slf4j
public class FilePictureUpload extends PictureUploadTemplate {

    private static final List<String> FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "bmp");

    @Override
    public void validPicture(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 1. 校验文件大小
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(file.getSize() > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2 M");
        // 2. 校验文件的格式
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        ThrowUtils.throwIf(!FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型不符合要求");
    }

    @Override
    public String getOriginalFileName(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        return file.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}