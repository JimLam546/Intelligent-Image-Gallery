package com.jim.yunPicture.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jim_Lam
 * @description PictureUpdateRequest
 */

@Data
@ApiModel(description = "图片编辑请求")
public class PictureEditRequest implements Serializable {

    private static final long serialVersionUID = 1344357598632129549L;
    @ApiModelProperty(value = "图片ID", required = true)
    private Long id;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称")
    private String name;

    /**
     * 简介
     */
    @ApiModelProperty(value = "简介")
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类")
    private String category;

    /**
     * 标签
     */
    @ApiModelProperty(value = "标签")
    private List<String> tags;
}