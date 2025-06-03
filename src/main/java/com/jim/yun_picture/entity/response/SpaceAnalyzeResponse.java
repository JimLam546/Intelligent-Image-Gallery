package com.jim.yun_picture.entity.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceAnalyzeResponse
 */

@Data
public class SpaceAnalyzeResponse implements Serializable {

    private static final long serialVersionUID = -7083169969315980857L;

    /**
     * 已使用大小
     */
    private Long usedSize;

    /**
     * 总大小
     */
    private Long maxSize;

    /**
     * 空间使用比例
     */
    private Double usedSizeRatio;

    /**
     * 当前图片数量
     */
    private Long usedCount;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 空间数量占比
     */
    private Double spaceCountRatio;
}