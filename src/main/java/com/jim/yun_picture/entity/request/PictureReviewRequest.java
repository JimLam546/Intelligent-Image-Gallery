package com.jim.yun_picture.entity.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description PictureReviewRequest
 */

@Data
@ApiModel(value = "图片审核请求")
public class PictureReviewRequest implements Serializable {

    private static final long serialVersionUID = -8728815335870606497L;

    /**
     * 图片ID
     */
    private Long id;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;
}