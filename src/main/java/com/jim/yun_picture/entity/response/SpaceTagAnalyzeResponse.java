package com.jim.yun_picture.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceTagAnalyzeResponse
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 2512623651152526470L;

    /**
     * 标签名称
     */
    private String tag;

    /**
     * 标签使用数量
     */
    private Long count;
}