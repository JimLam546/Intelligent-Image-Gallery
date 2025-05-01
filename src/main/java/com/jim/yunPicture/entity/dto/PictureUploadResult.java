package com.jim.yunPicture.entity.dto;

import cn.hutool.core.bean.BeanUtil;
import com.jim.yunPicture.entity.Picture;
import com.jim.yunPicture.entity.vo.PictureVO;
import com.jim.yunPicture.entity.vo.UserVO;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description PictureUploadResult
 */

@Data
public class PictureUploadResult implements Serializable {
    private static final long serialVersionUID = -164795229975023549L;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 被压缩后的图片 url
     */
    private String compressedUrl;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    public static Picture objToDO(PictureUploadResult pictureUploadResult, UserVO loginUser) {
        if (pictureUploadResult == null) {
            return null;
        }
        Picture picture = BeanUtil.copyProperties(pictureUploadResult, Picture.class);
        picture.setUserId(loginUser.getId());
        // Picture picture = new Picture();
        // picture.setUrl(pictureUploadResult.getUrl());
        // picture.setName(pictureUploadResult.getName());
        // picture.setPicSize(pictureUploadResult.getPicSize());
        // picture.setPicWidth(pictureUploadResult.getPicWidth());
        // picture.setPicHeight(pictureUploadResult.getPicHeight());
        // picture.setPicScale(pictureUploadResult.getPicScale());
        // picture.setPicFormat(pictureUploadResult.getPicFormat());
        // picture.setUserId(loginUser.getId());
        return picture;
    }
}