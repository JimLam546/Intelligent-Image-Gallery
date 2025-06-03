package com.jim.yun_picture.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceAnalyzeRequest
 */

@Data
public class SpaceAnalyzeRequest implements Serializable {
    private static final long serialVersionUID = 2245534063980585497L;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 是否查询公开空间
     */
    private boolean queryPublic;

    /**
     * 是否查询所有空间
     */
    private boolean queryAll;
}