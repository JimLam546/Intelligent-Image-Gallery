package com.jim.yun_picture.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceUserQueryRequest
 */

@Data
public class SpaceUserQueryRequest implements Serializable {
    private static final long serialVersionUID = -4182435384249328598L;

    private Long id;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 空间角色
     */
    private String spaceRole;
}