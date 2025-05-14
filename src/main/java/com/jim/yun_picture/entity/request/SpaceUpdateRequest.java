package com.jim.yun_picture.entity.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceUpdateRequest
 */

@Data
@ApiModel("空间更新请求")
public class SpaceUpdateRequest implements Serializable {
    private static final long serialVersionUID = 5607771179579005517L;

    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;
}