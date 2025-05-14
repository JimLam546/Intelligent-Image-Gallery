package com.jim.yun_picture.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description PictureQueryRequest
 */

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "空间查询请求")
public class SpaceQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -4231588544612487493L;

    /**
     * 空间ID
     */
    @ApiModelProperty(value = "空间ID")
    private Long id;

    /**
     * 空间名称
     */
    @ApiModelProperty(value = "空间名称")
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @ApiModelProperty(value = "空间级别：0-普通版 1-专业版 2-旗舰版")
    private Integer spaceLevel;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private Long userId;
}