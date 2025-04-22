package com.jim.yunPicture.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jim_Lam
 * @description PictureQueryRequest
 */

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "图片查询请求")
public class PictureQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -4231588544612487493L;

    /**
     * id
     */
    @ApiModelProperty(value = "图片ID", dataType = "Long")
    private Long id;

    /**
     * 图片名称和名称的模糊查询
     */
    @ApiModelProperty(value = "图片名称和名称的模糊查询", dataType = "String")
    private String searchText;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类", dataType = "String")
    private String category;

    /**
     * 标签（JSON 数组）
     */
    @ApiModelProperty(value = "标签（JSON 数组）")
    private List<String> tags;

    /**
     * 图片体积
     */
    @ApiModelProperty(value = "图片体积", dataType = "Long")
    private Long picSize;

    /**
     * 图片宽度
     */
    @ApiModelProperty(value = "图片宽度", dataType = "Integer")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @ApiModelProperty(value = "图片高度", dataType = "Integer")
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    @ApiModelProperty(value = "图片宽高比例", dataType = "Double")
    private Double picScale;

    /**
     * 图片格式
     */
    @ApiModelProperty(value = "图片格式", dataType = "String")
    private String picFormat;

    /**
     * 创建用户 id
     */
    @ApiModelProperty(value = "创建用户 id", dataType = "Long")
    private Long userId;
}