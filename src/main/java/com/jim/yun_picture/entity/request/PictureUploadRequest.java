package com.jim.yun_picture.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description PictureUploadRequest
 */

@Data
@ApiModel("图片上传请求")
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 6130577646817127944L;

    @ApiModelProperty(value = "图片名称")
    private Long id;

    /**
     * 文件地址
     */
    @ApiModelProperty(value = "文件地址")
    private String fileUrl;

    /**
     * 空间id
     */
    @ApiModelProperty
    private Long spaceId;
}