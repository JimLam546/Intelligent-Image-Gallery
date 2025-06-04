package com.jim.yun_picture.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jim_Lam
 * @description SpaceUserEditRequest
 */

@Data
public class SpaceUserEditRequest implements Serializable {
    private static final long serialVersionUID = 6336711031499037193L;

    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}