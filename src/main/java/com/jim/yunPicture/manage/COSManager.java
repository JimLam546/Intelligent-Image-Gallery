package com.jim.yunPicture.manage;

import com.jim.yunPicture.config.COSClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author Jim_Lam
 * @description COSManager
 */

@Component
public class COSManager {

    @Resource
    private COSClient cosClient;

    @Resource
    private COSClientConfig cosClientConfig;

    public PutObjectResult putObject(String path, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucketName(), path, file);
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    public COSObject getObject(String path) {
        return cosClient.getObject(cosClientConfig.getBucketName(), path);
    }
}