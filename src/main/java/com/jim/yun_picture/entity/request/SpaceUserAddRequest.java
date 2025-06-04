package com.jim.yun_picture.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceUserAddRequest
 */

@Data
public class SpaceUserAddRequest implements Serializable {

    private static final long serialVersionUID = -8514218707377647983L;
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