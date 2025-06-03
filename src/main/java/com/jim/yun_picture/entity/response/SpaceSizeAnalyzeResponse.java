package com.jim.yun_picture.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceSizeAnalyzeResponse
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 5519597440447743748L;

    /**
     * 大小范围
     */
    private String sizeRange;

    /**
     * 图片数量
     */
    private Long count;
}