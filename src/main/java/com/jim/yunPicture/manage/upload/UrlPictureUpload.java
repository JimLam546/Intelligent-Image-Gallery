package com.jim.yunPicture.manage.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import com.jim.yunPicture.exception.BusinessException;
import com.jim.yunPicture.exception.ErrorCode;
import com.jim.yunPicture.exception.ThrowUtils;
import com.qcloud.cos.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jim_Lam
 * @description UrlPictureUpload
 */

@Service
@Slf4j
public class UrlPictureUpload extends PictureUploadTemplate{

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp");

    private static final long TWO_MB = 1024 * 1024 * 2L;

    @Override
    public void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(fileUrl == null, ErrorCode.PARAMS_ERROR, "url不能为空");
        try {
            // 1. 验证URL格式是否正确
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            log.error("url格式错误", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "url格式错误");
        }
        // 2. 验证URL的协议是否符合
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "url协议错误");
        // 3. 发送 HEAD 请求验证文件是否存在
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件的类型
            String contentType = httpResponse.header("Content-Type");
            if (CharSequenceUtil.isNotBlank(contentType)) {
                ThrowUtils.throwIf(!ALLOWED_FILE_TYPES.contains(contentType), ErrorCode.PARAMS_ERROR, "文件类型不符合要求");
            }
            // 5. 校验文件大小
            String contentLength = httpResponse.header("Content-Length");
            if (CharSequenceUtil.isNotBlank(contentLength)) {
                ThrowUtils.throwIf(Long.parseLong(contentLength) > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小超过2MB");
            }
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }



    @Override
    public String getOriginalFileName(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 从URL中提取文件名称
        return FileUtil.getName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        // 从URL中下载文件，存储到临时文件中
        HttpUtil.downloadFile(fileUrl, file);
    }
}