package com.jim.yun_picture.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceCategoryAnalyzeResponse
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 3530689509591412681L;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片数量
     */
    private Long count;

    /**
     * 分类图片总大小
     */
    private Long totalSize;
}