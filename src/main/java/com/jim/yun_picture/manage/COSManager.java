package com.jim.yun_picture.manage;

import cn.hutool.core.io.FileUtil;
import com.jim.yun_picture.config.COSClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.LinkedList;

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
        LinkedList<PicOperations.Rule> rules = new LinkedList<>();
        // 添加图片处理规则
        String webpKey = FileUtil.mainName(path) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setBucket(cosClientConfig.getBucketName());
        compressRule.setFileId(webpKey);
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        thumbnailRule.setBucket(cosClientConfig.getBucketName());
        thumbnailRule.setFileId(FileUtil.mainName(path) + "_thumbnail.webp");
        thumbnailRule.setRule("imageView2/thumbnail/128x128");
        rules.add(thumbnailRule);
        // 构造图片处理规则
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    public COSObject getObject(String path) {
        return cosClient.getObject(cosClientConfig.getBucketName(), path);
    }

    /**
     * 删除对象
     * @param key 对象的路径，不包括域名
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucketName(), key);
    }
}