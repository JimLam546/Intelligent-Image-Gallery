package com.jim.yunPicture.controller;

import com.jim.yunPicture.config.COSClientConfig;
import com.jim.yunPicture.manage.COSManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;


/**
 * @author Jim_Lam
 * @description FileController
 */

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private COSManager cosManager;

    @Resource
    private COSClientConfig cosClientConfig;

    final String TEST_PATH = "test/";

    @PostMapping("/test/upload")
    public boolean upload(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fullPathName = TEST_PATH + fileName;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test/" + fileName, null);
            file.transferTo(tempFile);
            log.info("临时文件名称：{}", tempFile.getName());
            log.info("图片URL：{}/test/{}", cosClientConfig.getHost(), tempFile.getName());
            cosManager.putObject(fullPathName, tempFile);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    @PostMapping("/test/download")
    public void download(String fileName, HttpServletResponse response) {
        log.info(fileName);
        COSObject object = cosManager.getObject(fileName);
        COSObjectInputStream objectInputStream = null;
        try {
            objectInputStream = object.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(objectInputStream);
            // 设置响应头（以二进制方式返回给前端）
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("content-Disposition","attachment; filename=211924PrUvQ.jpg");
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}